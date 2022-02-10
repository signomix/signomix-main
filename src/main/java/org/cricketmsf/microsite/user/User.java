/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package org.cricketmsf.microsite.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.signomix.event.IotEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author greg
 */
public class User {

    /**
     * @return the generalNotificationChannel
     */
    public String getGeneralNotificationChannel() {
        return generalNotificationChannel;
    }

    /**
     * @return the infoNotificationChannel
     */
    public String getInfoNotificationChannel() {
        return infoNotificationChannel;
    }

    /**
     * @return the warningNotificationChannel
     */
    public String getWarningNotificationChannel() {
        return warningNotificationChannel;
    }

    /**
     * @return the alertNotificationChannel
     */
    public String getAlertNotificationChannel() {
        return alertNotificationChannel;
    }

    public static final int USER = 0; // default type, standard user
    public static final int OWNER = 1; // owner, admin
    public static final int APPLICATION = 2; // application
    public static final int DEMO = 3;
    public static final int FREE = 4; // registered, free account
    public static final int PRIMARY = 5; // primary account
    public static final int READONLY = 6;
    public static final int EXTENDED = 7; // students, scientists, nonprofits
    public static final int SUPERUSER = 8;

    public static final int SUBSCRIBER = 100;

    public static final int IS_REGISTERING = 0;
    public static final int IS_ACTIVE = 1;
    public static final int IS_UNREGISTERING = 2;
    public static final int IS_LOCKED = 3;

    public static final int SERVICE_SMS = 0b00000001;
    public static final int SERVICE_NEW = 0b00000010; //not used

    private Integer type = FREE;
    private String uid;
    private String email;
    private String name;
    private String surname;
    private String role;
    private Boolean confirmed;
    private Boolean unregisterRequested;
    private String confirmString;
    private String password;
    private String generalNotificationChannel = "";
    private String infoNotificationChannel = "";
    private String warningNotificationChannel = "";
    private String alertNotificationChannel = "";
    private Integer authStatus;
    private long createdAt;
    private long number;
    private int services;
    private String phonePrefix;
    private long credits;
    private boolean autologin;
    private String preferredLanguage;

    public User() {
        confirmed = false;
        unregisterRequested = false;
        authStatus = IS_REGISTERING;
        createdAt = System.currentTimeMillis();
        services = 0b0;
        credits = 0;
        autologin = false;
    }

    public void clearStatus() {
        confirmed = null;
        unregisterRequested = null;
        authStatus = null;
        type = null;
    }

    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * @return the confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
        if (this.confirmed) {
            this.setStatus(IS_ACTIVE);
        } else {
            this.setStatus(IS_REGISTERING);
        }
    }

    /**
     * @return the waitingForUnregister
     */
    public boolean isUnregisterRequested() {
        return unregisterRequested;
    }

    /**
     * @param unregisterRequested the waitingForUnregister to set
     */
    public void setUnregisterRequested(boolean unregisterRequested) {
        this.unregisterRequested = unregisterRequested;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * newUser.setType(User.OWNER);
     *
     * @return the confirmString
     */
    public String getConfirmString() {
        return confirmString;
    }

    /**
     * @param confirmString the confirmString to set
     */
    public void setConfirmString(String confirmString) {
        this.confirmString = confirmString;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    @JsonIgnore
    public List getRoles() {
        return Arrays.asList(getRole().split(","));
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role.toLowerCase();
    }

    /**
     * @return the password
     */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
        //this.password = password;
    }

    public boolean checkPassword(String passToCheck) {
        return getPassword() != null && getPassword().equals(HashMaker.md5Java(passToCheck));
    }

    public String[] getChannelConfig(String eventTypeName) {
        String channel = "";
        switch (eventTypeName.toUpperCase()) {
            case IotEvent.GENERAL:
            case IotEvent.DEVICE_LOST:
                channel = getGeneralNotificationChannel();
                break;
            case IotEvent.INFO:
                channel = getInfoNotificationChannel();
                break;
            case IotEvent.WARNING:
                channel = getWarningNotificationChannel();
                break;
            case IotEvent.ALERT:
                channel = getAlertNotificationChannel();
                break;
        }
        if (channel == null) {
            channel = "";
        }
        return channel.split(":");
    }

    /**
     * @param generalNotificationChannel the generalNotificationChannel to set
     */
    public void setGeneralNotificationChannel(String generalNotificationChannel) {
        this.generalNotificationChannel = generalNotificationChannel;
    }

    /**
     * @param infoNotificationChannel the infoNotificationChannel to set
     */
    public void setInfoNotificationChannel(String infoNotificationChannel) {
        this.infoNotificationChannel = infoNotificationChannel;
    }

    /**
     * @param warningNotificationChannel the warningNotificationChannel to set
     */
    public void setWarningNotificationChannel(String warningNotificationChannel) {
        this.warningNotificationChannel = warningNotificationChannel;
    }

    /**
     * @param alertNotificationChannel the alertNotificationChannel to set
     */
    public void setAlertNotificationChannel(String alertNotificationChannel) {
        this.alertNotificationChannel = alertNotificationChannel;
    }

    /**
     * @return the status
     */
    @JsonIgnore
    public int getStatus() {
        return authStatus;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.authStatus = status;
    }
    
    public int getAuthStatus() {
        return authStatus;
    }

    /**
     * @param status the status to set
     */
    public void setAuthStatus(int status) {
        this.authStatus = status;
    }

    /**
     * @return the createdAt
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the number
     */
    public long getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(long number) {
        this.number = number;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the services
     */
    public int getServices() {
        return services;
    }

    /**
     * @param services the services to set
     */
    public void setServices(int services) {
        this.services = services;
    }

    public void addService(int newService) {
        services = services | newService;
    }

    public void removeService(int newService) {
        services = services ^ newService;
    }

    /**
     * @return the phonePrefix
     */
    public String getPhonePrefix() {
        return phonePrefix;
    }

    /**
     * @param phonePrefix the phonePrefix to set
     */
    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    /**
     * @return the credits
     */
    public long getCredits() {
        return credits;
    }

    /**
     * @param credits the credits to set
     */
    public void setCredits(long credits) {
        this.credits = credits;
        if (this.credits < 0) {
            this.credits = 0;
        }
    }

    /**
     * @return the autologin
     */
    public boolean isAutologin() {
        return autologin;
    }

    /**
     * @param autologin the autologin to set
     */
    public void setAutologin(boolean autologin) {
        this.autologin = autologin;
    }

    /**
     * @return the preferredLanguage
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * @param preferredLanguage the preferredLanguage to set
     */
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
