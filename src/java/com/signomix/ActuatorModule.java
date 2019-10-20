/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.signomix.in.http.ActuatorApi;
import com.signomix.iot.IotEvent;
import com.signomix.iot.TtnDownlinkMessage;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.iot.ChannelData;
import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.iot.VirtualDevice;
import com.signomix.out.iot.VirtualStackIface;
import com.signomix.out.script.ScriptingAdapterIface;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ActuatorModule {

    private static ActuatorModule logic;

    public static ActuatorModule getInstance() {
        if (logic == null) {
            logic = new ActuatorModule();
        }
        return logic;
    }

    public Object processRequest(
            Event event,
            ActuatorApi actuatorApi,
            ThingsDataIface thingsAdapter,
            ActuatorCommandsDBIface actuatorCommandsDB,
            VirtualStackIface virtualStack,
            ScriptingAdapterIface scriptingAdapter
    ) {
        RequestObject request = event.getRequest();

        StandardResult result = new StandardResult();
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        String userID = request.headers.getFirst("X-user-id");
        Device device;
        boolean hexPayload = false;
        String eui = request.pathExt;
        if (null != eui) {
            if (eui.endsWith("/hex")) {
                eui = eui.substring(0, eui.length() - 4);
                hexPayload = true;
            }
            eui = eui.toUpperCase();
        }
        try {
            device = thingsAdapter.getDevice(userID, eui, false);
        } catch (ThingsDataException ex) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(ex.getMessage());
            return result;
        }
        if (device == null) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        switch (request.method.toUpperCase()) {
            case "GET":
                result = processGet(device.getEUI(), actuatorCommandsDB);
                break;
            case "POST":
                result = processPost(device, userID, request, hexPayload, actuatorCommandsDB, virtualStack, thingsAdapter, scriptingAdapter);
                break;
            default:
                result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
        }
        return result;
    }

    private StandardResult processGet(String deviceEUI, ActuatorCommandsDBIface actuatorCommandsDB) {
        StandardResult result = new StandardResult();
        Event pattern = new Event();
        pattern.setOrigin(deviceEUI);
        try {
            result.setData(actuatorCommandsDB.getFirstCommand(deviceEUI));
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logSevere(this, ex.getMessage()));
        }
        return result;
    }

    private StandardResult processPost(
            Device device,
            String userID,
            RequestObject request,
            boolean hexPayload,
            ActuatorCommandsDBIface actuatorCommandsDB,
            VirtualStackIface virtualStack,
            ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter) {
        StandardResult result = new StandardResult();

        // dla virtual device obsługa powinna być inna
        //TODO: channel name other than "counter"?
        /*
        if (device.getType().equals(Device.VIRTUAL)) {
            try {
                JsonObject o = (JsonObject) JsonReader.jsonToJava(request.body);
                String newValue = (String) o.get("counter");
                VirtualDevice vd = new VirtualDevice(device.getEUI());
                Double value;
                value = Double.parseDouble(newValue); //to obcina część ułamkową.
                virtualStack.get(vd).resetAndSet(value.longValue());
                //DeviceManagementModule.getInstance().writeVirtualState(vd, device, userID, thingsAdapter, virtualStack, scriptingAdapter, true, value);
                DeviceIntegrationModule.getInstance().writeVirtualState(vd, device, userID, thingsAdapter, virtualStack, scriptingAdapter, true, value);
            } catch (ThingsDataException | ClassCastException | NumberFormatException | NullPointerException e) {
                //TODO:
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage(e.getMessage());
            }
            //store new channel data for the device
            return result;
        }
         */
        String cmdType = (String) request.parameters.get("hex");
        IotEvent event = new IotEvent();
        event.setOrigin(device.getEUI());
        if (hexPayload) {
            event.setType(IotEvent.ACTUATOR_HEXCMD);
        } else {
            event.setType(IotEvent.ACTUATOR_CMD);
        }
        event.setPayload(request.body.trim());
        Kernel.getInstance().dispatchEvent(event);
        return result;
    }

    public void processCommand(
            Event event,
            boolean hexagonalRepresentation,
            ActuatorCommandsDBIface actuatorCommandsDB,
            VirtualStackIface virtualStack,
            ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter
    ) {
        String deviceEUI;
        deviceEUI = event.getOrigin();
        String payload = (String) event.getPayload();
        boolean done = false;
        Device device = null;
        try {
            device = thingsAdapter.getDevice(deviceEUI);
            if (device == null) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, "device " + deviceEUI + " not found"));
                return;
            }
            if (device.getType().equals(Device.VIRTUAL)) {
                done = sendToVirtual(device, payload, virtualStack, thingsAdapter, scriptingAdapter);
            } else if (device.getType().equals(Device.TTN)) {
                done = sendToTtn(device, payload, hexagonalRepresentation);
            } else if (device.getType().equals(Device.LORA)) {
                done = true;
            } else if (device.getType().equals(Device.GENERIC) || device.getType().equals(Device.GATEWAY)) {
                // Nothing to do. Command will be included in the response for the next device data transfer.
            }
            //IotEvent commandEvent = new IotEvent(IotEvent.ACTUATOR_CMD, deviceEUI, payload);
            //commandEvent.setId(event.getId());
            //commandEvent.setCreatedAt(event.getCreatedAt());
            if (done) {
                actuatorCommandsDB.putCommandLog(event.getOrigin(), event);
            } else {
                actuatorCommandsDB.putDeviceCommand(event.getOrigin(), event);
            }
            Kernel.getInstance().dispatchEvent(Event.logInfo(this, "processCommand " + event.getOrigin() + ":" + event.getPayload()));
        } catch (ThingsDataException e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
        }

    }

    private boolean sendToVirtual(Device device, String payload, VirtualStackIface virtualStack, ThingsDataIface thingsAdapter, ScriptingAdapterIface scriptingAdapter) {
        VirtualDevice vd = virtualStack.get(new VirtualDevice(device.getEUI()));
        if (vd == null) {
            Kernel.getLogger().log(Event.logWarning(this, "virtual object for device " + device.getEUI() + "not initialized"));
            return false;
        }
        JsonObject o = (JsonObject) JsonReader.jsonToJava(payload);
        String channelName = (String) o.get("channel");
        String newValue = (String) o.get("value");
        String processName = "";
        try {
            if (channelName != null) {
                ChannelData data = new ChannelData();
                data.setDeviceEUI(device.getEUI());
                data.setName(channelName);
                data.setValue(new Double(virtualStack.get(vd).get()));
                data.setTimestamp(System.currentTimeMillis());
                thingsAdapter.putData(device.getUserID(), device.getEUI(), processName, data.getName(), data);
            } else {
                Double value;
                value = Double.parseDouble(newValue); //to obcina część ułamkową.
                virtualStack.get(vd).resetAndSet(value.longValue());
                //DeviceManagementModule.getInstance().writeVirtualState(vd, device, device.getUserID(), thingsAdapter, virtualStack, scriptingAdapter, true, value);
                DeviceIntegrationModule.getInstance().writeVirtualState(vd, device, device.getUserID(), thingsAdapter, virtualStack, scriptingAdapter, true, value);
            }
            Kernel.handle(Event.logFine(this, "command send to device " + device.getEUI()));
        } catch (ThingsDataException e) {
            Kernel.handle(Event.logWarning(this, e.getMessage()));
        }
        return true;
    }

    private boolean sendToTtn(Device device, String payload, boolean hexRepresentation) {
        return sendToTtn(device, payload, hexRepresentation, null);
    }

    private boolean sendToTtn(Device device, String payload, boolean hexRepresentation, String forceUrl) {
        //payload to jest String będący tablicą bajtów zapisanych w formacie hex
        //odpowiednie encodowanie jest w TtnDownlinkMessage
        String deviceID = device.getDeviceID();
        String downlink;
        if (null == forceUrl) {
            downlink = device.getDownlink();
        } else {
            downlink = forceUrl;
        }
        HashMap<String, Object> args = new HashMap<>();
        args.put(JsonWriter.TYPE, false);
        args.put(JsonWriter.SKIP_NULL_FIELDS, true);
        TtnDownlinkMessage message;
        if (hexRepresentation) {
            message = new TtnDownlinkMessage(deviceID, payload, false, 1);
        } else {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName() + ".sendToTtn()", "plain text commands not implemented"));
            return false;
        }
        /*
        TtnDownlinkMessage message = new TtnDownlinkMessage("aaa", null, false, 1);
        message.addField("on", true);
        message.addField("color", "blue");
         */
        String requestBody = JsonWriter.objectToJson(message, args);
        HttpClient client = new HttpClient();
        Request request = new Request()
                .setUrl(downlink)
                .setMethod("POST").setProperty("Content-Type", "application/json")
                .setData(requestBody);
        Result result = client.send(request);
        return 200 == result.getCode();
    }

    public Event getCommand(String deviceEUI, ActuatorCommandsDBIface actuatorCommandsDB) {
        //String result = "";
        Event result = null;
        if (deviceEUI != null) {
            try {
                result = (Event) actuatorCommandsDB.getFirstCommand(deviceEUI);
                //if (null != commandEvent) {
                //    result = (String) commandEvent.getPayload();
                //}
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
        }
        return result;
    }

    public String archiveCommand(Event command, ActuatorCommandsDBIface actuatorCommandsDB) {
        String result = "";
        if (command != null) {
            try {
                actuatorCommandsDB.removeCommand(command.getId());
                actuatorCommandsDB.putCommandLog(command.getOrigin(), command);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
        }
        return result;
    }

}
