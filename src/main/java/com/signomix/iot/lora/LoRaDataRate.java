/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.lora;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class LoRaDataRate {
    private String modulation;
    private int bandwidth;
    private int spreadFactor;

    /**
     * @return the modulation
     */
    public String getModulation() {
        return modulation;
    }

    /**
     * @param modulation the modulation to set
     */
    public void setModulation(String modulation) {
        this.modulation = modulation;
    }

    /**
     * @return the bandwidth
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     * @param bandwidth the bandwidth to set
     */
    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * @return the spreadFactor
     */
    public int getSpreadFactor() {
        return spreadFactor;
    }

    /**
     * @param spreadFactor the spreadFactor to set
     */
    public void setSpreadFactor(int spreadFactor) {
        this.spreadFactor = spreadFactor;
    }
    
}
