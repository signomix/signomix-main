/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.event.IotEvent;
import java.util.ArrayList;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.DeviceGroup;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.User;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DeviceManagementModule {

    private static DeviceManagementModule service;
    
    private long DEFAULT_GROUP_INTERVAL=60*60*1000; //60 MINUT

    public static DeviceManagementModule getInstance() {
        if (service == null) {
            service = new DeviceManagementModule();
        }
        return service;
    }

    /**
     *
     */
    public Object processDeviceEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform) {
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
                            ex.printStackTrace();
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                            result.setData("wrong parameters");
                        } catch (ThingsDataException ex) {
                            ex.printStackTrace();
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
                                    query = "last 1"; //nigdy nie zwracany wszystkich rekordów
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
                                    if (query.endsWith("csv.timeseries")) {
                                        //result.setHeader("Content-type", "text/csv");
                                        result.setFileExtension(".csv");
                                    }
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
                            StackTraceElement[] ste=ex.getStackTrace();
                            for(int i=0; i<ste.length;i++){
                                String msg="."+ste[i].getMethodName()+":"+ste[i].getLineNumber();
                                Kernel.handle(Event.logWarning(ste[i].getClassName(),msg));
                            }
                            
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                        }
                        break;
                    case "DELETE": // remove device and its channels
                        String eui = pathExt;
                        if (null != eui) {
                            eui = eui.toUpperCase();
                        }
                        Device dev = getDevice(userID, eui, thingsAdapter);
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

    public Object processTemplateEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform) {
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
            switch (request.method) {
                case "GET":  // get list of user devices
                    result.setData(getTemplates(thingsAdapter));
                    break;
                default:
                    result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processGroupEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform) {
        //TODO: exception handling - send 400 or 403
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        //String issuerID = request.headers.getFirst("X-issuer-id");
        String pathExt = request.pathExt;
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            result.setData("user not recognized");
            return result;
        }
        try {
            if (pathExt.isEmpty()) {  // user id is from request header
                switch (request.method) {
                    case "GET":  // get list of groups
                        if (null == request.parameters.get("group")) {
                            result.setData(getUserGroups(userID, thingsAdapter));
                        } else {
                            result.setData(
                                    getGroupDevices(userID, (String) request.parameters.get("group"), thingsAdapter)
                            );
                        }
                        break;
                    case "POST": // add new group
                        User user = users.get(userID);
                        DeviceGroup group = buildGroup(request, userID, null);
                        try {
                            thingsAdapter.putGroup(userID, group);
                            result.setCode(HttpAdapter.SC_CREATED);
                            result.setData(group.getEUI());
                            Kernel.getInstance().dispatchEvent(
                                    new Event(Kernel.getInstance().getName(), IotEvent.CATEGORY_IOT, IotEvent.GROUP_CREATED, null, group.getEUI())
                            );
                        } catch (NullPointerException ex) {
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                            result.setData("wrong parameters");
                        } catch (ThingsDataException ex) {
                            Kernel.handle(Event.logWarning(getClass().getSimpleName(), ex.getLocalizedMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                            result.setData(ex.getMessage());
                        }
                        break;
                    default:
                        result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
                }
            } else {
                switch (request.method) {
                    case "GET":
                        String[] params = pathExt.split("/");
                        switch (params.length) {
                            case 2:
                                String groupID = params[0];
                                String channels = params[1];
                                String[] channelNames = channels.split(",");
                                result.setData(getValuesOfGroup(userID, groupID, channelNames, thingsAdapter));
                                String format = event.getRequestParameter("format");
                                if (null != format && !format.isBlank()) {
                                    DeviceGroup group = getGroup(userID, groupID, thingsAdapter);
                                    result.setHeader("X-Format", format);
                                    result.setHeader("X-Group", groupID);
                                    result.setHeader("X-Group-Name", group.getName());
                                    String groupHref = group.getDescription().trim();
                                    if (groupHref.startsWith("http")) {
                                        groupHref = groupHref.replaceAll("[\\n\\t\\r ]", "").trim();
                                        result.setHeader("X-Group-Dashboard-Href", groupHref);
                                    }
                                }
                                break;

                            case 1:
                                result.setData(getGroup(userID, pathExt, thingsAdapter));
                                break;
                            default:
                                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                                break;
                        }
                        break;
                    case "PUT": // update device definition
                        DeviceGroup group = buildGroup(request, userID, getGroup(userID, pathExt, thingsAdapter));
                        try {
                            thingsAdapter.modifyGroup(userID, group);
                        } catch (ThingsDataException ex) {
                            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                            result.setCode(HttpAdapter.SC_BAD_REQUEST);
                        }
                        break;
                    case "DELETE": // remove device and its channels
                        String eui = pathExt;
                        if (null != eui) {
                            eui = eui.toUpperCase();
                        }
                        DeviceGroup gr = getGroup(userID, eui, thingsAdapter);
                        if (gr.getUserID().equals(userID)) {
                            removeGroup(pathExt, thingsAdapter);
                            Kernel.getInstance().dispatchEvent(
                                    new Event(Kernel.getInstance().getName(), IotEvent.CATEGORY_IOT, IotEvent.GROUP_REMOVED, null, gr.getEUI())
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

    public Object processGroupPublicationEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform) {
        //TODO: exception handling - send 400 or 403
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        String issuerID = request.headers.getFirst("X-issuer-id");
        String pathExt = request.pathExt;
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            result.setData("user not recognized");
            return result;
        }
        if (pathExt.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setData("path not set");
            return result;
        }
        if (!"GET".equalsIgnoreCase(request.method)) {
            result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
            return result;
        }
        String format = event.getRequestParameter("format");
        try {
            String groupID;
            String channels;
            String[] channelNames;
            DeviceGroup group;
            String[] params = pathExt.split("/");
            if (params.length == 0) {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
            } else {
                groupID = params[0];
                group = getGroup(userID, groupID, thingsAdapter);
                if (params.length > 1) {
                    channels = params[1];
                    channelNames = channels.split(",");
                } else {
                    channelNames=(String[]) group.getChannels().keySet().toArray(new String[group.getChannels().keySet().size()]);
                }
                result.setData(getValuesOfGroup(userID, groupID, channelNames, thingsAdapter));
                if (null != format && !format.isBlank()) {
                    result.setHeader("X-Format", format);
                    result.setHeader("X-Group", groupID);
                    result.setHeader("X-Group-Name", group.getName());
                    String groupHref = group.getDescription().trim();
                    if (groupHref.startsWith("http")) {
                        groupHref = groupHref.replaceAll("[\\n\\t\\r ]", "").trim();
                        result.setHeader("X-Group-Dashboard-Href", groupHref);
                    }
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

    private String createEui(String prefix) {
        return PlatformAdministrationModule.getInstance().createEui(prefix);
    }

    private Device buildDevice(RequestObject request, String userID, Device original) {
        // TODO: what if new definition has some channels removed?
        Device device = new Device();
        /*request.parameters.keySet().forEach(key -> {
            System.out.println(key);
        });
         */
        String eui = (String) request.parameters.getOrDefault("eui", "");
        if (eui == null || eui.isEmpty()) {
            eui = createEui("S-");
        }
        eui = eui.toUpperCase();
        device.setEUI(eui);
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
        String newAdministrators = (String) request.parameters.getOrDefault("administrators", "");
        if (newAdministrators != null && !newAdministrators.isEmpty()) {
            device.setAdministrators(newAdministrators);
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

        String newProject = (String) request.parameters.getOrDefault("project", "");
        device.setProject(newProject);
        String newActive = (String) request.parameters.getOrDefault("active", "true");
        device.setActive(Boolean.parseBoolean(newActive));

        String newLatitude = (String) request.parameters.getOrDefault("latitude", "");
        if (newLatitude != null && !newLatitude.isEmpty()) {
            try {
                device.setLatitude(Double.parseDouble(newLatitude.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String newLongitude = (String) request.parameters.getOrDefault("longitude", "");
        if (newLongitude != null && !newLongitude.isEmpty()) {
            try {
                device.setLongitude(Double.parseDouble(newLongitude.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String newAltitude = (String) request.parameters.getOrDefault("altitude", "");
        if (newAltitude != null && !newAltitude.isEmpty()) {
            try {
                device.setAltitude(Double.parseDouble(newAltitude.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String newState = (String) request.parameters.getOrDefault("state", "");
        if (newState != null && !newState.isEmpty()) {
            try {
                device.setState(Double.parseDouble(newState.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String newRetentionTime = (String) request.parameters.getOrDefault("retention", "");
        if (newRetentionTime != null && !newRetentionTime.isEmpty()) {
            try {
                device.setRetentionTime(Long.parseLong(newRetentionTime.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
        String newGroup = (String) request.parameters.getOrDefault("groups", "");
        if (newGroup != null && !newGroup.isEmpty()) {
            device.setGroups(newGroup);
        }
        String newDownlink = (String) request.parameters.getOrDefault("downlink", "");
        if (newDownlink != null && !newDownlink.isEmpty()) {
            device.setDownlink(newDownlink);
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

    private DeviceGroup buildGroup(RequestObject request, String userID, DeviceGroup original) {
        DeviceGroup group = new DeviceGroup();
        String eui = (String) request.parameters.getOrDefault("eui", "");
        if (eui == null || eui.isEmpty()) {
            eui = createEui("");
        }
        eui = eui.toUpperCase();
        group.setEUI(eui);
        if (original != null) {
            group.setEUI(original.getEUI());
        }
        group.setUserID(userID);

        String newName = (String) request.parameters.getOrDefault("name", "");
        if (newName != null && !newName.isEmpty()) {
            group.setName(newName);
        } else {
            group.setName(group.getEUI());
        }
        String newTeam = (String) request.parameters.getOrDefault("team", "");
        if (newTeam != null && !newTeam.isEmpty()) {
            group.setTeam(newTeam);
        }
        String newAdministrators = (String) request.parameters.getOrDefault("administrators", "");
        if (newAdministrators != null && !newAdministrators.isEmpty()) {
            group.setAdministrators(newAdministrators);
        }
        String newChannels = (String) request.parameters.getOrDefault("channels", "");
        if (newChannels != null && !newChannels.isEmpty()) {
            group.setChannels(newChannels);
        }
        String newDescription = (String) request.parameters.getOrDefault("description", "");
        if (newDescription != null && !newDescription.isEmpty()) {
            group.setDescription(newDescription);
        }
        return group;
    }

    private Device getDevice(String userID, String deviceEUI, ThingsDataIface thingsAdapter) {
        try {
            return thingsAdapter.getDevice(userID, deviceEUI, true);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
        return null;
    }

    private DeviceGroup getGroup(String userID, String groupEUI, ThingsDataIface thingsAdapter) {
        try {
            return thingsAdapter.getGroup(userID, groupEUI);
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

    private void removeGroup(String groupEUI, ThingsDataIface thingsAdapter) {
        try {
            String userID = thingsAdapter.getGroup(groupEUI).getUserID();
            thingsAdapter.removeGroup(userID, groupEUI);
            Kernel.handle(new IotEvent(IotEvent.GROUP_REMOVED, groupEUI + "\t" + userID));
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
                ArrayList result = (ArrayList) thingsAdapter.getValues(userID, deviceEUI, query + " channel " + channelID);
                return result;
            } else {
                return (ArrayList) thingsAdapter.getValues(userID, deviceEUI, query);
            }
        } catch (ThingsDataException ex) {
            ex.printStackTrace();
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

    private List getValuesOfGroup(String userID, String groupEUI, String[] channelNames, ThingsDataIface thingsAdapter) {
        try {
            //return thingsAdapter.getValuesOfGroup(userID, groupEUI, channelNames);
            return thingsAdapter.getLastValuesOfGroup(userID, groupEUI, channelNames, DEFAULT_GROUP_INTERVAL);
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

    private List getGroupDevices(String userID, String group, ThingsDataIface thingsAdapter) {
        try {
            return (ArrayList) thingsAdapter.getGroupDevices(userID, group);
        } catch (Exception ex) {
            ex.printStackTrace();
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }

    }

    private List getUserGroups(String userID, ThingsDataIface thingsAdapter) {
        try {
            return (ArrayList) thingsAdapter.getUserGroups(userID);
        } catch (Exception ex) {
            ex.printStackTrace();
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return new ArrayList();
        }

    }

    private List getTemplates(ThingsDataIface thingsAdapter) {
        try {
            return (ArrayList) thingsAdapter.getTemplates();
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
