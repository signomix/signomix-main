/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot.chirpstack.uplink;

import com.signomix.iot.IotDataIface;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Uplink implements IotDataIface{

    private String applicationID;
    private String applicationName;
    private String deviceName;
    private String devEUI;
    private List<RxInfo> rxInfo;
    private TxInfo txInfo;
    private boolean adr;
    private long fCnt;
    private long fPort;
    private String data;
    private Map<String, Map<String,Object>> object;
    private HashMap<String, Double> paylodFields=new HashMap<>();
    
    //private boolean authRequired;
    //private String authKey;
    
    public Uplink(){
    }
    
    public void addField(String name, Double value){
        getPaylodFields().put(name, value);
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
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
    public TxInfo getTxInfo() {
        return txInfo;
    }

    /**
     * @param txInfo the txInfo to set
     */
    public void setTxInfo(TxInfo txInfo) {
        this.txInfo = txInfo;
    }

    /**
     * @return the adr
     */
    public boolean isAdr() {
        return adr;
    }

    /**
     * @param adr the adr to set
     */
    public void setAdr(boolean adr) {
        this.adr = adr;
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
    public long getfPort() {
        return fPort;
    }

    /**
     * @param fPort the fPort to set
     */
    public void setfPort(long fPort) {
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

    /**
     * @return the object
     */
    public Map<String, Map<String,Object>> getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Map<String, Map<String,Object>> object) {
        this.object = object;
    }

    @Override
    public String getDeviceEUI() {
        return devEUI;
    }

    @Override
    public String getPayload() {
        return data;
    }

    @Override
    public String[] getPayloadFieldNames() {
        ArrayList<String> arr=new ArrayList<>();
        getPaylodFields().keySet().forEach(key->{
            arr.add(key);
        });
        return arr.toArray(new String[arr.size()]);
    }

    @Override
    public Instant getTimeField() {
        return Instant.now();
    }

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public long getReceivedPackageTimestamp() {
        return getTimestamp();
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        return getPaylodFields().get(fieldName);
    }

    @Override
    public String getStringValue(String fieldName) {
        return null;
    }

    @Override
    public Double getLatitude() {
        return rxInfo.get(0).getLocation().latitude;
    }

    @Override
    public Double getLongitude() {
        return rxInfo.get(0).getLocation().longitude;
    }

    @Override
    public Double getAltitude() {
        return Double.valueOf(rxInfo.get(0).getLocation().altitude);
    }

    @Override
    public void normalize() {
    }

    @Override
    public String getDeviceID() {
        return deviceName;
    }

    /**
     * @return the paylodFields
     */
    public HashMap<String, Double> getPaylodFields() {
        return paylodFields;
    }

    /**
     * @param paylodFields the paylodFields to set
     */
    public void setPaylodFields(HashMap<String, Double> paylodFields) {
        this.paylodFields = paylodFields;
    }

}
