/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.signomix.in.http.ActuatorApi;
import com.signomix.iot.IotEvent;
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
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;

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
        boolean dump = false;
        if ("true".equalsIgnoreCase(actuatorApi.getProperty("dump-request"))) {
            System.out.println(HttpAdapter.dumpRequest(request));
            dump = true;
        }
        StandardResult result = new StandardResult();
        boolean debugMode = "true".equalsIgnoreCase(request.headers.getFirst("X-debug"));
        String userID = request.headers.getFirst("X-user-id");
        Device device;
        try {
            device = thingsAdapter.getDevice(userID, request.pathExt, false);
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
                result = processPost(device, userID, request, actuatorCommandsDB, virtualStack, thingsAdapter, scriptingAdapter);
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
            ActuatorCommandsDBIface actuatorCommandsDB,
            VirtualStackIface virtualStack,
            ThingsDataIface thingsAdapter,
            ScriptingAdapterIface scriptingAdapter) {
        StandardResult result = new StandardResult();

        // dla virtual device obsługa powinna być inna
        //TODO: channel name other than "counter"?
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

        IotEvent event = new IotEvent();
        event.setOrigin(device.getEUI());
        event.setType(IotEvent.ACTUATOR_CMD);
        event.setPayload(request.body);
        /*try {
            actuatorCommandsDB.putDeviceCommand(event.getOrigin(), event);
            result.setCode(HttpAdapter.SC_ACCEPTED);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logSevere(this, ex.getMessage()));
            result.setCode(HttpAdapter.SC_INTERNAL_SERVER_ERROR);
        }*/
        return result;
    }

    public void processCommand(
            Event event,
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
                Kernel.handle(Event.logWarning(this, "device " + deviceEUI + " not found"));
                return;
            }

            if (device.getType().equals(Device.VIRTUAL)) {
                done = sendToVirtual(device, payload, virtualStack, thingsAdapter, scriptingAdapter);
            } else if (device.getType().equals(Device.TTN)) {

            } else if (device.getType().equals(Device.LORA)) {

            } else if (device.getType().equals(Device.GENERIC) || device.getType().equals(Device.GATEWAY)) {

            }
            Event commandEvent = new Event(deviceEUI, Event.CATEGORY_GENERIC, "COMMAND", null, payload);
            commandEvent.setId(event.getId());
            commandEvent.setCreatedAt(event.getCreatedAt());
            if (done) {
                actuatorCommandsDB.putCommandLog(commandEvent.getOrigin(), commandEvent);
            } else {
                actuatorCommandsDB.putDeviceCommand(commandEvent.getOrigin(), commandEvent);
            }
            Kernel.handle(Event.logInfo(this, "processCommand " + commandEvent.getOrigin() + ":" + commandEvent.getPayload()));
        } catch (ThingsDataException e) {
            Kernel.handle(Event.logWarning(this, e.getMessage()));
        } catch (Exception e) {
            Kernel.handle(Event.logWarning(this, e.getMessage()));
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
        try {
            if (channelName != null) {
                ChannelData data = new ChannelData();
                data.setDeviceEUI(device.getEUI());
                data.setName(channelName);
                data.setValue(new Double(virtualStack.get(vd).get()));
                data.setTimestamp(System.currentTimeMillis());
                thingsAdapter.putData(device.getUserID(), device.getEUI(), data.getName(), data);
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

    public Event getCommand(String deviceEUI, ActuatorCommandsDBIface actuatorCommandsDB) {
        //String result = "";
        Event result=null;
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
