/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.event;

import org.cricketmsf.microsite.user.UserEvent;

/**
 *
 * @author greg
 */
public class SignomixUserEvent extends UserEvent {

    public static final String USER_SMS_SENT = "USER_SMS_SENT";
    public static final String SUBSCRIBER_REGISTERED = "SUBSCRIBER_REG_SCHEDULED";
    public static final String SUBSCRIBER_REG_CONFIRMED = "SUBSCRIBER_REGISTERED";

    
    public SignomixUserEvent(String type, String payload){
        super(type,payload);
    }
    
}
