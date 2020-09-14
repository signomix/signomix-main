/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.script;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import java.util.ArrayList;

/**
 *
 * @author greg
 */
public interface ScriptingAdapterIface {
    
    public ScriptResult processData(ArrayList<ChannelData> values, String script, 
            String deviceID, String userID, long dataTimestamp, 
            Double latitude, Double longitude, Double altitude, Double state, 
            int alert, Double devLatitude, Double devLongitude, Double devAltitude, 
            String command, String requestData) throws ScriptAdapterException;
    public ScriptResult processRawData(String requestBody, String script, String deviceID, String userID, long dataTimestamp) throws ScriptAdapterException;
    public ArrayList<ChannelData> decodeData(byte[] data, String script, String deviceID, long dataTimestamp, String userID) throws ScriptAdapterException;
    public ArrayList<ChannelData> decodeHexData(String hexPayload, String script, String deviceID, long dataTimestamp, String userID) throws ScriptAdapterException;
    public ScriptResult processData(ArrayList<ChannelData> values, Device device, long dataTimestamp, 
            Double latitude, Double longitude, Double altitude, Double state, 
            int alert, String command, String requestData) throws ScriptAdapterException;
}
