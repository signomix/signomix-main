
package com.signomix.iot.ttn3;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author greg
 */
public class Decoder {
    
    /*
    public String deviceEui;
    public String deviceId;
    public String timestampStr1;
    public String timestampStr2;
    public Long fPort;
    public String frmPayload;
    public Map decodedPayload;
    public Double latitude;
    public Double longitude;
    public Double altitude;
    public Object[] rxMetadata;
    
    */
    /*
    public String getDeviceEUI();
    public String getDeviceID();
    public String getPayload();
    public String[] getPayloadFieldNames();
    //public long getLongValue(String fieldName, int multiplier);
    public Instant getTimeField();
    public long getTimestamp();
    public long getReceivedPackageTimestamp(); // timestamp from data object metadata
    public Double getDoubleValue(String fieldName);
    public String getStringValue(String fieldName);
    public Double getLatitude();
    public Double getLongitude();
    public Double getAltitude();
    */
    
    public TtnData3 decode(String json){
        String rxMetadataJson;
        TtnData3 data=new TtnData3();
        Map map=JsonReader.jsonToMaps(json);
        
        data.deviceId=(String)((Map)map.get("end_device_ids")).get("device_id");
        data.deviceEui=(String)((Map)map.get("end_device_ids")).get("dev_eui");
        data.timestampStr1=(String)map.get("received_at");
        Map uplinkMessage=(Map)map.get("uplink_message");
        data.fPort=(Long)uplinkMessage.get("f_port");
        data.frmPayload=(String)uplinkMessage.get("frm_payload");
        data.decodedPayload=(Map)uplinkMessage.get("decoded_payload");
        data.rxMetadata=(Object[])uplinkMessage.get("rx_metadata");
        data.timestampStr2=(String)map.get("received_at");
        data.normalize();
        HashMap args=new HashMap();
        args.put(JsonWriter.PRETTY_PRINT, true);
        args.put(JsonWriter.TYPE, false);
        rxMetadataJson=JsonWriter.objectToJson(data.rxMetadata, args);
        System.out.println(rxMetadataJson);
        return data;
    }
    
}
