/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.iot.IotEvent;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.iot.VirtualDevice;
import com.signomix.out.iot.VirtualStackIface;
import com.signomix.out.notification.NotificationIface;
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

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class IotEventHandler {

    public static void handleEvent(
            Kernel kernel,
            Event event,
            SchedulerIface scheduler,
            UserAdapterIface userAdapter,
            ThingsDataIface thingsAdapter,
            NotificationIface smtpNotification,
            NotificationIface smsNotification,
            NotificationIface pushoverNotification,
            NotificationIface slackNotification,
            NotificationIface telegramNotification,
            DashboardAdapterIface dashboardAdapter,
            AuthAdapterIface authAdapter,
            VirtualStackIface virtualStackAdapter,
            ScriptingAdapterIface scriptingAdapter,
            ActuatorCommandsDBIface actuatorCommandsDB) {
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
                    //save alert
                    AlertModule.getInstance().putAlert(event, thingsAdapter);
                    //send message
                    User user;
                    String payload;
                    String[] params;
                    try {
                        user = userAdapter.get(origin[0]);
                        if (user == null) {
                            //TODO: this shouldn't happen - log error?
                            break;
                        }
                        String nodeName = origin[1];
                        if (IotEvent.DEVICE_LOST.equals(event.getType())) {
                            // this event can appear several times: for device owner + all team members
                            thingsAdapter.updateAlertStatus(nodeName, Device.FAILURE);
                            // see also: 
                            // ThingsDataIface.updateHealthStatus()
                            // DeviceManagementModule.checkStatus()
                        }
                        String[] channelConfig = user.getChannelConfig(event.getType());
                        if (!(channelConfig != null && channelConfig.length == 2)) {
                            break; // OK its normal behaviour
                        }
                        String messageChannel = channelConfig[0];
                        String address = channelConfig[1];
                        String message = (String) event.getPayload();
                        String response = "";
                        switch (messageChannel.toUpperCase()) {
                            case "SMTP":
                                response = smtpNotification.send(address, nodeName, message);
                                break;
                            case "SMS":
                                response = smsNotification.send(address, nodeName, message);
                                break;
                            case "PUSHOVER":
                                response = pushoverNotification.send(address, nodeName, message);
                                break;
                            case "SLACK":
                                response = slackNotification.send(address, nodeName, message);
                                break;
                            case "TELEGRAM":
                                response = telegramNotification.send(address, nodeName, message);
                                break;
                            default:
                                Kernel.getInstance().dispatchEvent(Event.logWarning(IotEventHandler.class.getSimpleName(), "message channel " + messageChannel + " not supported"));
                        }
                        if (response.startsWith("ERROR")) {
                            Kernel.getInstance().dispatchEvent(Event.logWarning(IotEventHandler.class.getSimpleName(), response));
                        }
                    } catch (UserException ex) {
                        Kernel.getInstance().dispatchEvent(Event.logWarning(IotEventHandler.class.getSimpleName(), ex.getMessage()));
                    } catch (ThingsDataException ex) {
                        Kernel.getInstance().dispatchEvent(Event.logSevere(IotEventHandler.class.getSimpleName(), "updateAlertStatus() " + ex.getMessage()));
                    }
                    break;
                case IotEvent.CHANNEL_REMOVE:
                    payload = "" + (String) event.getPayload();
                    params = payload.split("@");
                    //create user alert with information about channel removal
                    try {
                        Device device = thingsAdapter.getDevice(params[1]);
                        IotEvent info = new IotEvent(IotEvent.INFO, params[1] + "\t" + device.getUserID(), "Data channel \"" + params[0] + "\" has been removed from the device definition. You should check dependent dashboards.");
                        Kernel.getInstance().dispatchEvent(info);
                    } catch (ThingsDataException e) {
                        Kernel.getInstance().dispatchEvent(Event.logSevere(IotEventHandler.class.getSimpleName() + ".processIotEvent()", e.getMessage()));
                    }
                    break;
                case IotEvent.DEVICE_REGISTERED:
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(), event.getPayload() + " device registered"));
                    PlatformAdministrationModule.getInstance().buildDefaultDashboard((String) event.getPayload(), thingsAdapter, dashboardAdapter, authAdapter);
                    break;

                case IotEvent.DEVICE_REMOVED:
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(), event.getPayload() + " device unregistered"));
                    try {
                        params = ((String) event.getPayload()).split("\t");
                        if (params.length == 2) {
                            dashboardAdapter.removeDashboard(params[1], params[0]);
                        }
                    } catch (Exception e) {
                        Kernel.getInstance().dispatchEvent(Event.logWarning("Problem renoving dashboards", e.getMessage()));
                    }
                    break;
                case IotEvent.DASHBOARD_SHARED:
                    //TODO: add "public" team member to dashboards reported
                    break;
                case IotEvent.DASHBOARD_REMOVED:
                case IotEvent.DASHBOARD_UNSHARED:
                    //TODO: remove "public" team member from devices raported by this dashboard
                    Kernel.getInstance().dispatchEvent(Event.logInfo(IotEventHandler.class.getSimpleName(), "removing shared token " + event.getPayload()));
                    try {
                        authAdapter.removePermanentToken("" + event.getPayload());
                    } catch (AuthException ex) {
                        Kernel.getInstance().dispatchEvent(Event.logWarning(IotEventHandler.class.getSimpleName(), "error while removing token " + event.getPayload() + "->" + ex.getMessage()));
                    }
                    break;
                case IotEvent.VIRTUAL_DATA:
                    try {
                        String userID = event.getOrigin();
                        payload = (String) event.getPayload();
                        String[] channels = payload.split(";");
                        params = channels[0].split(":");
                        String deviceEUI = params[0];

                        //params = payload.split(":");
                        Device device = thingsAdapter.getDevice(userID, deviceEUI, false);
                        if (device != null) {
                            if (device.getType().equals(Device.VIRTUAL)) {
                                ArrayList<ChannelData> values = new ArrayList<>();
                                ChannelData data;
                                String channelName;
                                Double value;
                                long timestamp;
                                boolean counterChange;
                                VirtualDevice vd;
                                for (String channel : channels) {
                                    params = channel.split(":");
                                    channelName = params[1];
                                    try {
                                        value = Double.parseDouble(params[2]);
                                        timestamp = Long.parseLong(params[3]);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                    counterChange = false;
                                    vd = new VirtualDevice(device.getEUI());
                                    if ("_in".equals(channelName) && value != 0) { // previously "incoming"
                                        counterChange = true;
                                        virtualStackAdapter.get(vd).addInputs(value.longValue());
                                    } else if ("_out".equals(channelName) && value != 0) { // previously "exiting"
                                        counterChange = true;
                                        virtualStackAdapter.get(vd).addOutputs(value.longValue());
                                    } else {
                                        //virtua/lStackAdapter.get(device.getEUI())
                                        //TODO: ? reset?
                                    }
                                    if (counterChange && virtualStackAdapter.getProperty("write-interval").isEmpty()) {
                                        // empty write interval == write virtual device counters to the data channels
                                        DeviceIntegrationModule
                                                .getInstance()
                                                .writeVirtualState(vd, device, userID, thingsAdapter, virtualStackAdapter, scriptingAdapter, false, null);
                                    } else if (!counterChange) {
                                        data = new ChannelData();
                                        data.setDeviceEUI(device.getEUI());
                                        data.setName(channelName);
                                        data.setValue(value);
                                        data.setTimestamp(timestamp);
                                        values.add(data);
                                    }
                                }
                                if (!values.isEmpty()) {
                                    DeviceIntegrationModule.getInstance().writeVirtualData(thingsAdapter, scriptingAdapter, device, values);
                                }
                            } else {
                                System.out.println("NOT VIRTUAL DEVICE");
                            }
                        } else {
                            System.out.println("DEVICE NOT FOUND");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case IotEvent.ACTUATOR_CMD:
                    ActuatorModule.getInstance().processCommand(event, false, actuatorCommandsDB, virtualStackAdapter, thingsAdapter, scriptingAdapter);
                    break;
                case IotEvent.ACTUATOR_HEXCMD:
                    ActuatorModule.getInstance().processCommand(event, true, actuatorCommandsDB, virtualStackAdapter, thingsAdapter, scriptingAdapter);
                    break;
                default:
                    Kernel.getInstance().dispatchEvent(
                            Event.logWarning("Don't know how to handle category/type " + event.getCategory() + "/" + event.getType(),
                                    "" + event.getPayload())
                    );
            }
        }
    }

}
