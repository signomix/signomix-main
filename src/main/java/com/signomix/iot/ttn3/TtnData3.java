package com.signomix.iot.ttn3;

import com.signomix.iot.IotDataIface;
import com.signomix.iot.TtnData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author greg
 */
public class TtnData3 extends TtnData implements IotDataIface {

    public String deviceEui;
    public String deviceId;
    public Long fPort;
    public String frmPayload;
    public Map decodedPayload;
    public Double latitude=null;
    public Double longitude=null;
    public Double altitude=null;
    public Object[] rxMetadata;
    public String rxMetadataJson;
    private long timestamp;
    private long receivedUplinkTimestamp;
    private String[] payloadFieldNames = {};
    public String timestampStr1;
    public String timestampStr2;

    @Override
    public String getDeviceID() {
        return deviceId;
    }

    @Override
    public String getDeviceEUI() {
        return deviceEui;
    }

    @Override
    public String getPayload() {
        return frmPayload;
    }

    @Override
    public String[] getPayloadFieldNames() {
        return payloadFieldNames;
    }

    @Override
    public Instant getTimeField() {
        try {
            return Instant.parse(timestampStr1);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getReceivedPackageTimestamp() {
        return receivedUplinkTimestamp;
    }

    @Override
    public Double getDoubleValue(String fieldName) {
        Double value = null;
        try {
            value = (Double) decodedPayload.get(fieldName);
            return value;
        } catch (Exception e) {
        }
        try {
            value = new Double((Long) decodedPayload.get(fieldName));
            return value;
        } catch (Exception e) {
        }
        try {
            // when the field is received as String
            value = Double.parseDouble((String) decodedPayload.get(fieldName));
            return value;
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public String getStringValue(String fieldName) {
        return "" + decodedPayload.get(fieldName);
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public Double getAltitude() {
        return altitude;
    }
    
    public String getDownlink(){
        return "";
    }

    public long getFrameCounter(){
        return 0;
    }
    
    public HashMap getPayloadFields(){
        return (HashMap)decodedPayload;
    }
    
    @Override
    public void normalize() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        try {
            timestamp = sdf.parse(timestampStr1).getTime();
        } catch (ParseException ex) {
            timestamp = System.currentTimeMillis();
        }
        try {
            receivedUplinkTimestamp = sdf.parse(timestampStr2).getTime();
        } catch (ParseException ex) {
            receivedUplinkTimestamp = System.currentTimeMillis();
        }
        Iterator it=decodedPayload.keySet().iterator();
        String key,subKey;
        Object element;
        HashMap tmpMap=new HashMap();
        while(it.hasNext()){
            key=(String)it.next();
            element=decodedPayload.get(key);
            if(element instanceof Map){
                Iterator it2=((Map) element).keySet().iterator();
                while(it2.hasNext()){
                    subKey=(String)it2.next();
                    tmpMap.put(key+"_"+subKey, ((Map) element).get(subKey));
                }
            }else{
                tmpMap.put(key, element);
            }
        }
        decodedPayload.clear();
        it=tmpMap.keySet().iterator();
        while(it.hasNext()){
            key=(String)it.next();
            decodedPayload.put(key,tmpMap.get(key));
        }
        String[] arr = new String[decodedPayload.size()];
        Set<String> s = decodedPayload.keySet();
        int i = 0;
        for (String x : s) {
            arr[i++] = x;
        }
        payloadFieldNames = arr;
        latitude = null;
        longitude = null;
        altitude = null;
        if(null==deviceEui){
            deviceEui=deviceId;
        }
    }

}
