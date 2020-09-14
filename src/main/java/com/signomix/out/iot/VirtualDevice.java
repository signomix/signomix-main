/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class VirtualDevice{

    public String eui;
    private AtomicLong totalInputs;
    private AtomicLong totalOutputs;
    private AtomicLong correction;
    private Double numberOfInputs = 1.0;
    private Double numberOfOutputs = 1.0;

    public VirtualDevice(){
        totalInputs = new AtomicLong(0);
        totalOutputs = new AtomicLong(0);
        correction = new AtomicLong(0);
    }    
    public VirtualDevice(String eui){
        this.setEui(eui);
        totalInputs = new AtomicLong(0);
        totalOutputs = new AtomicLong(0);
        correction = new AtomicLong(0);
    }
    
    public VirtualDevice(String eui, Double numberOfInputs, Double numberOfOutputs) {
        this.setEui(eui);
        totalInputs = new AtomicLong(0);
        totalOutputs = new AtomicLong(0);
        correction = new AtomicLong(0);
        this.numberOfInputs = numberOfInputs;
        this.numberOfOutputs = numberOfOutputs;
    }

    public void addInputs(long delta) {
        totalInputs.addAndGet(delta);
    }

    public long get() {
        return getInputs() - getOutputs() + correction.get();
    }

    public long getInputs() {
        return (long) (totalInputs.get() / numberOfInputs);
    }

    public long getOutputs() {
        return (long) (totalOutputs.get() / numberOfOutputs);
    }

    public void addOutputs(long delta) {
        totalOutputs.addAndGet(delta);
    }

    public void add(long delta) {
        if (delta >= 0) {
            totalInputs.addAndGet(delta);
        } else {
            totalOutputs.addAndGet(-1 * delta);
        }
    }

    public void resetAndSet(long newCorrection) {
        correction.set(newCorrection);
        totalInputs.set(0);
        totalOutputs.set(0);
    }

    public void setCorrection(long newCorrection) {
        this.correction.set(newCorrection);
    }

    public long getCorrection() {
        return correction.get();
    }

    /**
     * @return the eui
     */
    public String getEui() {
        return eui;
    }

    /**
     * @param eui the eui to set
     */
    public void setEui(String eui) {
        this.eui = eui;
    }

}
