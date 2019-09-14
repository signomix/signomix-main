/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.lora;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class LoRaRxInfo {
    private String mac;
    private int rssi;
    private int loRaSNR;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private String time;

    /**
     * @return the mac
     */
    public String getMac() {
        return mac;
    }

    /**
     * @param mac the mac to set
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * @return the rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * @param rssi the rssi to set
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * @return the loRaSNR
     */
    public int getLoRaSNR() {
        return loRaSNR;
    }

    /**
     * @param loRaSNR the loRaSNR to set
     */
    public void setLoRaSNR(int loRaSNR) {
        this.loRaSNR = loRaSNR;
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
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the altitude
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }
    
}
