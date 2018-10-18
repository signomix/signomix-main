/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import org.cricketmsf.Event;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Alert extends Event {
    
    private String userID = null;
    private String deviceEUI = null;
    
    public Alert(){
        super();
    }
    
    public Alert(Event event){
        setCalculatedTimePoint(event.getCalculatedTimePoint());
        setCategory(event.getCategory());
        setCreatedAt(event.getCreatedAt());
        setOrigin(event.getOrigin());
        setPayload(event.getPayload());
        setRootEventId(event.getRootEventId());
        setServiceId(event.getServiceId());
        setTimePoint(event.getTimePoint());
        setType(event.getType());
    }
    
    @Override
    public void setOrigin(String origin){
        super.setOrigin(origin);
        int pos = getOrigin().indexOf("\t");
        if (pos > -1) {
            userID=getOrigin().substring(0, pos);
            deviceEUI=getOrigin().substring(pos + 1);
        }
    }

    public String getUserID() {
        return userID;
    }

    public String getDeviceEUI() {
        return deviceEUI;
    }

}
