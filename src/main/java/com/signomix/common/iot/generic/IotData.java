/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.common.iot.generic;

import com.signomix.common.iot.chirpstack.uplink.Uplink;

/**
 *
 * @author greg
 */
public class IotData {

    public static final int CHIRPSTACK = 0;
    public static final int GENERIC = 1;
    public static final int TTN = 2;
    public static final int KPN = 3;

    private int type;
    private Uplink chirpstackData;
    private IotData2 iotData;
    private boolean authRequired;
    private String authKey;
    private String serializedData;
    private String deviceEUI;
    private String gatewayEUI;
    private String clientName;

    public IotData(Uplink data) {
        chirpstackData = data;
        type = CHIRPSTACK;
        deviceEUI=data.getDevEUI();
        gatewayEUI=null;
        clientName=null;
    }
    
    public IotData(IotData2 data) {
        iotData = data;
        type = GENERIC;
        deviceEUI=data.getDeviceEUI();
        gatewayEUI=data.gateway_eui;
        clientName=data.clientname;
    }

    public IotData authRequired(boolean required) {
        this.setAuthRequired(required);
        return this;
    }

    public IotData authKey(String key) {
        this.setAuthKey(key);
        return this;
    }

    public IotData serializedData(String data) {
        this.setSerializedData(data);
        return this;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the chirpstackData
     */
    public Uplink getChirpstackData() {
        return chirpstackData;
    }

    /**
     * @param chirpstackData the chirpstackData to set
     */
    public void setChirpstackData(Uplink chirpstackData) {
        this.chirpstackData = chirpstackData;
    }

    public IotData2 getIotData() {
        return iotData;
    }

    public void setIotData(IotData2 iotData) {
        this.iotData=iotData;
    }

    /**
     * @return the authRequired
     */
    public boolean isAuthRequired() {
        return authRequired;
    }

    /**
     * @param authRequired the authRequired to set
     */
    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    /**
     * @return the authKey
     */
    public String getAuthKey() {
        return authKey;
    }

    /**
     * @param authKey the authKey to set
     */
    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    /**
     * @return the serializedData
     */
    public String getSerializedData() {
        return serializedData;
    }

    /**
     * @param serializedData the serializedData to set
     */
    public void setSerializedData(String serializedData) {
        this.serializedData = serializedData;
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

    /**
     * @return the gatewayEUI
     */
    public String getGatewayEUI() {
        return gatewayEUI;
    }

    /**
     * @param gatewayEUI the gatewayEUI to set
     */
    public void setGatewayEUI(String gatewayEUI) {
        this.gatewayEUI = gatewayEUI;
    }

    /**
     * @return the clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientName the clientName to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

}
