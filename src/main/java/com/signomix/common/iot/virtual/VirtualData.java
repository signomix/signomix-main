package com.signomix.common.iot.virtual;

import java.util.HashMap;

public class VirtualData {
    public String eui;
    public long timestamp;
    public HashMap<String,Double> payload_fields;

    public VirtualData(){
    }

    public VirtualData(String eui){
        this.eui=eui;
        timestamp=System.currentTimeMillis();
        payload_fields=new HashMap<>();
    }
}
