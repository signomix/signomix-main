/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.common.iot.ChannelData;
import com.signomix.common.iot.Device;
import com.signomix.event.IotEvent;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.db.IotDbDataIface;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.iot.VirtualDevice;
import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.MessageEnvelope;
import com.signomix.out.script.ScriptingAdapterIface;
import java.util.ArrayList;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class IotEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(IotEventHandler.class);

    public static void handleEvent(
            Kernel kernel,
            Event event,
            SchedulerIface scheduler,
            UserAdapterIface userAdapter,
            ThingsDataIface thingsAdapter,
            DashboardAdapterIface dashboardAdapter,
            AuthAdapterIface authAdapter,
            ScriptingAdapterIface scriptingAdapter,
            //ActuatorCommandsDBIface actuatorCommandsDB,
            MessageBrokerIface externalNotificator,
            IotDbDataIface iotDB) {
        String[] origin;
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            switch (event.getType()) {
                case IotEvent.GENERAL:
                case IotEvent.INFO:
                case IotEvent.WARNING:
                case IotEvent.ALERT:
                case IotEvent.DEVICE_LOST:
                case IotEvent.PLATFORM_DEVICE_LIMIT_EXCEEDED:
                    origin = event.getOrigin().split("\t");
                    if (origin.length < 2) {
                        // TODO: log error
                        System.out.println(">>>>> event origin not properly set: " + event.getOrigin());
                        break;
                    }
                    // save alert
                    AlertModule.getInstance().putAlert(event, thingsAdapter);
                    // send message
                    User user;
                    String payload;
                    String[] params;
                    try {
                        user = userAdapter.get(origin[0]);
                        if (user == null) {
                            // TODO: this shouldn't happen - log error?
                            logger.warn("user is null");
                            break;
                        }
                        String nodeName = origin[1];
                        if (IotEvent.DEVICE_LOST.equals(event.getType())) {
                            // this event can appear several times: for device owner + all team members
                            // TODO: the event should be directed for "system" user and only this
                            // particulrar
                            // event instance should modify alert status
                            thingsAdapter.updateAlertStatus(nodeName, Device.FAILURE);
                            // see also:
                            // ThingsDataIface.updateHealthStatus()
                            // DeviceManagementModule.checkStatus()
                        }

                        String message = (String) event.getPayload();
                        String response = "";

                        // Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(),
                        // "externalNotificator="+(null!=externalNotificator)+","+externalNotificator.isReady()));
                        if (null != externalNotificator
                                && externalNotificator.isReady()) {
                            String[] channelConfig = user.getChannelConfig(event.getType());
                            if (channelConfig == null || channelConfig.length < 2 || channelConfig[0].toUpperCase().startsWith("SIGNOMIX:")) {
                                break; // OK, notification on the web GUI or API
                            } else {
                                MessageEnvelope wrapper = new MessageEnvelope();
                                wrapper.type = event.getType();
                                wrapper.eui = nodeName;
                                wrapper.message = message;
                                wrapper.user = user;
                                response = externalNotificator.send(wrapper);
                            }
                        }
                        if (null != response && response.startsWith("ERROR")) {
                            logger.warn(response);
                        }
                    } catch (UserException ex) {
                        Kernel.getInstance().dispatchEvent(
                                Event.logWarning(IotEventHandler.class.getSimpleName(), ex.getMessage()));
                    } catch (ThingsDataException ex) {
                        Kernel.getInstance().dispatchEvent(Event.logSevere(IotEventHandler.class.getSimpleName(),
                                "updateAlertStatus() " + ex.getMessage()));
                    }
                    break;
                case IotEvent.CHANNEL_REMOVE:
                    payload = "" + (String) event.getPayload();
                    params = payload.split("@");
                    // create user alert with information about channel removal
                    try {
                        Device device = thingsAdapter.getDevice(false,params[1]);
                        IotEvent info = new IotEvent(IotEvent.INFO, params[1] + "\t" + device.getUserID(),
                                "Data channel \"" + params[0]
                                        + "\" has been removed from the device definition. You should check dependent dashboards.");
                        Kernel.getInstance().dispatchEvent(info);
                    } catch (ThingsDataException e) {
                        Kernel.getInstance().dispatchEvent(Event.logSevere(
                                IotEventHandler.class.getSimpleName() + ".processIotEvent()", e.getMessage()));
                    }
                    break;
                case IotEvent.DEVICE_REGISTERED:
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(),
                            event.getPayload() + " device registered"));
                    PlatformAdministrationModule.getInstance().buildDefaultDashboard((String) event.getPayload(),
                            thingsAdapter, dashboardAdapter, authAdapter);
                    break;

                case IotEvent.DEVICE_REMOVED:
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(),
                            event.getPayload() + " device unregistered"));
                    try {
                        params = ((String) event.getPayload()).split("\t");
                        if (params.length == 2) {
                            dashboardAdapter.removeDashboard(params[1], params[0]);
                        }
                    } catch (Exception e) {
                        Kernel.getInstance()
                                .dispatchEvent(Event.logWarning("Problem removing dashboards", e.getMessage()));
                    }
                    break;
                case IotEvent.DASHBOARD_SHARED:
                    // TODO: add "public" team member to dashboards reported
                    break;
                case IotEvent.DASHBOARD_REMOVED:
                case IotEvent.DASHBOARD_UNSHARED:
                    // TODO: remove "public" team member from devices raported by this dashboard
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(),
                            "removing shared token " + event.getPayload()));
                    try {
                        authAdapter.removePermanentToken("" + event.getPayload());
                    } catch (AuthException ex) {
                        Kernel.getInstance().dispatchEvent(Event.logWarning(IotEventHandler.class.getSimpleName(),
                                "error while removing token " + event.getPayload() + "->" + ex.getMessage()));
                    }
                    break;
                case IotEvent.VIRTUAL_DATA:
                    String sourceDeviceEui = event.getOrigin();
                    payload = (String) event.getPayload();
                    String[] channels = payload.split(";");
                    params = channels[0].split(":");
                    String deviceEUI = params[0];
                    Device device = null;
                    Device sourceDevice = null;
                    try {
                        sourceDevice = thingsAdapter.getDevice(false,sourceDeviceEui);
                        if (null != sourceDevice) {
                            device = thingsAdapter.getDevice(false,sourceDevice.getUserID(), -1, deviceEUI, false);
                        }
                    } catch (ThingsDataException ex) {
                        Kernel.getInstance().dispatchEvent(
                                Event.logWarning("IotEventHandler", "virtual device: " + ex.getMessage()));
                        return;
                    }
                    if (null == sourceDevice) {
                        System.out.println("SOURCE DEVICE NOT FOUND: " + sourceDeviceEui);
                        return;
                    }
                    if (null == device) {
                        System.out.println("VIRTUAL DEVICE NOT FOUND: " + deviceEUI);
                        return;
                    } else if (!device.getType().equals(Device.VIRTUAL)) {
                        System.out.println("DEVICE IS NOT VIRTUAL: " + deviceEUI);
                        return;
                    }
                    ArrayList<ChannelData> values = new ArrayList<>();
                    ChannelData data;
                    String channelName;
                    Double value;
                    long timestamp;
                    //VirtualDevice vd;
                    for (String channel : channels) {
                        params = channel.split(":");
                        channelName = params[1];
                        try {
                            value = Double.parseDouble(params[2]);
                            timestamp = Long.parseLong(params[3]);
                            data = new ChannelData();
                            data.setDeviceEUI(device.getEUI());
                            data.setName(channelName);
                            data.setValue(value);
                            data.setTimestamp(timestamp);
                            values.add(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    if (!values.isEmpty()) {
                        DeviceIntegrationModule.getInstance().writeVirtualData(thingsAdapter, scriptingAdapter, device,
                                values);
                    }
                    break;
                case IotEvent.ACTUATOR_CMD:
                        ActuatorModule.getInstance().processCommand(event, ActuatorModule.JSON_COMMAND, (ActuatorCommandsDBIface) iotDB,
                                thingsAdapter, scriptingAdapter);
                    break;
                case IotEvent.ACTUATOR_HEXCMD:
                        ActuatorModule.getInstance().processCommand(event, ActuatorModule.HEX_COMMAND, (ActuatorCommandsDBIface) iotDB,
                                thingsAdapter, scriptingAdapter);
                    break;
                    case IotEvent.ACTUATOR_PLAINCMD:
                        ActuatorModule.getInstance().processCommand(event, ActuatorModule.PLAIN_COMMAND, (ActuatorCommandsDBIface) iotDB,
                                thingsAdapter, scriptingAdapter);
                    break;
                case IotEvent.PLATFORM_MONITORING:
                    String eui = (String) kernel.getProperties().getOrDefault("monitoring_device", "");
                    if (!eui.isEmpty()) {
                        Device d = null;
                        try {
                            d = thingsAdapter.getDevice(false,eui);
                        } catch (ThingsDataException ex) {
                        }
                        if (null != d) {
                            event.setOrigin("@" + eui); // source@target
                                ActuatorModule.getInstance().processCommand(event, ActuatorModule.JSON_COMMAND,
                                        (ActuatorCommandsDBIface) iotDB, thingsAdapter, scriptingAdapter);
                        }
                    }
                    break;
                default:
                    Kernel.getInstance().dispatchEvent(
                            Event.logWarning(
                                    "Don't know how to handle category/type " + event.getCategory() + "/"
                                            + event.getType(),
                                    "" + event.getPayload()));
            }
        }
    }

}
