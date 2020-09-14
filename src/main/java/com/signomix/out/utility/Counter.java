/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.utility;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Counter extends OutboundAdapter implements Adapter, CounterIface {

    AtomicLong totalInputs;
    AtomicLong totalOutputs;
    AtomicLong correction;
    private Double inputsDivider = 1.0;
    private Double outputsDivider = 1.0;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        totalInputs = new AtomicLong(0);
        totalOutputs = new AtomicLong(0);
        correction = new AtomicLong(0);
        try {
            inputsDivider = Double.parseDouble(properties.getOrDefault("incoming-divider", "1.0"));
        } catch (NumberFormatException ex) {
            Kernel.handle(Event.logWarning(this, "malformed config"));
        }
        try {
            outputsDivider = Double.parseDouble(properties.getOrDefault("exiting-divider", "1.0"));
        } catch (NumberFormatException ex) {
            Kernel.handle(Event.logWarning(this, "malformed config"));
        }
    }

    @Override
    public void addInputs(long delta) {
        totalInputs.addAndGet(delta);
    }

    @Override
    public long get() {
        return getInputs()-getOutputs()+correction.get();
    }

    @Override
    public long getInputs() {
        return (long)(totalInputs.get()/inputsDivider);
    }

    @Override
    public long getOutputs() {
        return (long)(totalOutputs.get()/outputsDivider);
    }

    @Override
    public void addOutputs(long delta) {
        totalOutputs.addAndGet(delta);
    }

    @Override
    public void add(long delta) {
        if (delta >= 0) {
            totalInputs.addAndGet(delta);
        } else {
            totalOutputs.addAndGet(-1*delta);
        }
    }

    @Override
    public void resetAndSet(long newCorrection) {
        correction.set(newCorrection);
        totalInputs.set(0);
        totalOutputs.set(0);
    }
    
    @Override
    public void setCorrection(long newCorrection){
        this.correction.set(newCorrection);
    }

    @Override
    public long getCorrection() {
        return correction.get();
    }

}
