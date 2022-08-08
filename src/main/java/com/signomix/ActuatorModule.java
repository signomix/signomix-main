/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.cedarsoftware.util.io.*;
import com.signomix.in.http.ActuatorApi;
import com.signomix.common.iot.ChannelData;
import com.signomix.common.iot.Device;
import com.signomix.event.IotEvent;
import com.signomix.iot.TtnDownlinkMessage;
import com.signomix.out.db.ActuatorCommandsDBIface;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;

import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.notification.CommandWebHookIface;
import com.signomix.out.script.ScriptingAdapterIface;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.AdapterException;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ActuatorModule {
    public static int PLAIN_COMMAND = 0;
    public static int HEX_COMMAND = 1;
    public static int JSON_COMMAND = 2;

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
            ScriptingAdapterIface scriptingAdapter) {
        RequestObject request = event.getRequest();

        StandardResult result = new StandardResult();
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        String userID = request.headers.getFirst("X-user-id");
        Device device;
        boolean hexPayload = false;
        int type = 2;
        String eui = request.pathExt;
        if (null != eui) {
            if (eui.endsWith("/hex")) {
                eui = eui.substring(0, eui.length() - 4);
                hexPayload = true;
                type = 1;
            } else if (eui.endsWith("/plain")) {
                eui = eui.substring(0, eui.length() - 6);
                hexPayload = true;
                type = 0;
            }
            eui = eui.toUpperCase();
        }
        try {
            device = thingsAdapter.getDevice(userID, -1, eui, false);
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
                result = processPost(device, userID, request, type, actuatorCommandsDB, thingsAdapter,
                        scriptingAdapter);
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
            int type,
            ActuatorCommandsDBIface actuatorCommandsDB,
            ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter) {
        StandardResult result = new StandardResult();

        IotEvent event = new IotEvent();
        event.setOrigin("@" + device.getEUI());
        switch (type) {
            case 1:
                event.setType(IotEvent.ACTUATOR_HEXCMD);
                break;
            case 0:
                event.setType(IotEvent.ACTUATOR_PLAINCMD);
                break;
            default:
                event.setType(IotEvent.ACTUATOR_CMD);
        }
        /*
         * leading "#" - overwrite previous command if still not send,
         * leading "&" - send command after previously registered
         */
        event.setPayload("#" + request.body.trim());
        Kernel.getInstance().dispatchEvent(event);
        return result;
    }

    public void processCommand(
            Event event,
            // boolean hexagonalRepresentation,
            int type,
            ActuatorCommandsDBIface actuatorCommandsDB,
            ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter) {
        String[] devices = event.getOrigin().split("@");
        String sourceEUI = devices[0];
        String deviceEUI = null;
        if (devices.length > 1) {
            deviceEUI = devices[1];
        }
        String payload = (String) event.getPayload();
        boolean done = false;
        Device sourceDevice = null;
        Device device = null;
        try {
            if (!sourceEUI.isEmpty()) {
                sourceDevice = thingsAdapter.getDevice(sourceEUI);
            }
            device = thingsAdapter.getDevice(deviceEUI);
            if (device == null) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, "device " + deviceEUI + " not found"));
                return;
            }
            if (device.getType().equals(Device.VIRTUAL)) {
                if (null != sourceDevice && !sourceDevice.getType().equals(Device.VIRTUAL)) {
                    done = sendToVirtual(device, payload.substring(1), thingsAdapter, scriptingAdapter);
                } else if (null == sourceDevice && event.getType().equals(IotEvent.PLATFORM_MONITORING)) {
                    done = sendToVirtual(device, payload, thingsAdapter, scriptingAdapter);
                } else {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(this, "blocked command from virtual to virtual device"));
                    done = true;
                }

            } else if (device.getType().equals(Device.TTN)) {
                done = sendToTtn(device, payload.substring(1), type == HEX_COMMAND);
            } else if (device.getType().equals(Device.LORA)) {
                // TODO: not implemented
                done = true;
            } else if (device.getType().equals(Device.KPN)) {
                // TODO: not implemented
                done = true;
            } else if (device.getType().equals(Device.GENERIC) || device.getType().equals(Device.GATEWAY)) {
                // Nothing to do. Command will be included in the response for the next device
                // data transfer.
                done = false;
            } else if (device.getType().equals(Device.EXTERNAL)) {
                done = sendToWebhook(device, payload.substring(1), type==HEX_COMMAND);
            }
            event.setId(((Service) Kernel.getInstance()).getCommandId(deviceEUI));
            if (done) {
                actuatorCommandsDB.putCommandLog(event.getOrigin(), event);
            } else {
                actuatorCommandsDB.putDeviceCommand(event.getOrigin(), event);
            }
            Kernel.getInstance().dispatchEvent(
                    Event.logInfo(this, "processCommand " + event.getOrigin() + ":" + event.getPayload()));
        } catch (ThingsDataException e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
        } catch (Exception e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
        }

    }

    /**
     * Redirect command to virtual device.
     *
     * @param device
     * @param command
     * @param thingsAdapter
     * @param scriptingAdapter
     * @return
     */
    private boolean sendToVirtual(Device device, String command, ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter) {
        try {
            String cmd = new String(Base64.getDecoder().decode(command));
            long now = System.currentTimeMillis();
            thingsAdapter.updateHealthStatus(device.getEUI(), now, 0/* new frame count */, "", "");
            ArrayList<ArrayList> outputList;
            try {
                Object[] processingResult = DataProcessor.processValues(new ArrayList(), device, scriptingAdapter,
                        now, device.getLatitude(), device.getLongitude(), device.getAltitude(), "", cmd);
                outputList = (ArrayList<ArrayList>) processingResult[0];
                for (int i = 0; i < outputList.size(); i++) {
                    thingsAdapter.putData(device.getUserID(), device.getEUI(), device.getProject(), device.getState(),
                            fixValues(device, outputList.get(i)));
                }
                if (device.isActive() && device.getState().compareTo((Double) processingResult[1]) != 0) {
                    thingsAdapter.updateDeviceState(device.getEUI(), (Double) processingResult[1]);
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, e.getMessage()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // end
        return true;
    }

    private boolean sendToTtn(Device device, String payload, boolean hexRepresentation) {
        return sendToTtn(device, payload, hexRepresentation, null);
    }

    private boolean sendToTtn(Device device, String payload, boolean hexRepresentation, String forceUrl) {
        // payload to jest String będący tablicą bajtów zapisanych w formacie hex
        // odpowiednie encodowanie jest w TtnDownlinkMessage
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
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName() + ".sendToTtn()",
                    "plain text commands not implemented"));
            return false;
        }
        /*
         * TtnDownlinkMessage message = new TtnDownlinkMessage("aaa", null, false, 1);
         * message.addField("on", true);
         * message.addField("color", "blue");
         */
        String requestBody = JsonWriter.objectToJson(message, args);
        HttpClient client = new HttpClient();
        Request request = new Request()
                .setUrl(downlink)
                .setMethod("POST").setProperty("Content-Type", "application/json")
                .setData(requestBody);
        Result result;
        try {
            result = client.send(request);
        } catch (AdapterException ex) {
            return false;
        }
        return 200 == result.getCode();
    }

    private boolean sendToWebhook(Device device, String payload, boolean hexRepresentation) {
        CommandWebHookIface webhookSender = (CommandWebHookIface) Kernel.getInstance().getAdaptersMap()
                .get("CommandWebHook");
        if (null == webhookSender) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, "CommandWebHook adaper not configured"));
            return false;
        } else {
            return webhookSender.send(device, payload, hexRepresentation);
        }
    }

    public Event getCommand(String deviceEUI, ActuatorCommandsDBIface actuatorCommandsDB) {
        Event result = null;
        if (deviceEUI != null) {
            try {
                result = (Event) actuatorCommandsDB.getFirstCommand(deviceEUI);
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

}
