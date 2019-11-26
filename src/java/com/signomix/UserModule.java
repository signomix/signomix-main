/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.out.notification.NotificationIface;
import java.util.List;
import org.cricketmsf.microsite.user.*;
import java.util.Map;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class UserModule extends UserBusinessLogic {

    private static UserModule self;

    public static UserModule getInstance() {
        if (self == null) {
            self = new UserModule();
        }
        return self;
    }

    private boolean isAdmin(RequestObject request) {
        List<String> requesterRoles = request.headers.get("X-user-role");
        //String requesterRole = request.headers.getFirst("X-user-role");
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    @Override
    public Object handleGetRequest(Event event, UserAdapterIface userAdapter) {
        RequestObject request = event.getRequest();
        //handle(Event.logFinest(this.getClass().getSimpleName(), request.pathExt));
        String uid = request.pathExt;
        String requesterID = request.headers.getFirst("X-user-id");
        String requesterRole = request.headers.getFirst("X-user-role");

        StandardResult result = new StandardResult();
        try {
            if (uid.isEmpty() && "admin".equals(requesterRole)) {
                Map m = userAdapter.getAll();
                result.setData(m);
            } else if (uid.equals(requesterID) || "admin".equals(requesterRole)) {
                User u = (User) userAdapter.get(uid);
                result.setData(u);
            } else {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
            }

        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
        }
        return result;
    }

    public Object handleRegisterRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation, NotificationIface telegramNotifier) {
        //TODO: check requester rights
        //only admin can set: role or type differ than default (plus APPLICATION type)
        RequestObject request = event.getRequest();
        //handle(Event.logFinest(this.getClass().getSimpleName(), request.pathExt));
        //System.out.println("X-cms-user="+request.headers.getFirst("X-user-id"));
        StandardResult result = new StandardResult();
        String uid = request.pathExt;
        if (uid != null && !uid.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User newUser = new User();
            newUser.setUid(event.getRequestParameter("uid"));
            newUser.setEmail(event.getRequestParameter("email"));
            newUser.setType(User.FREE);
            newUser.setRole("");
            newUser.setPassword(HashMaker.md5Java(event.getRequestParameter("password")));
            String type = event.getRequestParameter("type");
            if (null != type) {
                switch (type.toUpperCase()) {
                    case "APPLICATION":
                        newUser.setType(User.APPLICATION);
                        break;
                    case "OWNER":
                        newUser.setType(User.OWNER);
                        break;
                    default:
                        newUser.setType(User.FREE);
                        break;
                }
            } else {
                newUser.setType(User.FREE);
            }
            newUser.setGeneralNotificationChannel(event.getRequestParameter("generalNotifications"));
            newUser.setInfoNotificationChannel(event.getRequestParameter("infoNotifications"));
            newUser.setWarningNotificationChannel(event.getRequestParameter("warningNotifications"));
            newUser.setAlertNotificationChannel(event.getRequestParameter("alertNotifications"));
            // validate
            boolean valid = true;
            if (!(newUser.getUid() != null && !newUser.getUid().isEmpty())) {
                valid = false;
            }
            if (!(newUser.getEmail() != null && !newUser.getEmail().isEmpty())) {
                valid = false;
            }
            if (!(newUser.getPassword() != null && !newUser.getPassword().isEmpty())) {
                valid = false;
            }
            if (!valid) {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage("lack of required parameters");
                return result;
            }
            //newUser = verifyNotificationsConfig(newUser, telegramNotifier);
            newUser = userAdapter.register(newUser);
            if (withConfirmation) {
                result.setCode(HttpAdapter.SC_ACCEPTED);
                //fire event to send "need confirmation" email
                UserEvent ev=new UserEvent(UserEvent.USER_REGISTERED, newUser.getUid());
                ev.setTimePoint("+5s");
                Kernel.getInstance().dispatchEvent(ev);
            } else {
                userAdapter.confirmRegistration(newUser.getUid());
                result.setCode(HttpAdapter.SC_CREATED);
                //fire event to send "welcome" email
                Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_REG_CONFIRMED, newUser.getNumber()));
            }
            result.setData(newUser.getUid());
        } catch (UserException e) {
            if (e.getCode() == UserException.USER_ALREADY_EXISTS) {
                result.setCode(HttpAdapter.SC_CONFLICT);
            } else {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
            result.setMessage(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public Object handleDeleteRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation) {
        //TODO: check requester rights
        //only admin can do this and user status must be IS_UNREGISTERING
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (uid == null) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User tmpUser = userAdapter.get(uid);
            userAdapter.remove(uid);
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_DELETED, tmpUser.getNumber() + " " + tmpUser.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(uid);
        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handleUpdateRequest(Event event, UserAdapterIface userAdapter, NotificationIface telegramNotifier) {
        //TODO: check requester rights
        //only admin can set: role or type differ than default
        RequestObject request = event.getRequest();
        boolean admin = isAdmin(request);
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User user = userAdapter.get(uid);
            if (user == null) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("user not found");
                return result;
            }
            String email = event.getRequestParameter("email");
            String type = event.getRequestParameter("type");
            String role = event.getRequestParameter("role");
            String password = event.getRequestParameter("password");
            String confirmed = event.getRequestParameter("confirmed");
            String generalNotifications = event.getRequestParameter("generalNotifications");
            String infoNotifications = event.getRequestParameter("infoNotifications");
            String warningNotifications = event.getRequestParameter("warningNotifications");
            String alertNotifications = event.getRequestParameter("alertNotifications");
            String unregisterRequested = event.getRequestParameter("unregisterRequested");
            if (email != null) {
                user.setEmail(email);
            }
            if (role != null && admin) {
                user.setRole(role);
            }
            if (type != null && admin) {
                try {
                    user.setType(Integer.parseInt(type));
                } catch (NumberFormatException e) {
                }
            }
            if (password != null) {
                user.setPassword(HashMaker.md5Java(event.getRequestParameter("password")));
            }
            if (confirmed != null) {
                user.setConfirmed("true".equalsIgnoreCase(confirmed));
                Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_REG_CONFIRMED, user.getNumber()));
            }
            if (generalNotifications != null) {
                user.setGeneralNotificationChannel(generalNotifications);
            }
            if (infoNotifications != null) {
                user.setInfoNotificationChannel(infoNotifications);
            }
            if (warningNotifications != null) {
                user.setWarningNotificationChannel(warningNotifications);
            }
            if (alertNotifications != null) {
                user.setAlertNotificationChannel(alertNotifications);
            }
            if (unregisterRequested != null) {
                //is this new request?
                if (!user.isUnregisterRequested() && "true".equalsIgnoreCase(unregisterRequested)) {
                    //fire event
                    Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_DEL_SHEDULED, user.getUid()));
                    user.setStatus(User.IS_UNREGISTERING);
                }
                user.setUnregisterRequested("true".equalsIgnoreCase(unregisterRequested));
            }
            user = verifyNotificationsConfig(user, telegramNotifier);
            userAdapter.modify(user);
            //fire event
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_UPDATED, user.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(user);
        } catch (NullPointerException | UserException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object refillAccount(String userName, Double amount) {
        return null;
    }

    /**
     * Increases reads counter by number
     *
     * @param userName
     * @param number
     * @return
     */
    public Object addReadings(String userName, long number) {
        return null;
    }

    /**
     * Increases writes counter by number
     *
     * @param userName
     * @param number
     * @return
     */
    public Object addWritings(String userName, long number) {
        return null;
    }

    /**
     * Sets reads and writes counters to zero
     *
     * @param userName
     * @return
     */
    public Object resetCounters(String userName) {
        return null;
    }

    public User verifyNotificationsConfig(User user, NotificationIface telegramNotifier) {
        //String chatID = telegramNotifier.getChatID(recipent);
        String telegramUserID;
        String telegramChatID;
        if (null == user.getGeneralNotificationChannel()) {
            return user;
        }
        if (user.getGeneralNotificationChannel().startsWith("TELEGRAM:")) {
            telegramUserID = getTelegramUserID(user.getGeneralNotificationChannel());
            telegramChatID = telegramNotifier.getChatID(telegramUserID);
            if (null != telegramChatID) {
                user.setGeneralNotificationChannel("TELEGRAM:" + telegramUserID + "@" + telegramChatID);
            } else {
                user.setGeneralNotificationChannel("SIGNOMIX:");
            }
        }
        if (user.getInfoNotificationChannel().startsWith("TELEGRAM:")) {
            telegramUserID = getTelegramUserID(user.getInfoNotificationChannel());
            telegramChatID = telegramNotifier.getChatID(telegramUserID);
            if (null != telegramChatID) {
                user.setInfoNotificationChannel("TELEGRAM:" + telegramUserID + "@" + telegramChatID);
            } else {
                user.setInfoNotificationChannel("SIGNOMIX:");
            }
        }
        if (user.getWarningNotificationChannel().startsWith("TELEGRAM:")) {
            telegramUserID = getTelegramUserID(user.getWarningNotificationChannel());
            telegramChatID = telegramNotifier.getChatID(telegramUserID);
            if (null != telegramChatID) {
                user.setWarningNotificationChannel("TELEGRAM:" + telegramUserID + "@" + telegramChatID);
            } else {
                user.setWarningNotificationChannel("SIGNOMIX:");
            }
        }
        if (user.getAlertNotificationChannel().startsWith("TELEGRAM:")) {
            telegramUserID = getTelegramUserID(user.getAlertNotificationChannel());
            telegramChatID = telegramNotifier.getChatID(telegramUserID);
            if (null != telegramChatID) {
                user.setAlertNotificationChannel("TELEGRAM:" + telegramUserID + "@" + telegramChatID);
            } else {
                user.setAlertNotificationChannel("SIGNOMIX:");
            }
        }
        return user;
    }

    private String getTelegramUserID(String config) {
        String telegramUserID = config.substring(config.indexOf(":") + 1);
        if (telegramUserID.indexOf("@") > 0) {
            telegramUserID = telegramUserID.substring(0, telegramUserID.indexOf("@"));
        }
        return telegramUserID;
    }
}
