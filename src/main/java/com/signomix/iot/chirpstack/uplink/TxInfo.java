/**
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.chirpstack.uplink;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class TxInfo {
    private long frequency;
    private long dr;

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
     * @return the dr
     */
    public long getDr() {
        return dr;
    }

    /**
     * @param dr the dr to set
     */
    public void setDr(long dr) {
        this.dr = dr;
    }

}
