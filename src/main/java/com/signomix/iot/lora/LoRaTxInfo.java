/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.lora;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class LoRaTxInfo {
    private long frequency;
    private LoRaDataRate dataRate;
    private boolean adr;
    private String codeRate;

    /**
     * @return the frequency
     */
    public long getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the adr
     */
    public boolean getAdr() {
        return adr;
    }

    /**
     * @param adr the adr to set
     */
    public void setAdr(boolean adr) {
        this.adr = adr;
    }

    /**
     * @return the codeRate
     */
    public String getCodeRate() {
        return codeRate;
    }

    /**
     * @param codeRate the codeRate to set
     */
    public void setCodeRate(String codeRate) {
        this.codeRate = codeRate;
    }

    /**
     * @return the dataRate
     */
    public LoRaDataRate getDataRate() {
        return dataRate;
    }

    /**
     * @param dataRate the dataRate to set
     */
    public void setDataRate(LoRaDataRate dataRate) {
        this.dataRate = dataRate;
    }
    
}
