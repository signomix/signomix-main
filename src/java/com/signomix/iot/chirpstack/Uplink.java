    /**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot.chirpstack;

import com.signomix.iot.lora.*;
import com.signomix.iot.IotDataIface;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Uplink implements IotDataIface {

    private String applicationID;
    private String applicationName;
    private String deviceName;
    private String devEUI;
    private List<RxInfo> rxInfo;
    private LoRaTxInfo txInfo;
    private boolean adr;
    private long fCnt;
    private int fPort;
    private String data;
    public Map<String,String> tags;
    public Map<String,Value> object;

    /*
    
    */
    @Override
    public String getDeviceEUI() {
        return getDevEUI();
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

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName the applicationName to set
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * @return the nodeName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param nodeName the nodeName to set
     */
    public void setDeviceName(String nodeName) {
        this.deviceName = nodeName;
    }

    /**
     * @return the devEUI
     */
    public String getDevEUI() {
        return devEUI;
    }

    /**
     * @param devEUI the devEUI to set
     */
    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    /**
     * @return the rxInfo
     */
    public List<RxInfo> getRxInfo() {
        return rxInfo;
    }

    /**
     * @param rxInfo the rxInfo to set
     */
    public void setRxInfo(List<RxInfo> rxInfo) {
        this.rxInfo = rxInfo;
    }

    /**
     * @return the txInfo
     */
    public LoRaTxInfo getTxInfo() {
        return txInfo;
    }

    /**
     * @param txInfo the txInfo to set
     */
    public void setTxInfo(LoRaTxInfo txInfo) {
        this.txInfo = txInfo;
    }

    /**
     * @return the fCnt
     */
    public long getfCnt() {
        return fCnt;
    }

    /**
     * @param fCnt the fCnt to set
     */
    public void setfCnt(long fCnt) {
        this.fCnt = fCnt;
    }

    /**
     * @return the fPort
     */
    public int getfPort() {
        return fPort;
    }

    /**
     * @param fPort the fPort to set
     */
    public void setfPort(int fPort) {
        this.fPort = fPort;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getPayload() {
        return getData();
    }

    @Override
    public String[] getPayloadFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instant getTimeField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getTimestamp() {
        long timestamp;
        try {
            timestamp = Long.parseLong(((LoRaRxInfo) getRxInfo()).getTime());
        } catch (NumberFormatException e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        return timestamp;
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStringValue(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void normalize() {
        if (this.devEUI != null) {
            this.devEUI = this.devEUI.toUpperCase();
        }
        //TODO: gateways?
    }

    @Override
    public long getReceivedPackageTimestamp() {
        return getTimestamp();
    }

    @Override
    public String getDeviceID() {
        return "";
    }

    @Override
    public Double getLatitude() {
        return null;
    }

    @Override
    public Double getLongitude() {
        return null;
    }

    @Override
    public Double getAltitude() {
        return null;
    }

    /**
     * @return the tags
     */
    public Map<String,String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(Map<String,String> tags) {
        this.tags = tags;
    }

    /**
     * @return the object
     */
    public Map<String,Value> getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Map<String,Value> object) {
        this.object = object;
    }

}
