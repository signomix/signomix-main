/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.iot;

import java.util.Base64;
import java.util.HashMap;

import com.signomix.common.HexTool;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class TtnDownlinkMessage {

    String dev_id;
    String payload_raw;
    HashMap<String, Object> payload_fields;
    boolean confirmed;
    int port;

    public TtnDownlinkMessage(String devID, String payload, boolean confirmed, int port) {
        dev_id = devID;
        Kernel.getInstance().dispatchEvent(Event.logInfo(this,"PAYLOAD:["+payload+"]"));
        payload_raw=Base64.getEncoder().encodeToString(HexTool.hexStringToByteArray(payload));
        this.confirmed = confirmed;
        this.port = port;
        payload_fields = null;
        Kernel.getInstance().dispatchEvent(Event.logInfo(this,"PAYLOAD_RAW:["+payload_raw+"]"));
    }

    public void addField(String name, Object value) {
        if (null == payload_fields) {
            payload_fields = new HashMap<>();
        }
        payload_fields.put(name, value);
    }

}
