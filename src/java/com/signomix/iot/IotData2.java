/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class IotData2 implements IotDataIface {

    public String applicationID;
    public String dev_eui;
    public String gateway_eui;
    public String time;
    public ArrayList<Map> payload_fields;
    public String timestamp;

    @Override
    public String getDeviceEUI() {
        return dev_eui;
    }

    @Override
    public String[] getPayloadFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instant getTimeField() {
        try {
            return Instant.parse(time);
        } catch (NullPointerException | DateTimeParseException e) {
            //e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getTimestamp() {
        long t;
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            if(null!=getTimeField()){
                t=getTimeField().toEpochMilli();
            }else{
                t=0;
            }
        }
        return t;
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the applicationID
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * @param applicationID the applicationID to set
     */
    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    @Override
    public void normalize() {
        if (this.gateway_eui != null) {
            this.gateway_eui = this.gateway_eui.toUpperCase();
        }
        if (this.dev_eui != null) {
            this.dev_eui = this.dev_eui.toUpperCase();
        }
        // Data channel names should be lowercase. We can fix user mistakes here.
        HashMap<String, Object> tempMap;
        for (int i = 0; i < payload_fields.size(); i++) {
            tempMap = new HashMap<>();
            tempMap.put("name", ((String) payload_fields.get(i).get("name")).toLowerCase());
            try {
                tempMap.put("value", (Double) payload_fields.get(i).get("value"));
            } catch (ClassCastException e) {
                tempMap.put("value", (String) payload_fields.get(i).get("value"));
            }
            payload_fields.set(i, tempMap);
        }
    }

    @Override
    public String getPayload() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
