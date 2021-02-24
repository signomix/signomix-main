/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class TtnData extends HashMap implements Map, IotDataIface {

    private String[] fieldNames = null;
    
    public TtnData(){
        super();
    }
    
    public TtnData(TtnData source){
        source.keySet().forEach(key->{this.put(key, source.get(key));});
    }
    
    public void putField(String key, Object value){
        getPayloadFields().put(key, value);
    }
    
    public void removeField(String key){
        getPayloadFields().remove(key);
    }

    public String getApplicationId() {
        return (String) get("app_id");
    }

    public String getDeviceEUI() {
        return (String) get("hardware_serial");
    }

    @Override
    public Instant getTimeField() {
        try {
            return Instant.parse(getMetadata("time"));
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDownlink() {
        return (String) get("downlink_url");
    }

    public String getPayload() {
        return (String) get("payload_raw");
    }

    @Override
    public String[] getPayloadFieldNames() {
        if (fieldNames == null) {
            Object[] a;
            try {
                a = getPayloadFields().keySet().toArray();
            } catch (NullPointerException e) {
                return null;
            }
            fieldNames = new String[a.length];
            for (int i = 0; i < a.length; i++) {
                fieldNames[i] = (String) a[i];
            }
        }
        return fieldNames;
    }

    public HashMap getPayloadFields() {
        return (HashMap) get("payload_fields");
    }

    public HashMap getMetadata() {
        return (HashMap) get("metadata");
    }

    public String getMetadata(String fieldName) {
        return ""+getMetadata().get(fieldName);
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        Double value = null;
        try {
            value = new Double((Long) getPayloadFields().get(fieldName));
            return value;
        } catch (Exception e) {
        }
        try {
            value = (Double) getPayloadFields().get(fieldName);
            return value;
        } catch (Exception e) {
        }
        try{
            // when the field is received as String
            value=Double.parseDouble((String)getPayloadFields().get(fieldName));
            return value;
        }catch(Exception e){
        }
        return value;
    }

    @Override
    public long getTimestamp() {
        long t;
        try {
            t = (Long) get("timestamp");
        } catch (Exception e) {
            t = getTimeField().toEpochMilli();
        }
        return t;
    }

    public long getFrameCounter() {
        return (Long) get("counter");
    }

    @Override
    public void normalize() {
        try {
            this.put("hardware_serial", ((String) get("hardware_serial")).toUpperCase());
        } catch (NullPointerException e) {
            //do nothing
        }
        //TODO: gateways?
    }

    @Override
    public long getReceivedPackageTimestamp() {
        return getTimeField().toEpochMilli();
    }

    @Override
    public String getDeviceID() {
        return (String) get("dev_id");
    }

    @Override
    public String getStringValue(String fieldName) {
        return ""+getPayloadFields().get(fieldName);
    }

    @Override
    public Double getLatitude() {
        try {
            return Double.parseDouble(getMetadata("latitude"));
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        return null;
    }

    @Override
    public Double getLongitude() {
        try {
            return Double.parseDouble(getMetadata("longitude"));
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        return null;
    }

    @Override
    public Double getAltitude() {
        try {
            return Double.parseDouble(getMetadata("altitude"));
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        return null;
    }
}
