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

    public String getApplicationId() {
        return (String) get("app_id");
    }

    //public String getDeviceId() {
    //    return (String) get("dev_id");
    //}
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
        return (String) getMetadata().get(fieldName);
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        Double value = null;
        try {
            value = new Double((Long) getPayloadFields().get(fieldName));
        } catch (Exception e) {
        }
        try {
            value = (Double) getPayloadFields().get(fieldName);
        } catch (Exception e) {
        }
        return value;
    }

    /*
    @Override
    public long getLongValue(String fieldName, int multiplier) {
        long value;
        try {
            Double d = ((Double)getPayloadFields().get(fieldName));
            d=d*multiplier;
            return d.longValue();
        } catch (Exception e) {
        }
        try {
            value = multiplier* (Long) getPayloadFields().get(fieldName);
            return value;
        } catch (Exception e) {
        }
        return 0;
    }
     */
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
}
