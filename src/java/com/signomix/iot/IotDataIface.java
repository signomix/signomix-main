/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot;

import java.time.Instant;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface IotDataIface {
    public String getDeviceEUI();
    public String getPayload();
    public String[] getPayloadFieldNames();
    //public long getLongValue(String fieldName, int multiplier);
    public Instant getTimeField();
    public long getTimestamp();
    public long getReceivedPackageTimestamp(); // timestamp from data object metadata
    public Double getDoubleValue(String fieldName);
    public void normalize();
}
