package com.signomix.util;


import com.signomix.in.http.formatter.kanarek.KanarekDto;
import com.signomix.in.http.formatter.kanarek.KanarekFormatter;
import com.signomix.in.http.formatter.kanarek.KanarekStationDto;
import com.signomix.in.http.formatter.kanarek.KanarekValue;
import com.signomix.out.iot.ChannelData;
import java.util.ArrayList;
import org.cricketmsf.in.http.StandardResult;

/**
 *
 * @author greg
 */
public class TestKanarekFormatter {
    
    public static void main(String[] args){
        
        ArrayList<ArrayList> stations = new ArrayList<>();
        ArrayList station1 = new ArrayList();
        ArrayList station2 = new ArrayList();
        
        // station 1
        ChannelData cdata= new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("pm25");
        cdata.setValue("10.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("pm10");
        cdata.setValue("20.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("latitude");
        cdata.setValue("19.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("longitude");
        cdata.setValue("15.0");
        station1.add(cdata);
        
        // station 2
        cdata= new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("pm25");
        cdata.setValue("10.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("pm10");
        cdata.setValue("20.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("latitude");
        cdata.setValue("19.0");
        station1.add(cdata);
        cdata=new ChannelData();
        cdata.setDeviceEUI("aaaa");
        cdata.setName("longitude");
        cdata.setValue("15.0");
        station1.add(cdata);
        
        
        stations.add(station1);
        stations.add(station2);
        
        KanarekFormatter kf=new KanarekFormatter();
        KanarekDto kdto = new KanarekDto();
        KanarekStationDto ks=new KanarekStationDto();
        ks.id= 11111L;
        ks.lat=19.1;
        ks.lon=15.2;
        KanarekValue kv=new KanarekValue();
        kv.type=KanarekValue.PM10;
        kv.v=12.3;
        kv.t=1234567890;
        ks.values.add(kv);
        kdto.stations.add(ks);
        System.out.println(kf.format(true, new StandardResult(kdto)));
    }
    
    private KanarekDto transform(ArrayList<ArrayList> stations){
        KanarekDto kdto = new KanarekDto();
        KanarekStationDto ks;
        ChannelData cdata;
        for(int i=0; i<stations.size(); i++){
            ks=new KanarekStationDto();
            for(int j=0; j<stations.get(i).size(); j++){
                cdata=(ChannelData)stations.get(i).get(j);
                ks.id=Long.parseLong(cdata.getDeviceEUI(),16);
                ks.country="PL";
                switch(cdata.getName()){
                    case "latitude":
                        ks.lat=cdata.getValue();
                        break;
                    case "longitude":
                        ks.lon=cdata.getValue();
                        break;
                    case "pm25":
                        break;
                    case "pm100":
                        break;
                }
            }
        }
        
        return kdto;
    }
    
}
