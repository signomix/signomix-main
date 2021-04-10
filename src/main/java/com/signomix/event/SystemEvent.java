/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.event;

import org.cricketmsf.Event;
import static org.cricketmsf.microsite.user.UserEvent.CATEGORY_USER;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SystemEvent extends Event {

    public static final String CATEGORY_SYSTEM = "SYSTEM";
    public static final String GENERAL = "GENERAL";
    public static final String MONITORING = "MONITORING";
    public static final String USER = "USER";
    
    

    public SystemEvent() {
        setCategory(CATEGORY_SYSTEM);
        setType(GENERAL);
    }
    
    public SystemEvent(String type, Object origin, String payload) {
        setCategory(CATEGORY_SYSTEM);
        setType(type);
        setOrigin(origin.getClass().getSimpleName());
        setPayload(payload);
    }

    public SystemEvent(String type, String payload) {
        setCategory(CATEGORY_SYSTEM);
        setOrigin(null);
        setType(type);
        setPayload(payload);
    }
    public SystemEvent(String payload) {
        setCategory(CATEGORY_SYSTEM);
        setType(GENERAL);
        setOrigin(null);
        setPayload(payload);
    }

    public SystemEvent addOrigin(String origin){
        setOrigin(origin);
        return this;
    }

    public SystemEvent addPayload(String payload) {
        setPayload(payload);
        return this;
    }

    public SystemEvent addType(String type) {
        setType(type);
        return this;
    }
    
    @Override
    public String[] getCategories(){
        String[] categories = {CATEGORY_SYSTEM};
        return categories;
    }

}
