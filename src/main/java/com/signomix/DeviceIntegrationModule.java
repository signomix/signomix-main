/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cedarsoftware.util.io.JsonReader;
import com.signomix.event.IotEvent;
import com.signomix.in.http.IntegrationApi;
import com.signomix.in.http.KpnApi;
import com.signomix.in.http.LoRaApi;
import com.signomix.in.http.TtnApi;
import com.signomix.iot.IotData;
import com.signomix.iot.chirpstack.uplink.Uplink;
import com.signomix.iot.generic.IotData2;
import com.signomix.iot.kpn.KPNData;
import com.signomix.iot.lora.LoRaData;
import com.signomix.iot.ttn3.Decoder;
import com.signomix.iot.ttn3.TtnData;
import com.signomix.iot.ttn3.TtnData3;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.script.ScriptingAdapterIface;
import com.signomix.util.HexTool;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DeviceIntegrationModule {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DeviceIntegrationModule.class);

    private static DeviceIntegrationModule logic;

    public static DeviceIntegrationModule getInstance() {
        if (logic == null) {
            logic = new DeviceIntegrationModule();
        }
        return logic;
    }

    public Object processLoRaRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, LoRaApi loraApi) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = request.headers.getFirst("Authorization");
            if (authKey == null || authKey.isEmpty()) {
                Kernel.getInstance()
                        .dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
                if (debugMode) {
                    result.setCode(HttpAdapter.SC_UNAUTHORIZED);
                    result.setData("Authorization header not found");
                }
                return result;
            }
            String jsonString = request.body;

            jsonString = "{\"@type\":\"com.signomix.iot.lora.LoRaData\","
                    + jsonString.substring(jsonString.indexOf("{") + 1);
            LoRaData data = null;
            try {

                data = (LoRaData) JsonReader.jsonToJava(jsonString);
                data.normalize();
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(),
                        "deserialization problem: incompatible format " + jsonString));
                e.printStackTrace();
            }
            if (data == null) {
                // TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }

            // save value and timestamp in device's channel witch name is the same as the
            // field name
            boolean isRegistered = false;
            Device device;
            try {
                // device = thingsAdapter.getDeviceChecked(data.getUserId(),
                // data.getDeviceId());
                device = thingsAdapter.getDevice(data.getDevEUI());
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDevEUI() + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if ("VIRTUAL".equals(device.getType()) || device.getType().startsWith("TTN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDevEUI() + " type is not valid"));
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

            // String secret = device.getKey();
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
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            // after successful authorization
            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            // check frame counter
            if (device.isCheckFrames()) {
                if (device.getLastFrame() == data.getfCnt()) {
                    // drop request
                    Kernel.getInstance().dispatchEvent(
                            Event.logWarning(this, "duplicated frame " + data.getfCnt() + " for " + device.getEUI()));
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData("OK");
                    return result;
                }
            }
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), data.getfCnt(), "", "");
            ArrayList<ChannelData> inputList = prepareLoRaValues(data, scriptingAdapter, device.getEncoderUnescaped(),
                    device.getEUI(), device.getUserID());

            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList, device, scriptingAdapter,
                        data.getReceivedPackageTimestamp(), data.getLatitude(),
                        data.getLongitude(), data.getAltitude(), "", "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processLoraRequest()", e.getMessage()));
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
    /*
     * public Object processIotRequest(Event event, ThingsDataIface thingsAdapter,
     * UserAdapterIface userAdapter, ScriptingAdapterIface scriptingAdapter,
     * IntegrationApi iotApi, ActuatorCommandsDBIface actuatorCommandsDB) {
     * RequestObject request = event.getRequest();
     * boolean htmlClient = false;
     * 
     * StandardResult result = new StandardResult();
     * result.setCode(HttpAdapter.SC_CREATED);
     * result.setData("");
     * result.setHeader("Content-type", "text/plain");
     * 
     * String clientAppTitle = (String)
     * request.parameters.getOrDefault("clienttitle", "");
     * if (!clientAppTitle.isEmpty()) {
     * result.setHeader("Content-type", "text/html");
     * htmlClient = true;
     * }
     * 
     * try {
     * String authKey = request.headers.getFirst("Authorization");
     * 
     * String dataString = request.body;
     * String jsonString;
     * if (dataString == null) {
     * dataString = ((String) request.parameters.getOrDefault("data", "")).trim();
     * }
     * IotData2 data = null;
     * if (dataString.isEmpty()) {
     * data = parseIotData(request.parameters);
     * dataString = buildParamString(request.parameters);
     * } else {
     * jsonString
     * = "{\"@type\":\"com.signomix.iot.IotData2\","
     * + dataString.substring(dataString.indexOf("{") + 1);
     * 
     * try {
     * data = (IotData2) JsonReader.jsonToJava(jsonString);
     * data.normalize();
     * } catch (Exception e) {
     * data = parseIotData(dataString);
     * if (null == data) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), "deserialization problem from " + request.clientIp + " " +
     * jsonString));
     * //e.printStackTrace();
     * }
     * }
     * }
     * 
     * if (data == null) {
     * //TODO: send warning to the service admin about deserialization error
     * result.setCode(HttpAdapter.SC_BAD_REQUEST);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Deserialization problem, the data format is not compatible with Signomix integration API"
     * ).getBytes());
     * return result;
     * }
     * 
     * // authorization can be send as request parameter "authkey"
     * if (authKey == null || authKey.isEmpty()) {
     * authKey = data.getAuthKey();
     * }
     * if (authKey == null || authKey.isEmpty()) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), "Authorization is required"));
     * result.setCode(HttpAdapter.SC_UNAUTHORIZED);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Not authorized").getBytes());
     * return result;
     * }
     * 
     * // save value and timestamp in device's channel witch name is the same as the
     * field name
     * boolean isRegistered = false;
     * Device device;
     * Device gateway;
     * try {
     * device = thingsAdapter.getDevice(data.dev_eui);
     * gateway = thingsAdapter.getDevice(data.gateway_eui);
     * isRegistered = (null != device);
     * if (!isRegistered) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), "Device " + data.dev_eui + " is not registered"));
     * result.setCode(HttpAdapter.SC_NOT_FOUND);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Device not found").getBytes());
     * return result;
     * }
     * if ("VIRTUAL".equals(device.getType()) || device.getType().startsWith("TTN"))
     * {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), "Device " + data.dev_eui + " type is not valid"));
     * result.setCode(HttpAdapter.SC_BAD_REQUEST);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Invalid device type").getBytes());
     * return result;
     * }
     * 
     * } catch (ThingsDataException ex) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), ex.getMessage()));
     * result.setCode(HttpAdapter.SC_NOT_FOUND);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Device not found").getBytes());
     * return result;
     * } catch (Exception e) {
     * e.printStackTrace();
     * Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().
     * getSimpleName(), e.getMessage()));
     * result.setCode(HttpAdapter.SC_BAD_REQUEST);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Bad request").getBytes());
     * return result;
     * }
     * 
     * //TODO: jeśli w danych przyszła informacja nt. gateway'a, to weryfikujemy
     * secret key gatewaya, a nie device
     * String secret;
     * if (gateway == null) {
     * secret = device.getKey();
     * } else {
     * secret = gateway.getKey();
     * }
     * String applicationSecret;
     * boolean authorized = false;
     * 
     * if (secret != null && !secret.isEmpty()) {
     * authorized = authKey.equals(secret);
     * }
     * if (!authorized) { //TODO: remove
     * try {
     * User user = userAdapter.get(data.getApplicationID());
     * if (user != null) {
     * applicationSecret = user.getConfirmString();
     * authorized = authKey.equals(applicationSecret);
     * }
     * } catch (UserException ex) {
     * } catch (Exception ex) {
     * }
     * }
     * if (!authorized) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName(), "Data request from device " + device.getEUI() +
     * " not authorized"));
     * result.setCode(HttpAdapter.SC_FORBIDDEN);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Not authorized").getBytes());
     * return result;
     * }
     * 
     * if (!device.isActive()) {
     * result.setCode(HttpAdapter.SC_UNAVAILABLE);
     * result.setPayload(buildResultData(htmlClient, false, clientAppTitle,
     * "Device is inactive").getBytes());
     * return result;
     * }
     * 
     * // auth method stop
     * //after successful authorization
     * thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(),
     * -1, "", "");
     * 
     * ArrayList<ChannelData> inputList = prepareIotValues(data);
     * ArrayList<ArrayList> outputList;
     * try {
     * Object[] processingResult
     * = DataProcessor.processValues(inputList, device, scriptingAdapter,
     * data.getReceivedPackageTimestamp(), data.getLatitude(),
     * data.getLongitude(), data.getAltitude(), dataString, "");
     * outputList = (ArrayList<ArrayList>) processingResult[0];
     * for (int i = 0; i < outputList.size(); i++) {
     * thingsAdapter.putData(device.getUserID(), device.getEUI(),
     * device.getProject(), device.getState(), fixValues(device,
     * outputList.get(i)));
     * }
     * if (device.getState().compareTo((Double) processingResult[1]) != 0) {
     * System.out.println("DEVICE STATE " + device.getState() + " " + (Double)
     * processingResult[1]);
     * thingsAdapter.updateDeviceState(device.getEUI(), (Double)
     * processingResult[1]);
     * }
     * } catch (Exception e) {
     * Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().
     * getSimpleName() + ".processIotRequest()", e.getMessage()));
     * fireEvent(2, device, e.getMessage());
     * }
     * if (Device.GENERIC.equals(device.getType()) ||
     * Device.GATEWAY.equals(device.getType())) {
     * Event command = ActuatorModule.getInstance().getCommand(device.getEUI(),
     * actuatorCommandsDB);
     * if (null != command) {
     * String commandPayload = (String) command.getPayload();
     * System.out.println("EVENT CATEGORY TYPE:" + command.getCategory() + " " +
     * command.getType());
     * if (IotEvent.ACTUATOR_HEXCMD.equals(command.getType())) {
     * String rawCmd = new
     * String(Base64.getEncoder().encode(HexTool.hexStringToByteArray(commandPayload
     * )));
     * result.setPayload(rawCmd.getBytes());
     * //TODO: odpowiedź jeśli dane z formularza
     * } else {
     * result.setPayload(commandPayload.getBytes());
     * //TODO: odpowiedź jeśli dane z formularza
     * }
     * ActuatorModule.getInstance().archiveCommand(command, actuatorCommandsDB);
     * }
     * }
     * if (htmlClient) {
     * result.setCode(HttpAdapter.SC_OK);
     * result.setPayload(buildResultData(htmlClient, true, clientAppTitle,
     * "Data saved.").getBytes());
     * }
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * return result;
     * }
     */
    /**
     *
     */
    public Object processTtnRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, TtnApi ttnApi) {
        // TODO: Authorization
        RequestObject request = event.getRequest();
        boolean authorizationRequired = !("false".equalsIgnoreCase(ttnApi.getProperty("authorization-required")));
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        String authKey = request.headers.getFirst("Authorization");

        if (authorizationRequired && (authKey == null || authKey.isEmpty())) {
            Kernel.getInstance()
                    .dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
            return result;
        }

        String jsonString = request.body;
        jsonString = "{\"@type\":\"com.signomix.iot.TtnData\","
                + jsonString.substring(jsonString.indexOf("{") + 1);
        TtnData data = null;
        try {

            data = (TtnData) JsonReader.jsonToJava(jsonString);
            data.normalize();
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(),
                    "deserialization problem: incompatible format " + jsonString));
            e.printStackTrace();
            // TODO: send warning to the service admin about deserialization error
        }
        if (data == null) {
            // we don't send error code to TTN
            return result;
        }

        Device device;
        try {
            device = thingsAdapter.getDevice(data.getDeviceEUI());
            if (null == device) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Device " + data.getDeviceEUI() + " is not registered"));
                return result;
            }
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        if (authorizationRequired) {
            try {
                if (!authKey.equals(device.getKey())) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Authorization key don't match for " + device.getEUI()));
                    return result;
                }
            } catch (Exception ex) { // catch (UserException ex) {
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
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Device " + data.getDeviceEUI() + " type is not valid"));
                return result;
            }
            if (device.isCheckFrames()) {
                if (device.getLastFrame() == data.getFrameCounter()) {
                    // drop request
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this,
                            "duplicated frame " + data.getFrameCounter() + " for " + device.getEUI()));
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData("OK");
                    return result;
                }
            }
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), data.getFrameCounter(),
                    data.getDownlink(), data.getDeviceID());

        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        try {
            ArrayList<ChannelData> inputList = prepareTtnValues(data, scriptingAdapter, device.getEncoderUnescaped(),
                    device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList,
                        device,
                        scriptingAdapter,
                        data.getReceivedPackageTimestamp(),
                        data.getLatitude(),
                        data.getLongitude(),
                        data.getAltitude(),
                        request.body, "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processTtnRequest()", e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processTtn3Request(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, TtnApi ttnApi) {
        // TODO: Authorization
        RequestObject request = event.getRequest();
        boolean authorizationRequired = !("false".equalsIgnoreCase(ttnApi.getProperty("authorization-required")));
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        String authKey = request.headers.getFirst("Authorization");

        if (authorizationRequired && (authKey == null || authKey.isEmpty())) {
            Kernel.getInstance()
                    .dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
            return result;
        }

        String path = request.pathExt;
        Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "TTN3 path:" + path));
        String jsonString = request.body;
        Decoder decoder = new Decoder();
        TtnData3 data = decoder.decode(jsonString);
        if (data == null) {
            // we don't send error code to TTN
            return result;
        }
        data.normalize();
        Device device;
        try {
            device = thingsAdapter.getDevice(data.getDeviceEUI());
            if (null == device) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Device " + data.getDeviceEUI() + " is not registered"));
                return result;
            }
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        if (authorizationRequired) {
            try {
                if (!authKey.equals(device.getKey())) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Authorization key don't match for " + device.getEUI()));
                    return result;
                }
            } catch (Exception ex) { // catch (UserException ex) {
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
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Device " + data.getDeviceEUI() + " type is not valid"));
                return result;
            }
            if (device.isCheckFrames()) {
                // if (device.getLastFrame() == data.getFrameCounter()) {
                // //drop request
                // Kernel.getInstance().dispatchEvent(Event.logWarning(this, "duplicated frame "
                // + data.getFrameCounter() + " for " + device.getEUI()));
                // result.setCode(HttpAdapter.SC_OK);
                // result.setData("OK");
                // return result;
                // }
            }
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), data.getFrameCounter(),
                    data.getDownlink(), data.getDeviceID());

        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }
        try {
            ArrayList<ChannelData> inputList = prepareTtn3Values(data, scriptingAdapter, device.getEncoderUnescaped(),
                    device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList,
                        device,
                        scriptingAdapter,
                        data.getReceivedPackageTimestamp(),
                        data.getLatitude(),
                        data.getLongitude(),
                        data.getAltitude(),
                        request.body, "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processTtnRequest()", e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Device getDeviceChecked(IotData data, int expectedType, ThingsDataIface thingsAdapter) {
        if (data.isAuthRequired() && (data.getAuthKey() == null || data.getAuthKey().isEmpty())) {
            Kernel.getInstance()
                    .dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "Authorization is required"));
            return null;
        }
        Device device;
        Device gateway;
        try {
            device = thingsAdapter.getDevice(data.getDeviceEUI());
            gateway = thingsAdapter.getDevice(data.getGatewayEUI());
            if (null == device) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Device " + data.getDeviceEUI() + " is not registered"));
                return null;
            }
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return null;
        }
        if (data.isAuthRequired()) {
            String secret;
            if (gateway == null) {
                secret = device.getKey();
            } else {
                secret = gateway.getKey();
            }
            try {
                if (!data.getAuthKey().equals(secret)) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Authorization key don't match for " + device.getEUI()));
                    return null;
                }
            } catch (Exception ex) { // catch (UserException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                return null;
            }
        }
        switch (expectedType) {
            case IotData.GENERIC:
                if (!device.getType().startsWith("GENERIC")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " type is not valid"));
                    return null;
                }
                break;
            case IotData.CHIRPSTACK:
                if (!device.getType().startsWith("LORA")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " type is not valid"));
                    return null;
                }
                break;
            case IotData.TTN:
                if (!device.getType().startsWith("TTN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " type is not valid"));
                    return null;
                }
                break;
            case IotData.KPN:
                if (!device.getType().startsWith("KPN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " type is not valid"));
                    return null;
                }
                break;
        }
        if (!device.isActive()) {
            // return "device is not active";
            return null;
        }
        return device;
    }

    public Object processChirpstackRequest(IotData data, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, TtnApi ttnApi) {
        Uplink uplink = data.getChirpstackData();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");

        Device device = getDeviceChecked(data, IotData.CHIRPSTACK, thingsAdapter);
        if (null == device) {
            // result.setData(authMessage);
            return result;
        }
        try {
            if (device.isCheckFrames()) {
                if (device.getLastFrame() == uplink.getfCnt()) {
                    // drop request
                    Kernel.getInstance().dispatchEvent(
                            Event.logWarning(this, "duplicated frame " + uplink.getfCnt() + " for " + device.getEUI()));
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData("OK");
                    return result;
                }
            }
            String downlink = "";
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), uplink.getfCnt(), downlink,
                    uplink.getDeviceID());
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return result;
        }

        try {
            ArrayList<ChannelData> inputList = prepareChirpstackValues(uplink, scriptingAdapter,
                    device.getEncoderUnescaped(), device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(
                        inputList,
                        device,
                        scriptingAdapter,
                        uplink.getReceivedPackageTimestamp(),
                        uplink.getLatitude(),
                        uplink.getLongitude(),
                        uplink.getAltitude(),
                        data.getSerializedData(),
                        "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processTtnRequest()", e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processGenericRequest(IotData data, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, TtnApi ttnApi, ActuatorCommandsDBIface actuatorCommandsDB) {
        IotData2 iotData = data.getIotData();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean htmlClient = false;
        String clientAppTitle = data.getClientName();
        if (null != clientAppTitle && !clientAppTitle.isEmpty()) {
            result.setHeader("Content-type", "text/html");
            htmlClient = true;
        }
        Device device = getDeviceChecked(data, IotData.GENERIC, thingsAdapter);
        if (null == device) {
            // result.setData(authMessage);
            return result;
        }

        try {
            // after successful authorization
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), -1, "", "");
        } catch (ThingsDataException ex) {
            Logger.getLogger(DeviceIntegrationModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<ChannelData> inputList = decodePayload(iotData, scriptingAdapter, clientAppTitle, clientAppTitle,
                clientAppTitle);
        ArrayList<ArrayList> outputList;
        String dataString = data.getSerializedData();
        try {
            Object[] processingResult = DataProcessor.processValues(inputList, device, scriptingAdapter,
                    iotData.getReceivedPackageTimestamp(), iotData.getLatitude(),
                    iotData.getLongitude(), iotData.getAltitude(), dataString, "");
            outputList = (ArrayList<ArrayList>) processingResult[0];
            for (int i = 0; i < outputList.size(); i++) {
                thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                        fixValues(device, outputList.get(i)));
            }
            if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Kernel.getInstance().dispatchEvent(
                    Event.logWarning(this.getClass().getSimpleName() + ".processGenericRequest()", e.getMessage()));
            fireEvent(2, device, e.getMessage());
        }

        Event command = ActuatorModule.getInstance().getCommand(device.getEUI(), actuatorCommandsDB);
        if (null != command) {
            String commandPayload = (String) command.getPayload();
            System.out.println("EVENT CATEGORY TYPE:" + command.getCategory() + " " + command.getType());
            if (IotEvent.ACTUATOR_HEXCMD.equals(command.getType())) {
                String rawCmd = new String(Base64.getEncoder().encode(HexTool.hexStringToByteArray(commandPayload)));
                result.setPayload(rawCmd.getBytes());
                // TODO: odpowiedź jeśli dane z formularza
            } else {
                result.setPayload(commandPayload.getBytes());
                // TODO: odpowiedź jeśli dane z formularza
            }
            ActuatorModule.getInstance().archiveCommand(command, actuatorCommandsDB);
        }

        if (htmlClient) {
            result.setCode(HttpAdapter.SC_OK);
            result.setPayload(buildResultData(htmlClient, true, clientAppTitle, "Data saved.").getBytes());
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

    public Object processKpnRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, KpnApi kpnApi) {
        // TODO: Authorization
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = null;

            String jsonString = request.body;
            jsonString = "{\"@type\":\"com.signomix.iot.kpn.KPNData\","
                    + jsonString.substring(jsonString.indexOf("{") + 1);
            KPNData data = null;
            try {

                data = (KPNData) JsonReader.jsonToJava(jsonString);
                data.normalize();
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(),
                        "deserialization problem: incompatible format " + jsonString));
                e.printStackTrace();
            }
            if (data == null) {
                // TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }

            // save value and timestamp in device's channel witch name is the same as the
            // field name
            boolean isRegistered = false;
            Device device;
            try {
                // device = thingsAdapter.getDeviceChecked(data.getUserId(),
                // data.getDeviceId());
                device = thingsAdapter.getDevice(data.getDeviceEUI());
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if (!device.getType().equalsIgnoreCase("KPN")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + data.getDeviceEUI() + " type is not valid"));
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

            // String secret = device.getKey();
            String secret = userAdapter.get(device.getUserID()).getConfirmString();
            String applicationSecret;
            boolean authorized = true;

            // TODO: authorization
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            // after successful authorization

            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            // TODO: check frame counter
            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), 0/* new frame count */, "",
                    "");
            ArrayList<ChannelData> inputList = prepareKpnValues(data, scriptingAdapter, device.getEncoderUnescaped(),
                    device.getEUI(), device.getUserID());
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(inputList, device, scriptingAdapter,
                        data.getReceivedPackageTimestamp(), data.getLatitude(),
                        data.getLongitude(), data.getAltitude(), request.body, "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    System.out.println("DEVICE STATE " + device.getState() + " " + (Double) processingResult[1]);
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processKpnRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processRawRequest(Event event, ThingsDataIface thingsAdapter, UserAdapterIface userAdapter,
            ScriptingAdapterIface scriptingAdapter, IntegrationApi rawApi, ActuatorCommandsDBIface actuatorCommandsDB) {
        // TODO: Authorization
        RequestObject request = event.getRequest();
        // TODO: kpnApi

        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_CREATED);
        result.setData("OK");
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        try {
            String authKey = null;
            String deviceEUI;
            // TODO: header should be configurable
            deviceEUI = request.headers.getFirst(rawApi.getProperty("header-name"));

            if (deviceEUI == null) {
                // TODO: send warning to the service admin about deserialization error
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setData("deserialization problem");
                return result;
            }
            deviceEUI = deviceEUI.toUpperCase();

            // save value and timestamp in device's channel witch name is the same as the
            // field name
            boolean isRegistered = false;
            Device device;
            try {
                // device = thingsAdapter.getDeviceChecked(data.getUserId(),
                // data.getDeviceId());
                device = thingsAdapter.getDevice(deviceEUI);
                isRegistered = (null != device);
                if (!isRegistered) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + deviceEUI + " is not registered"));
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    result.setData("device not found");
                    return result;
                }
                if (!device.getType().equalsIgnoreCase("GENERIC")) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                            "Device " + deviceEUI + " type is not valid"));
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

            // String secret = device.getKey();
            String secret = userAdapter.get(device.getUserID()).getConfirmString();
            String applicationSecret;
            boolean authorized = true;

            // TODO: authorization
            if (!authorized) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(),
                        "Data request from device " + device.getEUI() + " not authorized"));
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                result.setData("not authorized");
                return result;
            }
            // after successful authorization
            if (!device.isActive()) {
                result.setCode(HttpAdapter.SC_UNAVAILABLE);
                result.setData("device is not active");
                return result;
            }

            thingsAdapter.updateHealthStatus(device.getEUI(), System.currentTimeMillis(), 0/* new frame count */, "",
                    "");
            ArrayList<ChannelData> finalValues = null;
            try {
                finalValues = DataProcessor.processRawValues(request.body, device, scriptingAdapter,
                        System.currentTimeMillis());
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName() + ".processRawRequest()", e.getMessage()));
                fireEvent(2, device, e.getMessage());
            }
            thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                    fixValues(device, finalValues));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<ChannelData> prepareTtnValues(TtnData data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        ArrayList<ChannelData> values = new ArrayList<>();
        if (data.getPayloadFieldNames() == null || data.getPayloadFieldNames().length == 0) {
            if (null != data.getPayload()) {
                byte[] decodedPayload = Base64.getDecoder().decode(data.getPayload().getBytes());
                try {
                    values = scriptingAdapter.decodeData(decodedPayload, encoderCode, deviceID, data.getTimestamp(),
                            userID);
                } catch (Exception e) {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(
                                    this.getClass().getSimpleName() + ".prepareTtnValues for device " + deviceID,
                                    e.getMessage()));
                    fireEvent(1, deviceID, userID, e.getMessage());
                    return null;
                }
            }
        } else {
            TtnData processedData = new TtnData(data);
            // handling Cayenne LPP
            ArrayList<String> toExpand = new ArrayList<>();
            Iterator it = data.getPayloadFields().keySet().iterator();
            Object payloadField;
            String fieldName;
            while (it.hasNext()) {
                fieldName = (String) it.next();
                payloadField = data.getPayloadFields().get(fieldName);
                if (payloadField instanceof com.cedarsoftware.util.io.JsonObject) {
                    toExpand.add(fieldName);
                } else {
                    // nothing to do
                }
            }
            toExpand.forEach(name -> {
                com.cedarsoftware.util.io.JsonObject j = (com.cedarsoftware.util.io.JsonObject) data.getPayloadFields()
                        .get(name);
                Iterator it2 = j.keySet().iterator();
                String key;
                while (it2.hasNext()) {
                    key = (String) it2.next();
                    processedData.putField(name + "_" + key, j.get(key));
                }
            });
            toExpand.forEach(name -> {
                processedData.removeField(name);
            });
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

    private ArrayList<ChannelData> prepareTtn3Values(TtnData3 data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        ArrayList<ChannelData> values = new ArrayList<>();
        if (data.getPayloadFieldNames() == null || data.getPayloadFieldNames().length == 0) {
            if (null != data.getPayload()) {
                byte[] decodedPayload = Base64.getDecoder().decode(data.getPayload().getBytes());
                try {
                    values = scriptingAdapter.decodeData(decodedPayload, encoderCode, deviceID, data.getTimestamp(),
                            userID);
                } catch (Exception e) {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(
                                    this.getClass().getSimpleName() + ".prepareTtnValues for device " + deviceID,
                                    e.getMessage()));
                    fireEvent(1, deviceID, userID, e.getMessage());
                    return null;
                }
            }
        } else {
            TtnData processedData = data;
            // handling Cayenne LPP
            ArrayList<String> toExpand = new ArrayList<>();
            Iterator it = data.getPayloadFields().keySet().iterator();
            Object payloadField;
            String fieldName;
            while (it.hasNext()) {
                fieldName = (String) it.next();
                payloadField = data.getPayloadFields().get(fieldName);
                if (payloadField instanceof com.cedarsoftware.util.io.JsonObject) {
                    toExpand.add(fieldName);
                } else {
                    // nothing to do
                }
            }
            toExpand.forEach(name -> {
                com.cedarsoftware.util.io.JsonObject j = (com.cedarsoftware.util.io.JsonObject) data.getPayloadFields()
                        .get(name);
                Iterator it2 = j.keySet().iterator();
                String key;
                while (it2.hasNext()) {
                    key = (String) it2.next();
                    processedData.putField(name + "_" + key, j.get(key));
                }
            });
            toExpand.forEach(name -> {
                processedData.removeField(name);
            });
            // Cayenne LPP - end
            Kernel.getInstance().dispatchEvent(
                    Event.logInfo(this.getClass().getSimpleName() + ".prepareTtn3Values for device " + deviceID,
                            "payloadfieldnames size:" + processedData.getPayloadFieldNames().length));
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

    private ArrayList<ChannelData> prepareChirpstackValues(Uplink data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        ArrayList<ChannelData> values = new ArrayList<>();
        if (data.getPayloadFieldNames() == null || data.getPayloadFieldNames().length == 0) {
            if (null != data.getPayload()) {
                byte[] decodedPayload = Base64.getDecoder().decode(data.getPayload().getBytes());
                try {
                    values = scriptingAdapter.decodeData(decodedPayload, encoderCode, deviceID, data.getTimestamp(),
                            userID);
                } catch (Exception e) {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(
                                    this.getClass().getSimpleName() + ".prepareTtnValues for device " + deviceID,
                                    e.getMessage()));
                    fireEvent(1, deviceID, userID, e.getMessage());
                    return null;
                }
            }
        } else {
            for (String payloadFieldName : data.getPayloadFieldNames()) {
                ChannelData mval = new ChannelData();
                mval.setDeviceEUI(data.getDeviceEUI());
                mval.setName(payloadFieldName.toLowerCase());
                mval.setValue(data.getDoubleValue(payloadFieldName));
                mval.setStringValue(data.getStringValue(payloadFieldName));
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

    private ArrayList<ChannelData> decodePayload(IotData2 data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        if (!data.getDataList().isEmpty()) {
            return data.getDataList();
        }
        ArrayList<ChannelData> values = new ArrayList<>();
        if (data.getPayloadFieldNames() == null || data.getPayloadFieldNames().length == 0) {
            byte[] decodedPayload = {};
            if (null != data.getPayload()) {
                decodedPayload = Base64.getDecoder().decode(data.getPayload().getBytes());
            } else if (null != data.getHexPayload()) {
                decodedPayload = hexStringToByteArray(data.getHexPayload());
            }
            if (decodedPayload.length > 0) {
                try {
                    values = scriptingAdapter.decodeData(decodedPayload, encoderCode, deviceID, data.getTimestamp(),
                            userID);
                } catch (Exception e) {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(
                                    this.getClass().getSimpleName() + ".prepareTtnValues for device " + deviceID,
                                    e.getMessage()));
                    fireEvent(1, deviceID, userID, e.getMessage());
                    return null;
                }
            }
        }
        return values;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private ArrayList<ChannelData> prepareLoRaValues(LoRaData data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        byte[] encodedPayload = Base64.getDecoder().decode(data.getData().getBytes());
        // przekształcamy tablicę bajtów na listę obiektów ChannelData
        ArrayList<ChannelData> values;
        try {
            values = scriptingAdapter.decodeData(encodedPayload, encoderCode, deviceID, data.getTimestamp(), userID);
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(
                    this.getClass().getSimpleName() + ".prepareLoRaValues for device " + deviceID, e.getMessage()));
            fireEvent(1, deviceID, userID, e.getMessage());
            return null;
        }
        return values;
    }

    private ArrayList<ChannelData> prepareKpnValues(KPNData data, ScriptingAdapterIface scriptingAdapter,
            String encoderCode, String deviceID, String userID) {
        // przekształcamy hexadecimal payload na listę obiektów ChannelData
        ArrayList<ChannelData> values;
        try {
            values = scriptingAdapter.decodeHexData(data.getPayload(), encoderCode, deviceID, data.getTimestamp(),
                    userID);
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(
                    this.getClass().getSimpleName() + ".prepareLoRaValues for device " + deviceID, e.getMessage()));
            fireEvent(1, deviceID, userID, e.getMessage());
            return null;
        }
        return values;
    }

    public void writeVirtualData(ThingsDataIface thingsAdapter, ScriptingAdapterIface scriptingAdapter, Device device,
            ArrayList<ChannelData> values) {
        try {
            long now = System.currentTimeMillis();
            if (!device.getEUI()
                    .equalsIgnoreCase((String) Kernel.getInstance().getProperties().get("monitoring_device"))) {
                logger.debug("virtual data to {} {}", device.getEUI(),
                        (String) Kernel.getInstance().getProperties().get("monitoring_device"));
                thingsAdapter.updateHealthStatus(device.getEUI(), now, 0/* new frame count */, "", "");
            }
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(values, device, scriptingAdapter,
                        now, device.getLatitude(), device.getLongitude(), device.getAltitude(), "", "");
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
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

    /*
     * private IotData2 parseIotData(String dataStr) {
     * IotData2 data = new IotData2();
     * data.dev_eui = null;
     * data.timestamp = "" + System.currentTimeMillis();
     * data.payload_fields = new ArrayList<>();
     * String[] params = dataStr.split("&");
     * String[] pair;
     * HashMap<String, String> map;
     * for (int i = 0; i < params.length; i++) {
     * pair = params[i].split("=");
     * if (pair.length < 2) {
     * continue;
     * }
     * if ("eui".equalsIgnoreCase(pair[0])) {
     * data.dev_eui = pair[1];
     * } else if ("timestamp".equalsIgnoreCase(pair[0])) {
     * data.timestamp = pair[1];
     * } else if ("authkey".equalsIgnoreCase(pair[0])) {
     * data.authKey = pair[1];
     * } else {
     * map = new HashMap<>();
     * map.put("name", pair[0]);
     * map.put("value", pair[1]);
     * data.payload_fields.add(map);
     * }
     * }
     * if (null == data.dev_eui || data.payload_fields.isEmpty()) {
     * return null;
     * }
     * if (null == data.timestamp) {
     * data.timestamp = "" + System.currentTimeMillis();
     * }
     * data.normalize();
     * return data;
     * }
     * 
     * private String buildParamString(Map<String, Object> parameters) {
     * StringBuilder result = new StringBuilder();
     * for (Map.Entry<String, Object> entry : parameters.entrySet()) {
     * String key = entry.getKey();
     * String value = (String) entry.getValue();
     * result.append(entry.getKey()).append("=").append((String)
     * entry.getValue()).append("\r\n");
     * }
     * return result.toString();
     * }
     * 
     * private IotData2 parseIotData(Map<String, Object> parameters) {
     * IotData2 data = new IotData2();
     * data.dev_eui = null;
     * data.timestamp = "" + System.currentTimeMillis();
     * data.payload_fields = new ArrayList<>();
     * HashMap<String, String> map;
     * for (Map.Entry<String, Object> entry : parameters.entrySet()) {
     * String key = entry.getKey();
     * String value = (String) entry.getValue();
     * if ("eui".equalsIgnoreCase(key)) {
     * data.dev_eui = value;
     * System.out.println("dev_eui:" + data.dev_eui);
     * } else if ("timestamp".equalsIgnoreCase(key)) {
     * data.timestamp = value;
     * } else if ("authkey".equalsIgnoreCase(key)) {
     * data.authKey = value;
     * //} else if ("callbackurl".equalsIgnoreCase(key)) {
     * // data.callbackurl = value;
     * } else {
     * map = new HashMap<>();
     * map.put("name", key);
     * map.put("value", value);
     * data.payload_fields.add(map);
     * System.out.println(key + ":" + value);
     * }
     * System.out.println("timestamp:" + data.timestamp);
     * }
     * if (null == data.dev_eui || data.payload_fields.isEmpty()) {
     * System.out.println("ERROR: " + data.dev_eui + "," + data.payload_fields);
     * return null;
     * }
     * data.normalize();
     * return data;
     * }
     */
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

    String buildResultData(boolean html, boolean isSuccess, String title, String text) {
        if (!html) {
            return text;
        }
        String err = isSuccess ? "" : "ERROR<br>";
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='text-align: center;'><h1>")
                .append(title)
                .append("</h1><p>")
                .append(err)
                .append(text)
                .append("</p><button type='button' onclick='window.history.go(-1); return false;'>")
                .append("OK")
                .append("</button></body></html>");
        return sb.toString();
    }

}
