/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.iot.IotEvent;
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
            DashboardAdapterIface dashboardAdapter,
            AuthAdapterIface authAdapter,
            VirtualStackIface virtualStackAdapter,
            ScriptingAdapterIface scriptingAdapter) {
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
                            default:
                                Kernel.handle(Event.logWarning(IotEventHandler.class.getSimpleName(), "message channel " + messageChannel + " not supported"));
                        }
                        if (response.startsWith("ERROR")) {
                            Kernel.handle(Event.logWarning(IotEventHandler.class.getSimpleName(), response));
                        }
                    } catch (UserException ex) {
                        Kernel.handle(Event.logWarning(IotEventHandler.class.getSimpleName(), ex.getMessage()));
                    }
                    break;
                case IotEvent.CHANNEL_REMOVE:
                    payload = "" + (String) event.getPayload();
                    params = payload.split("@");
                    //create user alert with information about channel removal
                    try {
                        Device device = thingsAdapter.getDevice(params[1]);
                        IotEvent info = new IotEvent(IotEvent.INFO, params[1] + "\t" + device.getUserID(), "Data channel \"" + params[0] + "\" has been removed from the device definition. You should check dependent dashboards.");
                        Kernel.handle(info);
                    } catch (ThingsDataException e) {
                        Kernel.handle(Event.logSevere(IotEventHandler.class.getSimpleName() + ".processIotEvent()", e.getMessage()));
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
                            dashboardAdapter.removeDashboard(params[1], params[1] + "~" + params[0]);
                        }
                    } catch (Exception e) {
                        Kernel.handle(Event.logWarning("Problem renoving dashboards", e.getMessage()));
                    }
                    break;
                case IotEvent.DASHBOARD_SHARED:
                    //TODO: add "public" team member to dashboards reported
                    break;
                case IotEvent.DASHBOARD_REMOVED:
                case IotEvent.DASHBOARD_UNSHARED:
                    //TODO: remove "public" team member from devices raported by this dashboard
                    Kernel.handle(Event.logInfo(IotEventHandler.class.getSimpleName(), "removing shared token " + event.getPayload()));
                    try {
                        authAdapter.removePermanentToken("" + event.getPayload());
                    } catch (AuthException ex) {
                        Kernel.handle(Event.logWarning(IotEventHandler.class.getSimpleName(), "error while removing token " + event.getPayload() + "->" + ex.getMessage()));
                    }
                    break;
                case IotEvent.VIRTUAL_DATA:
                    try {
                        String userID = event.getOrigin();
                        payload = (String) event.getPayload();
                        params = payload.split(":");
                        /*
                        System.out.println("userID="+userID);
                        System.out.println("EUI="+params[0]);//params[0] - device EUI
                        System.out.println("channel="+params[1]);//params[1] - channel name
                        System.out.println("value="+params[2]);//params[2] - value
                        System.out.println("timestamp="+params[3]);//params[3= - timestamp
                         */
                        Device device = thingsAdapter.getDevice(userID, params[0], false);
                        if (device != null) {
                            if (device.getType().equals(Device.VIRTUAL)) {
                                long value = 0;
                                boolean counterChange = false;
                                VirtualDevice vd = new VirtualDevice(device.getEUI());
                                try {
                                    value = (long) Double.parseDouble(params[2]); //to obcina część ułamkową.
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if ("incoming".equals(params[1]) && value != 0) {
                                    counterChange = true;
                                    virtualStackAdapter.get(vd).addInputs(value);
                                } else if ("exiting".equals(params[1]) && value != 0) {
                                    counterChange = true;
                                    virtualStackAdapter.get(vd).addOutputs(value);
                                } else {
                                    //virtua/lStackAdapter.get(device.getEUI())
                                    //TODO: ? reset?
                                }
                                if (counterChange && virtualStackAdapter.getProperty("write-interval").isEmpty()) {
                                    // empty write interval == write virtual device counters to the data channels
                                    DeviceIntegrationModule
                                            .getInstance()
                                            .writeVirtualState(vd, device, userID, thingsAdapter, virtualStackAdapter, scriptingAdapter, false, null);
                                } else if (counterChange && !virtualStackAdapter.getProperty("write-interval").isEmpty()) {
                                    //TODO: data will be send to DB by Scheduler
                                    //System.out.println("WRITE_INTERVAL is not empty");
                                } else {
                                    // data channels != (incoming || exiting) are stored immediately
                                    //System.out.println(">>>> writing data to virtual device");
                                    ArrayList<ChannelData> values = new ArrayList<>();
                                    ChannelData data = new ChannelData();
                                    data.setDeviceEUI(device.getEUI());
                                    data.setName(params[1]);
                                    data.setValue(params[2]);
                                    data.setTimestamp(Long.parseLong(params[3]));
                                    values.add(data);
                                    DeviceIntegrationModule.getInstance().writeVirtualData(thingsAdapter, scriptingAdapter, device, values);
                                    //thingsAdapter.putData(userID, device.getEUI(), data.getName(), data);
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
                default:
                    Kernel.handle(
                            Event.logWarning("Don't know how to handle category/type " + event.getCategory() + "/" + event.getType(),
                                    "" + event.getPayload())
                    );
            }
        }
    }

}
