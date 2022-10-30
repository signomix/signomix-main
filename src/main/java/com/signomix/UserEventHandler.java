/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

//import com.signomix.out.notification.EmailSenderIface;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.microsite.user.UserEvent;
import org.cricketmsf.out.log.LoggerAdapterIface;

import com.signomix.event.SignomixUserEvent;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.mailing.MailingIface;
import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.MessageEnvelope;

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
            DashboardAdapterIface dashboardAdapter) {
        // TODO: all events should be send to the relevant queue
        switch (event.getType()) {
            case UserEvent.USER_REGISTERED: // send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    long timeout = 1800 * 1000; // 30 minut
                    gdprLogger.log(Event.logInfo(event.getId(), "REGISTERED USER " + user.getNumber()));
                    authAdapter.createConfirmationToken(uid, user.getConfirmString(), timeout);
                    sendConfirmationEmail(user, kernel);
                } catch (Exception e) {
                    e.printStackTrace();
                    Kernel.handle(Event.logSevere(UserEventHandler.class.getSimpleName(),
                            e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case SignomixUserEvent.SUBSCRIBER_REGISTERED: // send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    long timeout = 1800 * 1000; // 30 minut
                    gdprLogger.log(Event.logInfo(event.getId(), "SUBSCRIBTION FOR USER " + user.getNumber()));
                    authAdapter.createConfirmationToken(uid, user.getConfirmString(), timeout);
                    sendSubscriptionConfirmEmail(user, kernel);
                } catch (Exception e) {
                    e.printStackTrace();
                    Kernel.handle(Event.logSevere(UserEventHandler.class.getSimpleName(),
                            e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DEL_SHEDULED: // send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    gdprLogger.log(Event.logInfo(event.getId(), "DELETE REQUEST FOR " + user.getNumber()));
                    sendDeleteRequestedEmail(user, kernel);
                } catch (Exception e) {
                    e.printStackTrace();
                    kernel.handle(Event.logSevere(UserEventHandler.class.getSimpleName(),
                            e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DELETED: // TODO: authorization
                String[] tmpPayload = ((String) event.getPayload()).split(" ");
                gdprLogger.log(Event.logInfo(event.getId(), "DELETED USER " + tmpPayload[0] + " " + tmpPayload[1]));
                String uid = (String) event.getPayload();
                AlertModule.getInstance().removeAll(uid, thingsAdapter);
                ((Service)Kernel.getInstance()).deviceLogic.removeUserData(uid, thingsAdapter); // devices and channels
                //DeviceManagementModule.getInstance().removeUserData(uid, thingsAdapter); // devices and channels
                DashboardBusinessLogic.getInstance().removeUserDashboards(uid, dashboardAdapter);
                break;
            case UserEvent.USER_RESET_PASSWORD:
                sendPasswordResetLink(event, kernel);
                gdprLogger.log(Event.logInfo(event.getId(), "RESET PASSWORD REQUESTED FOR " + event.getPayload()));
                break;
            case UserEvent.USER_REG_CONFIRMED: // TODO: update user
                sendRegistrationConfirmedEmail(event, kernel, userAdapter);
                gdprLogger.log(Event.logInfo(event.getId(), "REGISTRATION CONFIRMED FOR " + event.getPayload()));
                break;
            case SignomixUserEvent.SUBSCRIBER_REG_CONFIRMED: // TODO: update user
                gdprLogger.log(Event.logInfo(event.getId(), "SUBSCRIPTION CONFIRMED FOR " + event.getPayload()));
                break;
            case UserEvent.USER_UPDATED:
                gdprLogger.log(Event.logInfo(event.getId(), "USER DATA UPDATED FOR " + event.getPayload()));
                break;
            case SignomixUserEvent.USER_SMS_SENT:
                try {
                    String userid = (String) event.getPayload();
                    User user = userAdapter.get(userid);
                    user.setCredits(user.getCredits() - 1);
                    userAdapter.modify(user);
                } catch (UserException e) {
                    Kernel.getInstance().dispatchEvent(Event.logWarning("UserEvent.USER_SMS_SENT", e.getMessage()));
                }
                break;
            default:
                kernel.handleEvent(
                        Event.logInfo(UserEventHandler.class.getSimpleName(), "Event recived: " + event.getType()));
                break;
        }

    }

    private static void sendConfirmationEmail(User user, Kernel kernel) {
        MessageBrokerIface externalNotificator = ((Service) kernel).messageBroker;
        if (null != externalNotificator) {
            MessageEnvelope message1 = new MessageEnvelope();
            message1.message = user.getUid();
            message1.subject = "Signomix - registration";
            message1.type = MessageEnvelope.ADMIN_EMAIL;
            User admin = new User();
            admin.setEmail((String) kernel.getProperties().getOrDefault("admin-notification-email", ""));
            message1.user = admin;
            externalNotificator.send(message1);
            MessageEnvelope message2 = new MessageEnvelope();
            message2.subject = "Signomix registration confirmation";
            message2.message = "We received a request to sign up to Signomix Platform with this email address.<br>"
                    + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/confirm?key="
                    + user.getConfirmString() + "'>Click here to confirm your registration</a><br>"
                    + "If you received this email by mistake, simply delete it. You won't be registered if you don't click the confirmation link above."
                    + "<br><br>"
                    + "Otrzymaliśmy prośbę założenia konta na platformie Signomix z tym adresem email.<br>"
                    + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/confirm?key="
                    + user.getConfirmString() + "'>Kliknij tu w celu potwierdzenia swojej rejestracji</a><br>"
                    + "Jeżeli otrzymałeś ten email przez pomyłkę, po prostu go skasuj. Nie zostaniesz zarejestrowany jeśli nie klikniesz powyższego odnośnika.";
            message2.type = MessageEnvelope.DIRECT_EMAIL;
            message2.user = user;
            externalNotificator.send(message2);
        }
    }

    private static void sendRegistrationConfirmedEmail(Event event, Kernel kernel, UserAdapterIface userAdapter) {
        MessageBrokerIface externalNotificator = ((Service) kernel).messageBroker;
        if (null == externalNotificator) {
            return;
        }
        MailingIface mailingAdapter = ((Service) kernel).mailingAdapter;
        CmsIface cmsAdapter = ((Service) kernel).cms;
        Long userNumber = (Long) event.getPayload();
        try {
            User user = userAdapter.getByNumber(userNumber);
            mailingAdapter.sendWelcomeDocument(user, cmsAdapter, externalNotificator);
        } catch (UserException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning("User not found", ex.getMessage()));
        }
    }

    private static void sendSubscriptionConfirmEmail(User user, Kernel kernel) {
        MessageBrokerIface externalNotificator = ((Service) kernel).messageBroker;
        if (null != externalNotificator) {
            MessageEnvelope message1 = new MessageEnvelope();
            message1.message = user.getUid();
            message1.subject = "Signomix newsletter subscription";
            message1.type = MessageEnvelope.ADMIN_EMAIL;
            User admin = new User();
            admin.setEmail((String) kernel.getProperties().getOrDefault("admin-notification-email", ""));
            message1.user = admin;
            externalNotificator.send(message1);

            MessageEnvelope message2 = new MessageEnvelope();
            message2.subject = "Newsletter subscription confirmation";
            message2.message = "<p>Confirm subscription by clicking on the link below.<br>"
                    + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/subsconfirm?key="
                    + user.getConfirmString() + "'>I confirm my subscription</a><br>"
                    + "If your subscription is not from you, simply delete this e-mail. An unconfirmed subscription will be deleted."
                    + "</p>"
                    + "<p>Potwierdź subskrybcję klikając na link poniżej.<br>"
                    + "<a href='" + kernel.getProperties().get("serviceurl") + "/api/subsconfirm?key="
                    + user.getConfirmString() + "'>Potwierdzam subskrypcję</a><br>"
                    + "Jeżeli zgłoszenie nie pochodzi od Ciebie, po prostu skasuj ten e-mail. Niepotwierdzona subskrypcja zostanie usunięta.</p>";
            message2.type = MessageEnvelope.DIRECT_EMAIL;
            message2.user = user;
            externalNotificator.send(message2);
        }
    }

    private static void sendDeleteRequestedEmail(User user, Kernel kernel) {
        MessageBrokerIface externalNotificator = ((Service) kernel).messageBroker;
        if (null != externalNotificator) {
            MessageEnvelope message1 = new MessageEnvelope();
            message1.message = user.getUid();
            message1.subject = "signomix - unregister";
            message1.type = MessageEnvelope.ADMIN_EMAIL;
            User admin = new User();
            admin.setEmail((String) kernel.getProperties().getOrDefault("admin-notification-email", ""));
            message1.user = admin;
            externalNotificator.send(message1);

            MessageEnvelope message2 = new MessageEnvelope();
            message2.subject = "Signomix unregistration confirmed";
            message2.message = "We received a request to remove your account from Signomix Platform with this email address.<br>"
                    + "Your account is locked now and all data related to your account will be deleted to the end of next work day.<br>"
                    + "If you received this email by mistake, you can contact our support before this date to stop unregistration procedure."
                    + "<br><br>"
                    + "Otrzymaliśmy prośbę usunięcia konta z platformy Signomix z tym adresem email.<br>"
                    + "Twoje konto zostało zablokowane i wszystkie dane z nim związane zostaną wykasowane z końcem następnego dnia roboczego.<br>"
                    + "Jeżeli otrzymałeś ten email przez pomyłkę, możesz skontaktować się z nami przed tą datą w celu odwołania procesu wyrejestrowania.";
            message2.type = MessageEnvelope.DIRECT_EMAIL;
            message2.user = user;
            externalNotificator.send(message2);
        }
    }

    private static void sendPasswordResetLink(Event event, Kernel kernel) {
        String payload = null;
        try {
            payload = (String) event.getPayload();
        } catch (ClassCastException e) {
        }
        if (null == payload || payload.isEmpty()) {
            return;
        }
        String[] params = payload.split(":");
        if (params.length != 2) {
            kernel.handle(
                    Event.logWarning("UserEvent.USER_RESET_PASSWORD", "Malformed payload->" + payload));
            return;
        }
        MessageBrokerIface externalNotificator = ((Service) kernel).messageBroker;
        if (null == externalNotificator) {
            return;
        }
        // TODO: email templates from CMS
        String passResetLink = kernel.properties.getOrDefault("serviceurl", "") + "/app/?tid="
                + params[0] + "#!account";
        MessageEnvelope message2 = new MessageEnvelope();
        message2.subject = "Password Reset Request";
        message2.message = "Click here to change password: <a href=\"" + passResetLink + "\">"
                + passResetLink + "</a>";
        message2.type = MessageEnvelope.DIRECT_EMAIL;
        User tmpUser = new User();
        tmpUser.setEmail(params[1]);
        message2.user = tmpUser;
        externalNotificator.send(message2);
    }
}
