/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.cedarsoftware.util.io.JsonReader;
import com.signomix.in.http.IntegrationApi;
import com.signomix.in.http.KpnApi;
import com.signomix.in.http.LoRaApi;
import com.signomix.in.http.TtnApi;
import com.signomix.iot.IotData2;
import com.signomix.iot.IotEvent;
import com.signomix.iot.lora.LoRaData;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.iot.TtnData;
import com.signomix.iot.kpn.KPNData;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.VirtualDevice;
import com.signomix.out.iot.VirtualStackIface;
import com.signomix.out.script.ScriptingAdapterIface;
import com.signomix.util.HexTool;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DeviceIntegrationModule {

    private static DeviceIntegrationModule logic;

    public static DeviceIntegrationModule getInstance() {
        if (logic == null) {
            logic = new DeviceIntegrationModule();
        }
        return logic;
    }

    public Object processLoRaRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter, LoRaApi loraApi) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = request.headers.getFirst("Authorization");
            if (authKey == null || authKey.isEmpty()) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
                if (debugMode) {
                    result.setCode(HttpAdapter.SC_UNAUTHORIZED);
                    result.setData("Authorization header not found");
                }
                return result;
            }
            String jsonString = request.body;

            jsonString
                    = "{\"@type\":\"com.signomix.iot.lora.LoRaData\","
                    + jsonString.substring(jsonString.indexOf("{") + 1);
            LoRaData data = null;
            try {

                data = (LoRaData) JsonReader.jsonToJava(jsonString);
                data.normalize();
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem: incompatible format " + jsonString));
                e.printStackTrace();
            }
            if (data == null) {
                //TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }

            // save value and timestamp in device's channel witch name is the same as the field name
            boolean isRegistered = false;
            Device device;
            try {
                //device = thingsAdapter.getDevice(data.getUserId(), data.getDeviceId());
                device = thingsAdapter.getDevice(data.getDevEUI());
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDevEUI() + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if ("VIRTUAL".equals(device.getType()) || device.getType().startsWith("TTN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDevEUI() + " type is not valid"));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    result.setData("device type not valid");
                    return result;
                }

            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("device not found");
                return result;
            }

            //String secret = device.getKey();
            String secret = userAdapter.get(device.getUserID()).getConfirmString();
            String applicationSecret;
            boolean authorized = false;

            if (secret != null && !secret.isEmpty()) {
                authorized = authKey.equals(secret);
            }
            if (!authorized) {
                try {
                    User user = userAdapter.get(data.getApplicationID());
                    if (user != null) {
                        applicationSecret = user.getConfirmString();
                        authorized = authKey.equals(applicationSecret);
                    }
                } catch (UserException ex) {
                }
            }
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            //after successful authorization
            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            //check frame counter
            if (device.isCheckFrames()) {
                if (device.getLastFrame() == data.getfCnt()) {
                    //drop request
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this, "duplicated frame " + data.getfCnt() + " for " + device.getEUI()));
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData("OK");
                    return result;
                }
            }
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), data.getfCnt(), "", "");
            ArrayList<ChannelData> inputList = prepareLoRaValues(data, scriptingAdapter, device.getEncoderUnescaped(), device.getEUI(), device.getUserID());

            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList, device, scriptingAdapter,
                        data.getReceivedPackageTimestamp(), data.getLatitude(),
                        data.getLongitude(), data.getAltitude());
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
                }
                if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".processLoraRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     */
    public Object processIotRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter, IntegrationApi iotApi, ActuatorCommandsDBIface actuatorCommandsDB) {
        //TODO: Authorization
        RequestObject request = event.getRequest();

        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("");
        result.setHeader("Content-type", "text/plain");
        try {
            String authKey = request.headers.getFirst("Authorization");

            if (authKey == null || authKey.isEmpty()) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
                result.setCode(HttpAdapter.SC_UNAUTHORIZED);
                return result;
            }

            String dataString = request.body;
            String jsonString;
            if (dataString == null) {
                dataString = ((String) request.parameters.getOrDefault("data", "")).trim();
            }
            IotData2 data = null;
            if (dataString.isEmpty()) {
                data = parseIotData(request.parameters);
            } else {
                jsonString
                        = "{\"@type\":\"com.signomix.iot.IotData2\","
                        + dataString.substring(dataString.indexOf("{") + 1);

                try {
                    data = (IotData2) JsonReader.jsonToJava(jsonString);
                    data.normalize();
                } catch (Exception e) {
                    data = parseIotData(dataString);
                    if (null == data) {
                        Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "deserialization problem from " + request.clientIp + " " + jsonString));
                        //e.printStackTrace();
                    }
                }
            }
            if (data == null) {
                //TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem, the data format is not compatible with Signomix integration API");
                return result;
            }

            // save value and timestamp in device's channel witch name is the same as the field name
            boolean isRegistered = false;
            Device device;
            Device gateway;
            try {
                //device = thingsAdapter.getDevice(data.getUserId(), data.getDeviceId());
                device = thingsAdapter.getDevice(data.dev_eui);
                gateway = thingsAdapter.getDevice(data.gateway_eui);
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.dev_eui + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if ("VIRTUAL".equals(device.getType()) || device.getType().startsWith("TTN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.dev_eui + " type is not valid"));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    result.setData("device type not valid");
                    return result;
                }

            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("device not found");
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("exception");
                return result;
            }

            //TODO: jeśli w danych przyszła informacja nt. gateway'a, to weryfikujemy secret key gatewaya, a nie device
            String secret;
            if (gateway == null) {
                secret = device.getKey();
            } else {
                secret = gateway.getKey();
            }
            String applicationSecret;
            boolean authorized = false;

            if (secret != null && !secret.isEmpty()) {
                authorized = authKey.equals(secret);
            }
            if (!authorized) { //TODO: remove
                try {
                    User user = userAdapter.get(data.getApplicationID());
                    if (user != null) {
                        applicationSecret = user.getConfirmString();
                        authorized = authKey.equals(applicationSecret);
                    }
                } catch (UserException ex) {
                } catch (Exception ex) {
                }
            }
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }

            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            //after successful authorization
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), -1, "", "");

            ArrayList<ChannelData> inputList = prepareIotValues(data);
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult
                        = DataProcessor.processValues(inputList, device, scriptingAdapter,
                                data.getReceivedPackageTimestamp(), data.getLatitude(),
                                data.getLongitude(), data.getAltitude());
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
                }
                if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".processIotRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
            if (Device.GENERIC.equals(device.getType()) || Device.GATEWAY.equals(device.getType())) {
                Event command = ActuatorModule.getInstance().getCommand(device.getEUI(), actuatorCommandsDB);
                if (null != command) {
                    String commandPayload = (String) command.getPayload();
                    System.out.println("EVENT CATEGORY TYPE:" + command.getCategory() + " " + command.getType());
                    if (IotEvent.ACTUATOR_HEXCMD.equals(command.getType())) {
                        String rawCmd = new String(Base64.getEncoder().encode(HexTool.hexStringToByteArray(commandPayload)));
                        result.setPayload(rawCmd.getBytes());
                    } else {
                        result.setPayload(commandPayload.getBytes());
                    }
                    ActuatorModule.getInstance().archiveCommand(command, actuatorCommandsDB);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     */
    public Object processTtnRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter, TtnApi ttnApi) {
        //TODO: Authorization
        RequestObject request = event.getRequest();
        boolean authorizationRequired = !("false".equalsIgnoreCase(ttnApi.getProperty("authorization-required")));
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        String authKey = request.headers.getFirst("Authorization");

        if (authorizationRequired && (authKey == null || authKey.isEmpty())) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
            return result;
        }

        String jsonString = request.body;
        jsonString
                = "{\"@type\":\"com.signomix.iot.TtnData\","
                + jsonString.substring(jsonString.indexOf("{") + 1);
        TtnData data = null;
        try {

            data = (TtnData) JsonReader.jsonToJava(jsonString);
            data.normalize();
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem: incompatible format " + jsonString));
            e.printStackTrace();
            //TODO: send warning to the service admin about deserialization error
        }
        if (data == null) {
            //we don't send error code to TTN
            return result;
        }

        Device device;
        try {
            device = thingsAdapter.getDevice(data.getDeviceEUI());
            if (null == device) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDeviceEUI() + " is not registered"));
                return result;
            }
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        if (authorizationRequired) {
            try {
                if (!authKey.equals(device.getKey())) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization key don't match for " + device.getEUI()));
                    return result;
                }
            } catch (Exception ex) { //catch (UserException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                return result;
            }
        }

        if (!device.isActive()) {
            result.setData("device is not active");
            return result;
        }

        try {
            if (!device.getType().startsWith("TTN")) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDeviceEUI() + " type is not valid"));
                return result;
            }
            if (device.isCheckFrames()) {
                if (device.getLastFrame() == data.getFrameCounter()) {
                    //drop request
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this, "duplicated frame " + data.getFrameCounter() + " for " + device.getEUI()));
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData("OK");
                    return result;
                }
            }
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), data.getFrameCounter(), data.getDownlink(), data.getDeviceID());

        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        try {
            ArrayList<ChannelData> inputList = prepareTtnValues(data, scriptingAdapter, device.getEncoderUnescaped(), device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList,
                        device,
                        scriptingAdapter,
                        data.getReceivedPackageTimestamp(),
                        data.getLatitude(),
                        data.getLongitude(),
                        data.getAltitude());
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
                }
                if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".processTtnRequest()", e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    ArrayList<ChannelData> fixValues(Device device, ArrayList<ChannelData> values) {
        ArrayList<ChannelData> fixedList = new ArrayList<>();
        if (values != null && values.size() > 0) {
            for (ChannelData value : values) {
                if (device.getChannels().containsKey(value.getName())) {
                    fixedList.add(value);
                }
            }
        }
        return fixedList;
    }

    public Object processKpnRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter, KpnApi kpnApi) {
        //TODO: Authorization
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = null;

            String jsonString = request.body;
            jsonString
                    = "{\"@type\":\"com.signomix.iot.kpn.KPNData\","
                    + jsonString.substring(jsonString.indexOf("{") + 1);
            KPNData data = null;
            try {

                data = (KPNData) JsonReader.jsonToJava(jsonString);
                data.normalize();
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem: incompatible format " + jsonString));
                e.printStackTrace();
            }
            if (data == null) {
                //TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }

            // save value and timestamp in device's channel witch name is the same as the field name
            boolean isRegistered = false;
            Device device;
            try {
                //device = thingsAdapter.getDevice(data.getUserId(), data.getDeviceId());
                device = thingsAdapter.getDevice(data.getDeviceEUI());
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDeviceEUI() + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if (!device.getType().equalsIgnoreCase("KPN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + data.getDeviceEUI() + " type is not valid"));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    result.setData("device type not valid");
                    return result;
                }

            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("device not found");
                return result;
            }

            //String secret = device.getKey();
            String secret = userAdapter.get(device.getUserID()).getConfirmString();
            String applicationSecret;
            boolean authorized = true;

            //TODO: authorization
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            //after successful authorization

            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            //TODO: check frame counter
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), 0/*new frame count*/, "", "");
            ArrayList<ChannelData> inputList = prepareKpnValues(data, scriptingAdapter, device.getEncoderUnescaped(), device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList, device, scriptingAdapter,
                        data.getReceivedPackageTimestamp(), data.getLatitude(),
                        data.getLongitude(), data.getAltitude());
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
                }
                if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".processKpnRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processRawRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter, IntegrationApi rawApi, ActuatorCommandsDBIface actuatorCommandsDB) {
        //TODO: Authorization
        RequestObject request = event.getRequest();
        //TODO: kpnApi

        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = null;
            String deviceEUI;
            //TODO: header should be configurable
            deviceEUI = request.headers.getFirst(rawApi.getProperty("header-name"));

            if (deviceEUI == null) {
                //TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }
            deviceEUI = deviceEUI.toUpperCase();

            // save value and timestamp in device's channel witch name is the same as the field name
            boolean isRegistered = false;
            Device device;
            try {
                //device = thingsAdapter.getDevice(data.getUserId(), data.getDeviceId());
                device = thingsAdapter.getDevice(deviceEUI);
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + deviceEUI + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if (!device.getType().equalsIgnoreCase("GENERIC")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Device " + deviceEUI + " type is not valid"));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    result.setData("device type not valid");
                    return result;
                }

            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("device not found");
                return result;
            }

            //String secret = device.getKey();
            String secret = userAdapter.get(device.getUserID()).getConfirmString();
            String applicationSecret;
            boolean authorized = true;

            //TODO: authorization
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            //after successful authorization
            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), 0/*new frame count*/, "", "");
            ArrayList<ChannelData> finalValues = null;
            try {
                finalValues = DataProcessor.processRawValues(request.body, device, scriptingAdapter, System.currentTimeMillis());
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".processRawRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
            thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, finalValues));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<ChannelData> prepareTtnValues(TtnData data, ScriptingAdapterIface scriptingAdapter, String encoderCode, String deviceID, String userID) {
        ArrayList<ChannelData> values = new ArrayList<>();
        if (data.getPayloadFieldNames() == null || data.getPayloadFieldNames().length == 0) {
            if (null != data.getPayload()) {
                byte[] decodedPayload = Base64.getDecoder().decode(data.getPayload().getBytes());
                try {
                    values = scriptingAdapter.decodeData(decodedPayload, encoderCode, deviceID, data.getTimestamp(), userID);
                } catch (Exception e) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".prepareTtnValues for device " + deviceID, e.getMessage()));
                    fireEvent(1, deviceID, userID, e.getMessage());
                    return null;
                }
            }
        } else {
            TtnData processedData = new TtnData(data);
            // handling Cayenne LPP
            Iterator it = data.getPayloadFields().keySet().iterator();
            Object payloadField;
            String fieldName;
            while (it.hasNext()) {
                fieldName = (String) it.next();
                payloadField = data.getPayloadFields().get(fieldName);
                if (payloadField instanceof com.cedarsoftware.util.io.JsonObject) {
                    com.cedarsoftware.util.io.JsonObject j = (com.cedarsoftware.util.io.JsonObject) data.getPayloadFields().get(fieldName);
                    Iterator it2 = j.keySet().iterator();
                    String key;
                    while (it2.hasNext()) {
                        key = (String) it2.next();
                        processedData.putField(fieldName + "_" + key, j.get(key));
                    }
                    processedData.removeField(fieldName);
                } else {
                    // nothing to do
                }
            }
            // Cayenne LPP - end
            for (String payloadFieldName : processedData.getPayloadFieldNames()) {
                ChannelData mval = new ChannelData();
                mval.setDeviceEUI(processedData.getDeviceEUI());
                mval.setName(payloadFieldName.toLowerCase());
                mval.setValue(processedData.getDoubleValue(payloadFieldName));
                mval.setStringValue(processedData.getStringValue(payloadFieldName));
                if (data.getTimeField() != null) {
                    mval.setTimestamp(data.getTimeField().toEpochMilli());
                } else {
                    mval.setTimestamp(data.getTimestamp());
                }
                values.add(mval);
            }
        }
        return values;
    }

    private ArrayList<ChannelData> prepareIotValues(IotData2 data) {
        ArrayList<ChannelData> values = new ArrayList<>();

        for (int i = 0; i < data.payload_fields.size(); i++) {
            ChannelData mval = new ChannelData();
            mval.setDeviceEUI(data.getDeviceEUI());
            mval.setName((String) data.payload_fields.get(i).get("name"));
            try {
                mval.setValue((Double) data.payload_fields.get(i).get("value"));
            } catch (ClassCastException e) {
                mval.setValue((String) data.payload_fields.get(i).get("value"));
            }
            mval.setStringValue("" + data.payload_fields.get(i).get("value"));
            if (data.getTimeField() != null) {
                mval.setTimestamp(data.getTimeField().toEpochMilli());
            } else {
                mval.setTimestamp(data.getTimestamp());
            }
            if (mval.getTimestamp() == 0) {
                mval.setTimestamp(System.currentTimeMillis());
            }
            //System.out.println("TIMESTAMP:"+mval.getTimestamp());
            values.add(mval);
        }

        return values;
    }

    private ArrayList<ChannelData> prepareLoRaValues(LoRaData data, ScriptingAdapterIface scriptingAdapter, String encoderCode, String deviceID, String userID) {
        byte[] encodedPayload = Base64.getDecoder().decode(data.getData().getBytes());
        //przekształcamy tablicę bajtów na listę obiektów ChannelData
        ArrayList<ChannelData> values;
        try {
            values = scriptingAdapter.decodeData(encodedPayload, encoderCode, deviceID, data.getTimestamp(), userID);
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".prepareLoRaValues for device " + deviceID, e.getMessage()));
            fireEvent(1, deviceID, userID, e.getMessage());
            return null;
        }
        return values;
    }

    private ArrayList<ChannelData> prepareKpnValues(KPNData data, ScriptingAdapterIface scriptingAdapter, String encoderCode, String deviceID, String userID) {
        //przekształcamy hexadecimal payload na listę obiektów ChannelData
        ArrayList<ChannelData> values;
        try {
            values = scriptingAdapter.decodeHexData(data.getPayload(), encoderCode, deviceID, data.getTimestamp(), userID);
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".prepareLoRaValues for device " + deviceID, e.getMessage()));
            fireEvent(1, deviceID, userID, e.getMessage());
            return null;
        }
        return values;
    }

    public void writeVirtualData(ThingsDataIface thingsAdapter, ScriptingAdapterIface scriptingAdapter, Device device, ArrayList<ChannelData> values) {
        try {
            long now = System.currentTimeMillis();
            thingsAdapter.updateHealthStatus(device.getEUI(), now, 0/*new frame count*/, "", "");
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(values, device, scriptingAdapter, now, null, null, null);
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
                }
                if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeVirtualState(
            VirtualDevice vd,
            Device device,
            String userID,
            ThingsDataIface thingsAdapter,
            VirtualStackIface virtualStackAdapter,
            ScriptingAdapterIface scriptingAdapter,
            boolean reset,
            Double resetValue
    ) throws ThingsDataException {
        ArrayList<ChannelData> values = new ArrayList<>();
        long now = System.currentTimeMillis();
        VirtualDevice localCopy = virtualStackAdapter.get(vd);
        ChannelData data = new ChannelData();
        data.setDeviceEUI(device.getEUI());
        data.setName("counter");
        //data.setValue(new Double(virtualStackAdapter.get(vd).get()));
        data.setValue(new Double(localCopy.get()));
        data.setTimestamp(now);
        ChannelData data2 = new ChannelData();
        data2.setDeviceEUI(device.getEUI());
        data2.setName("incoming");
        //data2.setValue(new Double(virtualStackAdapter.get(vd).getInputs()));
        data2.setValue(new Double(localCopy.getInputs()));
        data2.setTimestamp(now);
        ChannelData data3 = new ChannelData();
        data3.setDeviceEUI(device.getEUI());
        data3.setName("exiting");
        //data3.setValue(new Double(virtualStackAdapter.get(vd).getOutputs()));
        data3.setValue(new Double(localCopy.getOutputs()));
        data3.setTimestamp(now);
        values.add(data);
        values.add(data2);
        values.add(data3);
        if (reset) {
            ChannelData data4 = new ChannelData();
            data4.setDeviceEUI(device.getEUI());
            data4.setName("reset");
            data4.setValue(resetValue);
            data4.setTimestamp(now);
            values.add(data4);
            //thingsAdapter.putData(userID, device.getEUI(), data4.getName(), data4);
        }
        //TODO: w putData, dla urządzeń virtual, trzeba wywołać skrypt preprocessora
        if (thingsAdapter.isAuthorized(userID, device.getEUI())) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        ArrayList<ArrayList> outputList;
        try {
            Object[] processingResult = DataProcessor.processValues((ArrayList) values, device,
                    scriptingAdapter, data.getTimestamp(), null, null, null);
            outputList = (ArrayList<ArrayList>) processingResult[0];
            for (int i = 0; i < outputList.size(); i++) {
                thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(), fixValues(device, outputList.get(i)));
            }
            if (device.getState().compareTo((Double) processingResult[1]) != 0) {
                System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
            }
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
        }
        //thingsAdapter.putVirtualData(userID, device, scriptingAdapter, values);
        //TODO: use DataProcessor
    }

    private IotData2 parseIotData(String dataStr) {
        IotData2 data = new IotData2();
        data.dev_eui = null;
        data.timestamp = "" + System.currentTimeMillis();
        data.payload_fields = new ArrayList<>();
        String[] params = dataStr.split("&");
        String[] pair;
        HashMap<String, String> map;
        for (int i = 0; i < params.length; i++) {
            pair = params[i].split("=");
            if (pair.length < 2) {
                continue;
            }
            if ("eui".equalsIgnoreCase(pair[0])) {
                data.dev_eui = pair[1];
            } else if ("timestamp".equalsIgnoreCase(pair[0])) {
                data.timestamp = pair[1];
            } else {
                map = new HashMap<>();
                map.put("name", pair[0]);
                map.put("value", pair[1]);
                data.payload_fields.add(map);
            }
        }
        if (null == data.dev_eui || data.payload_fields.isEmpty()) {
            return null;
        }
        if (null == data.timestamp) {
            data.timestamp = "" + System.currentTimeMillis();
        }
        data.normalize();
        return data;
    }

    private IotData2 parseIotData(Map<String, Object> parameters) {
        IotData2 data = new IotData2();
        data.dev_eui = null;
        data.timestamp = "" + System.currentTimeMillis();
        data.payload_fields = new ArrayList<>();
        HashMap<String, String> map;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if ("eui".equalsIgnoreCase(key)) {
                data.dev_eui = value;
                System.out.println("dev_eui:" + data.dev_eui);
            } else if ("timestamp".equalsIgnoreCase(key)) {
                data.timestamp = value;
            } else {
                map = new HashMap<>();
                map.put("name", key);
                map.put("value", value);
                data.payload_fields.add(map);
                System.out.println(key + ":" + value);
            }
            System.out.println("timestamp:" + data.timestamp);
        }
        if (null == data.dev_eui || data.payload_fields.isEmpty()) {
            System.out.println("ERROR: " + data.dev_eui + "," + data.payload_fields);
            return null;
        }
        data.normalize();
        return data;
    }

    private void fireEvent(int source, Device device, String message) {
        fireEvent(source, device.getUserID(), device.getEUI(), message);
    }

    private void fireEvent(int source, String userID, String deviceEUI, String message) {
        IotEvent ev = new IotEvent();
        ev.setOrigin(userID + "\t" + deviceEUI);
        if (source == 1) {
            ev.setPayload("Decoder script (0): " + message);
        } else {
            ev.setPayload("Data processor script (0): " + message);
        }
        ev.setType(IotEvent.GENERAL);
        Kernel.getInstance().dispatchEvent(ev);
    }
}
