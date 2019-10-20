/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import com.signomix.util.HexTool;
import java.util.Base64;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class TtnDownlinkMessage {
    String dev_id;
    String payload_raw;
    HashMap<String,Object> payload_fields;
    boolean confirmed;
    int port;
    
    public TtnDownlinkMessage(String devID, String payload, boolean confirmed, int port){
        dev_id=devID;
        payload_raw=new String(Base64.getEncoder().encode(HexTool.hexStringToByteArray(payload)));
        this.confirmed=confirmed;
        this.port=port;
        payload_fields=null;
    }
    
    public void addField(String name, Object value){
        if(null==payload_fields){
            payload_fields=new HashMap<>();
        }
        payload_fields.put(name, value);
    }
    
}
