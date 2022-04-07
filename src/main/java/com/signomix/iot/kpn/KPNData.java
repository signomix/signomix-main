/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.iot.kpn;

import com.signomix.iot.IotDataIface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class KPNData implements IotDataIface{
    private DevEuiUplink DevEUI_uplink;
    
    @Override
    public void normalize() {
        if(this.DevEUI_uplink!=null && this.DevEUI_uplink.DevEUI!=null){
            this.DevEUI_uplink.DevEUI=this.DevEUI_uplink.DevEUI.toUpperCase();
        }
    }

    @Override
    public String getDeviceEUI() {
        return DevEUI_uplink.DevEUI;
    }

    @Override
    public String[] getPayloadFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instant getTimeField() {
        LocalDateTime ldt = LocalDateTime.parse(DevEUI_uplink.Time);
        return ldt.toInstant(ZoneOffset.UTC);
    }

    @Override
    public long getTimestamp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getStringValue(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPayload() {
        return DevEUI_uplink.payload_hex;
    }

    @Override
    public long getReceivedPackageTimestamp() {
        return getTimeField().toEpochMilli();
    }

    @Override
    public String getDeviceID() {
        return "";
    }

    @Override
    public Double getLatitude() {
        return null;
    }

    @Override
    public Double getLongitude() {
        return null;
    }

    @Override
    public Double getAltitude() {
        return null;
    }

    @Override
    public String getHexPayload() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
