/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.notification.EmailSenderIface;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.microsite.user.UserEvent;
import org.cricketmsf.out.log.LoggerAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class UserEventHandler {

    public static void handleEvent(
            Kernel kernel,
            Event event,
            UserAdapterIface userAdapter,
            LoggerAdapterIface gdprLogger,
            AuthAdapterIface authAdapter,
            ThingsDataIface thingsAdapter,
            DashboardAdapterIface dashboardAdapter,
            EmailSenderIface emailSender) {
        //TODO: all events should be send to the relevant queue
        switch (event.getType()) {
            case UserEvent.USER_REGISTERED:     //send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    long timeout = 1800 * 1000; //30 minut
                    gdprLogger.log(Event.logInfo(event.getId(), "REGISTERED USER " + user.getNumber()));
                    /*
                    Token token = new Token(user.getUid(), timeout, false); //TODO: this token is probably not used
                    token.setToken(user.getConfirmString());
                    database.put("tokens", user.getConfirmString(), token);
                     */
                    authAdapter.createConfirmationToken(uid, user.getConfirmString(), timeout);
                    emailSender.send(
                            user.getEmail(),
                            "Signomix registration confirmation",
                            "We received a request to sign up to Signomix Platform with this email address.<br>"
                            + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/confirm?key=" + user.getConfirmString() + "'>Click here to confirm your registration</a><br>"
                            + "If you received this email by mistake, simply delete it. You won't be registered if you don't click the confirmation link above."
                            + "<br><br>"
                            + "Otrzymaliśmy prośbę założenia konta na platformie Signomix z tym adresem email.<br>"
                            + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/confirm?key=" + user.getConfirmString() + "'>Kliknij tu w celu potwierdzenia swojej rejestracji</a><br>"
                            + "Jeżeli otrzymałeś ten email przez pomyłkę, po prostu go skasuj. Nie zostaniesz zarejestrowany jeśli nie klikniesz powyższego odnośnika."
                    );
                    emailSender.send((String) kernel.getProperties().getOrDefault("admin-notification-email", ""), "Signomix - registration", uid);
                } catch (Exception e) {
                    e.printStackTrace();
                    Kernel.handle(Event.logSevere(UserEventHandler.class.getSimpleName(), e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DEL_SHEDULED:   //send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    gdprLogger.log(Event.logInfo(event.getId(), "DELETE REQUEST FOR " + user.getNumber()));
                    emailSender.send(
                            user.getEmail(),
                            "Signomix unregistration confirmed",
                            "We received a request to remove your account from Signomix Platform with this email address.<br>"
                            + "Your account is locked now and all data related to your account will be deleted to the end of next work day.<br>"
                            + "If you received this email by mistake, you can contact our support before this date to stop unregistration procedure."
                            + "<br><br>"
                            + "Otrzymaliśmy prośbę usunięcia konta z platformy Signomix z tym adresem email.<br>"
                            + "Twoje konto zostało zablokowane i wszystkie dane z nim związane zostaną wykasowane z końcem następnego dnia roboczego.<br>"
                            + "Jeżeli otrzymałeś ten email przez pomyłkę, możesz skontaktować się z nami przed tą datą w celu odwołania procesu wyrejestrowania."
                    );
                    emailSender.send((String) kernel.getProperties().getOrDefault("admin-notification-email", ""), "signomix - unregister", uid);
                } catch (Exception e) {
                    e.printStackTrace();
                    kernel.handle(Event.logSevere(UserEventHandler.class.getSimpleName(), e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DELETED:        //TODO: authorization
                String[] tmpPayload = ((String) event.getPayload()).split(" ");
                gdprLogger.log(Event.logInfo(event.getId(), "DELETED USER " + tmpPayload[0] + " " + tmpPayload[1]));
                String uid = (String) event.getPayload();
                AlertModule.getInstance().removeAll(uid, thingsAdapter);
                DeviceManagementModule.getInstance().removeUserData(uid, thingsAdapter); //devices and channels
                DashboardBusinessLogic.getInstance().removeUserDashboards(uid, dashboardAdapter);
                break;
            case UserEvent.USER_RESET_PASSWORD:
                String payload = null;
                try {
                    payload = (String) event.getPayload();
                } catch (ClassCastException e) {
                }
                if (payload != null && !payload.isEmpty()) {
                    String[] params = payload.split(":");
                    if (params.length == 2) {
                        //TODO: email templates from CMS
                        String passResetLink = kernel.properties.getOrDefault("serviceurl", "") + "/app/?tid=" + params[0] + "#account";
                        emailSender.send(params[1], "Password Reset Request", "Click here to change password: <a href=\"" + passResetLink + "\">" + passResetLink + "</a>");
                    } else {
                        kernel.handle(Event.logWarning("UserEvent.USER_RESET_PASSWORD", "Malformed payload->" + payload));
                    }
                } else {
                    kernel.handle(Event.logWarning("UserEvent.USER_RESET_PASSWORD", "Malformed payload->" + payload));
                }
                gdprLogger.log(Event.logInfo(event.getId(), "RESET PASSWORD REQUESTED FOR " + event.getPayload()));
            case UserEvent.USER_REG_CONFIRMED:  //TODO: update user
                gdprLogger.log(Event.logInfo(event.getId(), "REGISTRATION CONFIRMED FOR " + event.getPayload()));
            case UserEvent.USER_UPDATED:
                gdprLogger.log(Event.logInfo(event.getId(), "USER DATA UPDATED FOR " + event.getPayload()));
                break;

            default:
                kernel.handleEvent(Event.logInfo(UserEventHandler.class.getSimpleName(), "Event recived: " + event.getType()));
                break;
        }
        
    }

}
