/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.out.db.IotDatabaseIface;
import com.signomix.out.db.IotDbDataIface;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ActuatorDataIface;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.MessageEnvelope;
//import com.signomix.out.notification.EmailSenderIface;
import com.signomix.out.script.ScriptingAdapterIface;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.notification.EmailSenderIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SystemEventHandler {

    public static void handleEvent(
            Kernel kernel,
            Event event,
            KeyValueDBIface database,
            KeyValueDBIface cmsDatabase,
            UserAdapterIface userAdapter,
            KeyValueDBIface userDB,
            AuthAdapterIface authAdapter,
            KeyValueDBIface authDB,
            ActuatorDataIface actuatorAdapter,
            /* ActuatorCommandsDBIface actuatorCommandsDB, */
            ThingsDataIface thingsAdapter,
            /*
             * IotDatabaseIface thingsDB,
             * IotDataStorageIface iotDataDB,
             */
            DashboardAdapterIface dashboardAdapter,
            ScriptingAdapterIface scriptingAdapter,
            MessageBrokerIface externalNotificator,
            IotDbDataIface iotDB) {

        /*
         * if (event.getTimePoint() != null) {
         * scheduler.handleEvent(event);
         * return;
         * }
         */
        switch (event.getType()) {
            case "SHUTDOWN":
                kernel.shutdown();
                break;
            case "EMAIL_ADMIN_STARTUP":
                // ExternalNotificatorIface externalNotificator = ((Service)
                // kernel).externalNotificator;
                String subject = "Signomix - started";
                String text = "" + event.getPayload();
                if (null != externalNotificator) {
                    MessageEnvelope message = new MessageEnvelope();
                    message.message = text;
                    message.subject = subject;
                    message.type = MessageEnvelope.ADMIN_EMAIL;
                    User user = new User();
                    user.setEmail((String) kernel.getProperties().getOrDefault("admin-notification-email", ""));
                    message.user = user;
                    externalNotificator.send(message);
                }
                break;
            case "CLEAR_DATA":
                try {
                    String payload = (String) event.getPayload();
                    String[] params = payload.split("|");
                    String dataCategory = "";
                    String userType = "";
                    if (params != null && params.length > 0) {
                        dataCategory = params[0];
                        if (params.length > 1) {
                            userType = params[1];
                        }
                    }
                    boolean demoMode = kernel.getName().toUpperCase().contains("DEMO");
                    PlatformAdministrationModule.getInstance().clearData(
                            demoMode, dataCategory, userType, userAdapter, thingsAdapter, authAdapter, authDB,
                            dashboardAdapter,
                            actuatorAdapter);
                } catch (ClassCastException | IndexOutOfBoundsException ex) {
                    kernel.handleEvent(Event.logWarning(SystemEventHandler.class,
                            "Problem with clearing data parameters- " + ex.getMessage()));
                }
            case "CONTENT":
                try {
                    database.clear("webcache_pl");
                } catch (KeyValueDBException ex) {
                    kernel.dispatchEvent(Event.logWarning(SystemEventHandler.class,
                            "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_en");
                } catch (KeyValueDBException ex) {
                    kernel.dispatchEvent(Event.logWarning(SystemEventHandler.class,
                            "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_fr");
                } catch (KeyValueDBException ex) {
                    kernel.dispatchEvent(Event.logWarning(SystemEventHandler.class,
                            "Problem while clearing web cache - " + ex.getMessage()));
                }
                break;
            // case "MAILING_SEND":
            // MailingModule.getInstance().sendMailing((Long)event.getPayload());
            // break;
            case "STATUS":
                System.out.println(kernel.printStatus());
                break;
            // case "COMMAND":
            // ActuatorModule.getInstance().processCommand(event, actuatorCommandsDB,
            // virtualStackAdapter, thingsAdapter, scriptingAdapter);
            // break;
            case "BACKUP":
                PlatformAdministrationModule
                        .getInstance()
                        .backupDatabases(database, userDB, authDB, cmsDatabase,
                                /* thingsDB, iotDataDB, actuatorCommandsDB, */ iotDB);
                break;
            case "CHECK_DEVICES":
                DeviceManagementModule.getInstance().checkStatus(thingsAdapter);
                break;
            default:
                kernel.handleEvent(Event.logWarning("Don't know how to handle type " + event.getType(),
                        event.getPayload().toString()));
        }

    }

}
