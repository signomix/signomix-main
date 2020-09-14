/**
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.chirpstack.uplink;


/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class RxInfo {
    
    private String gatewayID;
    private String uplinkID;
    private String name;
    private String time;
    private long rssi;
    private long loRaSNR;
    public Location location;

    /**
     * @return the gatewayID
     */
    public String getGatewayID() {
        return gatewayID;
    }

    /**
     * @param gatewayID the gatewayID to set
     */
    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    /**
     * @return the rssi
     */
    public long getRssi() {
        return rssi;
    }

    /**
     * @param rssi the rssi to set
     */
    public void setRssi(long rssi) {
        this.rssi = rssi;
    }

    /**
     * @return the loRaSNR
     */
    public long getLoRaSNR() {
        return loRaSNR;
    }

    /**
     * @param loRaSNR the loRaSNR to set
     */
    public void setLoRaSNR(long loRaSNR) {
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

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the uplinkID
     */
    public String getUplinkID() {
        return uplinkID;
    }

    /**
     * @param uplinkID the uplinkID to set
     */
    public void setUplinkID(String uplinkID) {
        this.uplinkID = uplinkID;
    }
    
}
