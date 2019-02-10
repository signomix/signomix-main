/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.iot.IotEvent;
import java.util.ArrayList;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import java.util.Base64;
import java.util.Random;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.User;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DeviceManagementModule {

    private static DeviceManagementModule service;

    public static DeviceManagementModule getInstance() {
        if (service == null) {
            service = new DeviceManagementModule();
        }
        return service;
    }

    /**
     *
     */
    public Object processEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform) {
        //TODO: exception handling - send 400 or 403
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        String issuerID = request.headers.getFirst("X-issuer-id");
        //request.headers.keySet().forEach(key -> System.out.println(key + ":" + request.headers.getFirst(key)));
        String pathExt = request.pathExt;
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            result.setData("user not recognized");
            return result;
        }
        try {
            if (pathExt.isEmpty()) {  // user id is from request header
                switch (request.method) {
                    case "GET":  // get list of user devices
                        result.setData(getUserDevices(userID, thingsAdapter));
                        break;
                    case "POST": // add new device
                        User user = users.get(userID);
                        Device device = buildDevice(request, userID, null);
                        int numberOfDevices = thingsAdapter.getUserDevicesCount(userID);
                        try {
                            platform.checkDevicesLimit(user, numberOfDevices);
                            thingsAdapter.putDevice(userID, device);
                            result.setCode(HttpAdapter.SC_CREATED);
                            result.setData(device.getEUI());
                            Kernel.getInstance().dispatchEvent(
                                    new Event(Kernel.getInstance().getName(), IotEvent.CATEGORY_IOT, IotEvent.DEVICE_REGISTERED, null, device.getEUI())
                            );
                        } catch (NullPointerException ex) {
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                            result.setData("wrong parameters");
                        } catch (ThingsDataException ex) {
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                            result.setData(ex.getMessage());
                        } catch (PlatformException ex) {
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            if (ex.getCode() == PlatformException.TOO_MANY_USER_DEVICES) {
                                Kernel.handle(
                                        new IotEvent(IotEvent.PLATFORM_DEVICE_LIMIT_EXCEEDED, "unable to add device (limit reached)").addOrigin(userID + "\tSIGNOMIX")
                                );
                            }
                            result.setCode(PlatformAdministrationModule.ERR_PAYMENT_REQUIRED);
                            result.setData(ex.getMessage());
                        }
                        break;
                    default:
                        result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
                }
            } else {  // 
                switch (request.method) {
                    case "GET":  // get device definition or channel data - depending on pathExt
                        String query = (String) request.parameters.getOrDefault("query", null);
                        String params[] = pathExt.split("/"); // pathExt has form: deviceID/channel (so deviceID must be unique)
                        String tmpUserID = userID;
                        if ("public".equals(tmpUserID)) {
                            tmpUserID = issuerID;
                        }
                        switch (params.length) {
                            case 2:  //get channel data
                                // in case of request from shared dashboards we needt to use token issuer ID instead of tuken user ID
                                //TODO: data query
                                // zmiana: 
                                if (null == query) {
                                    query = "last"; //nigdy nie zwracany wszystkich rekordów
                                }                                //if (null == query) {
                                //    result.setData(getAllValuesOfChannel(tmpUserID, params[0], params[1], thingsAdapter));
                                //} else {
                                result.setData(getValuesOfChannel(tmpUserID, params[0], params[1], query, thingsAdapter));
                                //}
                                break;
                            case 1: // get device definition
                                // get device info
                                // zmiana: 
                                if (query != null) {
                                    result.setData(getValues(tmpUserID, params[0], query, thingsAdapter));
                                } else {
                                    Device device = getDevice(userID, params[0], thingsAdapter);
                                    if (device != null) {
                                        result.setData(device);
                                    } else {
                                        result.setCode(HttpAdapter.SC_NOT_FOUND);
                                        result.setData("not found");
                                    }
                                }
                                break;
                            default:
                                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                                break;
                        }
                        break;
                    case "PUT": // update device definition
                        Device device = buildDevice(request, userID, getDevice(userID, pathExt, thingsAdapter));
                        try {
                            thingsAdapter.modifyDevice(userID, device);
                        } catch (ThingsDataException ex) {
                            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                        }
                        break;
                    case "DELETE": // remove device and its channels
                        Device dev = getDevice(userID, pathExt, thingsAdapter);
                        if (dev.getUserID().equals(userID)) {
                            removeDevice(pathExt, thingsAdapter);
                            Kernel.getInstance().dispatchEvent(
                                    new Event(Kernel.getInstance().getName(), IotEvent.CATEGORY_IOT, IotEvent.DEVICE_REMOVED, null, dev.getEUI())
                            );
                        } else {
                            result.setCode(HttpAdapter.SC_UNAUTHORIZED);
                        }
                        break;
                    default:
                        result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void removeUserData(String userId, ThingsDataIface thingsAdapter) {
        try {
            thingsAdapter.removeAllDevices(userId);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "unable to clear user data of user " + userId));
        }
    }

    public void checkStatus(ThingsDataIface thingsAdapter) {
        ArrayList<Device> devices = null;
        try {
            devices = (ArrayList<Device>) thingsAdapter.getInactiveDevices();
            ArrayList<String> recipents;
            String[] team;
            IotEvent ev;
            for (Device device : devices) {
                recipents = new ArrayList<>();
                team = device.getTeam().split(",");
                for (String member : team) {
                    if (!member.isEmpty()) {
                        recipents.add(member);
                    }
                }
                if (!recipents.contains(device.getUserID())) {
                    recipents.add(device.getUserID());
                }
                for (String recipent : recipents) {
                    ev = new IotEvent(IotEvent.DEVICE_LOST, "possible device failure").addOrigin(recipent + "\t" + device.getEUI());
                    Kernel.getInstance().dispatchEvent(ev);
                }

            }
        } catch (ThingsDataException e) {
            e.printStackTrace();
        }
    }

    private Device buildDevice(RequestObject request, String userID, Device original) {
        // TODO: what if new definition has some channels removed?
        Device device = new Device();
        request.parameters.keySet().forEach(key -> {
        });
        String eui = (String) request.parameters.getOrDefault("eui", "");
        if (eui == null || eui.isEmpty()) {
            return null;
        }
        device.setEUI((String) request.parameters.getOrDefault("eui", ""));
        if (original != null) {
            device.setEUI(original.getEUI());
        }
        device.setType((String) request.parameters.getOrDefault("type", "GENERIC"));
        device.setUserID(userID);

        String newKey = (String) request.parameters.getOrDefault("key", "");
        if (newKey != null && !newKey.isEmpty()) {
            device.setKey(newKey);
        } else {
            Random r = new Random(System.currentTimeMillis());
            device.setKey(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
        }

        String newName = (String) request.parameters.getOrDefault("name", "");
        if (newName != null && !newName.isEmpty()) {
            device.setName(newName);
        } else {
            device.setName(device.getEUI());
        }
        String newAppEUI = (String) request.parameters.getOrDefault("appeui", "");
        if (newAppEUI != null && !newAppEUI.isEmpty()) {
            device.setApplicationEUI(newAppEUI);
        }
        String newAppID = (String) request.parameters.getOrDefault("appid", "");
        if (newAppID != null && !newAppID.isEmpty()) {
            device.setApplicationID(newAppID);
        }
        String newTeam = (String) request.parameters.getOrDefault("team", "");
        if (newTeam != null && !newTeam.isEmpty()) {
            device.setTeam(newTeam);
        }
        String newDescription = (String) request.parameters.getOrDefault("description", "");
        if (newDescription != null && !newDescription.isEmpty()) {
            device.setDescription(newDescription);
        }
        device.setChannels((String) request.parameters.get("channels"));

        String newInterval = (String) request.parameters.getOrDefault("transmissionInterval", "");
        try {
            device.setTransmissionInterval(Long.parseLong(newInterval));
        } catch (NullPointerException | NumberFormatException e) {
        }

        String newCode = (String) request.parameters.getOrDefault("code", "");
        if (newCode != null && !newCode.isEmpty()) {
            try {
                newCode = newCode.trim();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            device.setCode(newCode);
        }
        String newEncoder = (String) request.parameters.getOrDefault("encoder", "");
        if (newEncoder != null && !newEncoder.isEmpty()) {
            try {
                newEncoder = newEncoder.trim();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            device.setEncoder(newEncoder);
        }
        if (original != null) {
            original.getChannels().forEach((k, v) -> {
                if (!device.getChannels().containsKey(k)) {
                    // if existing channel is not set for new device
                    // send event to remove it from the database
                    Kernel.handle(
                            new IotEvent()
                                    .addType(IotEvent.CHANNEL_REMOVE)
                                    .addPayload(k + "@" + device.getEUI())
                    );
                }
            });
        }

        // frame control
        String newCheckFrames = (String) request.parameters.get("framecheck");
        if (newCheckFrames != null && !newCheckFrames.isEmpty()) {
            boolean checkF = Boolean.parseBoolean(newCheckFrames);
            device.setCheckFrames(checkF);
        }
        boolean resetFrames = Boolean.parseBoolean((String) request.parameters.getOrDefault("framesreset", "false"));
        if (resetFrames) {
            device.setLastFrame(-1);
        }
        return device;
    }

    private Device getDevice(String userID, String deviceEUI, ThingsDataIface thingsAdapter) {
        try {
            return thingsAdapter.getDevice(userID, deviceEUI, true);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
        return null;
    }

    private void removeDevice(String deviceEUI, ThingsDataIface thingsAdapter) {
        try {
            String userID = thingsAdapter.getDevice(deviceEUI).getUserID();
            thingsAdapter.removeDevice(deviceEUI);
            Kernel.handle(new IotEvent(IotEvent.DEVICE_REMOVED, deviceEUI + "\t" + userID));
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
    }

    /*private List getAllValuesOfChannel(String userID, String deviceEUI, String channelID, ThingsDataIface thingsAdapter) {
        try {
            return (ArrayList) thingsAdapter.getAllValues(userID, deviceEUI, channelID);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }
    }*/
    private List getValuesOfChannel(String userID, String deviceEUI, String channelID, String query, ThingsDataIface thingsAdapter) {
        try {
            if (channelID != null && !"$".equals(channelID)) {
                //Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), "channelID parameter is not supported"));
                return (ArrayList) thingsAdapter.getValues(userID, deviceEUI, channelID, query);
            } else {
                return (ArrayList) thingsAdapter.getValues(userID, deviceEUI, query);
            }
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }
    }

    private List getValues(String userID, String deviceEUI, String query, ThingsDataIface thingsAdapter) {
        try {
            return thingsAdapter.getValues(userID, deviceEUI, query);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }
    }

    private List getUserDevices(String userID, ThingsDataIface thingsAdapter) {
        try {
            return (ArrayList) thingsAdapter.getUserDevices(userID, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }

    }
    /*
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
        ArrayList<ChannelData> finalValues = null;
        try {
            finalValues = DataProcessor.processValues((ArrayList) values, device, scriptingAdapter);
        } catch (Exception e) {
            Kernel.handle(Event.logWarning(this, e.getMessage()));
        }
        thingsAdapter.putData(userID, device.getEUI(), values);
        //thingsAdapter.putVirtualData(userID, device, scriptingAdapter, values);
        //TODO: use DataProcessor
    }
     */
}
