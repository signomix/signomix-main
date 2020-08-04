/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import com.signomix.iot.chirpstack.uplink.Uplink;

/**
 *
 * @author greg
 */
public class IotData {

    private static int CHIRPSTACK = 0;
    
    private int type;
    private Uplink chirpstackData;
    private boolean authRequired;
    private String authKey;
    private String serializedData;
    
    public IotData(Uplink data){
        chirpstackData=data;
        type=CHIRPSTACK;
    }
    
    public IotData authRequired(boolean required){
        this.setAuthRequired(required);
        return this;
    }
    
    public IotData authKey(String key){
        this.setAuthKey(key);
        return this;
    }

    public IotData serializedData(String data){
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
    
}
