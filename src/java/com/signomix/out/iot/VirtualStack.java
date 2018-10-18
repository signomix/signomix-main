/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class VirtualStack  extends OutboundAdapter implements Adapter, VirtualStackIface {
    
    private ConcurrentHashMap<String,VirtualDevice> cache;
    private String writeInterval;
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        cache = new ConcurrentHashMap<>();
        writeInterval = properties.getOrDefault("write-interval", "");
        Kernel.getInstance().getLogger().print("\twrite-interval: " + writeInterval);
        properties.put("write-interval", writeInterval);
    }
    
    @Override
    public void put(VirtualDevice device){
        cache.put(device.getEui(), device);
    }
    
    @Override
    public VirtualDevice get(String deviceEUI){
        return cache.get(deviceEUI);
    }

    @Override
    public VirtualDevice get(VirtualDevice device) {
        cache.putIfAbsent(device.getEui(), device);
        return get(device.getEui());
    }
    
}
