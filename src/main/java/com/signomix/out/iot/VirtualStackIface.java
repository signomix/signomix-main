/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import org.cricketmsf.out.OutboundAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface VirtualStackIface extends OutboundAdapterIface{
    
    public VirtualDevice get(String deviceEUI);
    public VirtualDevice get(VirtualDevice device);
    public void put(VirtualDevice device);
    
}
