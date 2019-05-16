/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot;

import org.cricketmsf.Event;
import static org.cricketmsf.microsite.user.UserEvent.CATEGORY_USER;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class IotEvent extends Event {

    public static final String CATEGORY_IOT = "IOT";

    public static final String GENERAL = "GENERAL";

    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ALERT = "ALERT";
    
    public static final String VIRTUAL_DATA = "VIRTUAL_DATA";
    public static final String ACTUATOR_CMD = "ACTUATOR_CMD";

    public static final String CHANNEL_REMOVE = "CHANNEL_REMOVE";
    public static final String DEVICE_REGISTERED = "DEVICE_REGISTERED";
    public static final String DEVICE_REMOVED = "DEVICE_REMOVED";
    public static final String DASHBOARD_SHARED = "DASHBOARD_SHARED";
    public static final String DASHBOARD_REMOVED = "DASHBOARD_REMOVED";
    public static final String DASHBOARD_UNSHARED = "DASHBOARD_UNSHARED";
    public static final String DEVICE_LOST = "DEVICE_LOST";
    
    public static final String GROUP_CREATED = "GROUP_CREATED";
    public static final String GROUP_REMOVED = "GROUP_REMOVED";
    
    public static final String PLATFORM_DEVICE_LIMIT_EXCEEDED = "PLATFORM_DEVICE_LIMIT_EXCEEDED";
    

    public IotEvent() {
        //super("", "", "", null, null); //TODO: cannot use super() because of NPE
        setCategory(CATEGORY_IOT);
        setType(GENERAL);
    }
    
    public IotEvent(String type, Object origin, String payload) {
        setCategory(CATEGORY_IOT);
        setType(type);
        setOrigin(origin.getClass().getSimpleName());
        setPayload(payload);
    }

    public IotEvent(String type, String payload) {
        //super(origin, "", type, null, payload); //TODO: cannot use super() because of NPE
        setCategory(CATEGORY_IOT);
        setOrigin(null);
        setType(type);
        setPayload(payload);
        setCategory(CATEGORY_IOT);
        switch (type.toUpperCase()) {
            case "ALERT":
                setAlertMessage(payload);
                break;
            case "WARNING":
                setWarningMessage(payload);
                break;
            case "INFO":
                setInfoMessage(payload);
                break;
            case "CHANNEL_REMOVE":
                setType(CHANNEL_REMOVE);
                setPayload(payload);
                break;
            case "DEVICE_REGISTERED":
                setType(DEVICE_REGISTERED);
                setPayload(payload);
                break;
            case "DEVICE_REMOVED":
                setType(DEVICE_REMOVED);
                setPayload(payload);
                break;
            case "DASHBOARD_REMOVED":
                setType(DASHBOARD_REMOVED);
                setPayload(payload);
                break;
            case "DASHBOARD_UNSHARED":
                setType(DASHBOARD_UNSHARED);
                setPayload(payload);
                break;
            case "VIRTUAL_DATA":
                setType(VIRTUAL_DATA);
                setPayload(payload);
                break;
            default:
                setGeneralMessage(payload);
        }
    }

    public void setGeneralMessage(String message) {
        setType(GENERAL);
        setPayload(message);
    }

    public void setInfoMessage(String message) {
        setType(INFO);
        setPayload(message);
    }

    public void setWarningMessage(String message) {
        setType(WARNING);
        setPayload(message);
    }

    public void setAlertMessage(String message) {
        setType(ALERT);
        setPayload(message);
    }
    
    public IotEvent addOrigin(String origin){
        setOrigin(origin);
        return this;
    }

    public IotEvent addPayload(String payload) {
        setPayload(payload);
        return this;
    }

    public IotEvent addType(String type) {
        setType(type);
        return this;
    }
    
    @Override
    public String[] getCategories(){
        String[] categories = {CATEGORY_IOT};
        return categories;
    }

}
