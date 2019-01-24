/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.iot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ChannelData {

    private String deviceEUI;
    private String name;
    private Double value;
    //private long longValue;
    private long timestamp;
    //private int multiplier;

    public ChannelData() {
    }

    public ChannelData(String deviceEUI, String name, Double value, long timestamp) {
        this.deviceEUI = deviceEUI;
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public ChannelData(String name, Double value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    public ChannelData(String name, Long value, long timestamp) {
        this.name = name;
        this.value = value.doubleValue();
        this.timestamp = timestamp;
    }

    public String toString() {
        return getDeviceEUI()+ ":" + getName()+ ":" + getTimestamp() + ":" + getValue();
    }

    public String toCsv(String delimeter, boolean convertTimestamp) {

        if (convertTimestamp) {
            String dateFormatted;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormatted = dateFormat.format(new Date(getTimestamp()));
            return getDeviceEUI() + delimeter + getName() + delimeter + dateFormatted + delimeter + getValue();
        }
        return getDeviceEUI() + delimeter + getName() + delimeter + getTimestamp() + delimeter + getValue();
    }

    public static String getCsvHeaderLine(boolean convertTimestamp) {
        return "EUI,name,"+(convertTimestamp?"date (UTC)":"timestamp")+",value\r\n";
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
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String stringValue) {
        Double v = null;
        try {
            v = Double.parseDouble(stringValue);
        } catch (Exception e) {
        }
        try {
            v = new Double((Long.parseLong(stringValue)));
        } catch (Exception e) {
        }
        this.value = v;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the deviceEUI
     */
    public String getDeviceEUI() {
        return deviceEUI;
    }

    /**
     * @param deviceEUI the deviceEUI to set
     */
    public void setDeviceEUI(String deviceEUI) {
        this.deviceEUI = deviceEUI;
    }

}
