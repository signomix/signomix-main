/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import java.time.Instant;
import java.util.List;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class LoRaData implements IotDataIface {

    private String applicationID;
    private String applicationName;
    private String nodeName;
    private String devEUI;
    private List<LoRaRxInfo> rxInfo;
    private LoRaTxInfo txInfo;
    private long fCnt;
    private int port;
    private String data;

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
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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
    public List<LoRaRxInfo> getRxInfo() {
        return rxInfo;
    }

    /**
     * @param rxInfo the rxInfo to set
     */
    public void setRxInfo(List<LoRaRxInfo> rxInfo) {
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
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
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
    public void normalize() {
        if (this.devEUI != null) {
            this.devEUI = this.devEUI.toUpperCase();
        }
        //TODO: gateways?
    }

}
