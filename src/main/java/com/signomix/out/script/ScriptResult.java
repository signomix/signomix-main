/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.script;

import com.signomix.common.iot.ChannelData;
import com.signomix.event.IotEvent;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ScriptResult {

    //ArrayList<MeasureValue> measures;
    HashMap<String, ChannelData> measures;
    HashMap<String, ArrayList> dataEvents;
    ArrayList<Event> events;
    /**
     * Each map in the output list can include data with different timestamps
     * This way, output can hold several data points
     */
    ListOfMaps output = new ListOfMaps();
    public Double deviceState;
    private boolean listsUsed = false;

    public ScriptResult() {
        dataEvents = new HashMap<>();
        measures = new HashMap<>();
        events = new ArrayList<>();
        output = new ListOfMaps();
    }

    public void putData(ChannelData v) {
        measures.put(v.getName(), v);
    }

    public void removeData(String channelName) {
        measures.remove(channelName);
    }

    public void rename(ChannelData v, String newName) {
        measures.remove(v.getName());
        v.setName(newName);
        measures.put(newName, v);
    }

    public void rename(String oldName, String newName) {
        ChannelData v = measures.get(oldName);
        if (null != v) {
            measures.remove(oldName);
            v.setName(newName);
            measures.put(newName, v);
        }
    }

    public void addEvent(String type, String message) {
        events.add(new IotEvent(type, message));
    }

    public void addDataEvent(String deviceName, String fromEui, ChannelData data) {
        String payload = deviceName + ":" + data.getName() + ":" + data.getValue() + ":" + data.getTimestamp();
        IotEvent event = new IotEvent(IotEvent.VIRTUAL_DATA, payload);
        event.setOrigin(fromEui); //to be informed which device created the event
        ArrayList<Event> list = dataEvents.get(deviceName);
        if (null == list) {
            list = new ArrayList<>();
        }
        list.add(event);
        dataEvents.put(deviceName, list);
    }

/*
    public void addCommand(String toDevice, String fromDevice, String payload, boolean hexRepresentation, boolean overwrite) {
        //events.add(new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "COMMAND", null, payload));
        IotEvent event = new IotEvent();
        event.setOrigin(fromDevice+"@"+toDevice);
        if (hexRepresentation) {
            event.setType(IotEvent.ACTUATOR_HEXCMD);
        } else {
            event.setType(IotEvent.ACTUATOR_CMD);
        }
        String prefix;
        if(overwrite){
            prefix="#";
        }else{
            prefix="&";
        }
        if(hexRepresentation){
            event.setPayload(prefix+payload);
        }else{
            event.setPayload(prefix+Base64.getEncoder().encodeToString(payload.getBytes()));
        }
        events.add(event);
    }
*/
    public void addCommand(String toDevice, String fromDevice, String payload, int representation, boolean overwrite) {
        int PLAIN_VER = 0;
        int HEX_VER = 1;
        int ENCODED_VER =2;

        IotEvent event = new IotEvent();
        event.setOrigin(fromDevice+"@"+toDevice);
        if (HEX_VER==representation) {
            event.setType(IotEvent.ACTUATOR_HEXCMD);
        } else if(PLAIN_VER==representation){
            event.setType(IotEvent.ACTUATOR_PLAINCMD);
        }else{
            event.setType(IotEvent.ACTUATOR_CMD);
        }
        String prefix;
        if(overwrite){
            prefix="#";
        }else{
            prefix="&";
        }
        if(ENCODED_VER!=representation){
            event.setPayload(prefix+payload);
        }else{
            event.setPayload(prefix+Base64.getEncoder().encodeToString(payload.getBytes()));
        }
        events.add(event);
    }

    public ArrayList<ChannelData> getMeasures() {
        ArrayList<ChannelData> result = new ArrayList<>();
        measures.keySet().forEach(key -> {
            result.add(measures.get(key));
        });
        return result;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public HashMap<String, ArrayList> getDataEvents() {
        return dataEvents;
    }

    ////////////////////  refactoring : new functions
    
    /**
     * Calculates distance between 2 points on Earth
     * @param lat1 latitude of the first point
     * @param lon1 longitude of the first
     * @param lat2 latitude of the second point
     * @param lon2 latitude of the second point
     * @return distance between points in meters
     */
    public long getDistance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            lon1 = Math.toRadians(lon1);
            lon2 = Math.toRadians(lon2);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);
            // Haversine formula  
            double dlon = lon2 - lon1;
            double dlat = lat2 - lat1;
            double a = Math.pow(Math.sin(dlat / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2)
                    * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            // Radius of earth in kilometers.
            double r = 6371;
            // result in meters 
            return (long)(c * r * 1000);
        }
    }

    public long getModulo(long value, long divider) {
        return value % divider;
    }

    public long parseDate(String dateString) {
        return 0;
    }

    public void putData(String eui, String name, Double value, long timestamp) {
        output.put(new ChannelData(eui, name, value, timestamp));
        listsUsed = true;
    }

    public ArrayList<ArrayList> getOutput() {
        if (listsUsed) {
            return output.getMeasures();
        } else {
            ArrayList<ArrayList> list = new ArrayList<>();
            list.add(getMeasures());
            return list;
        }
    }

    class DataMap extends HashMap<String, ChannelData> {

        private long timestamp = 0;

        DataMap(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }

    class ListOfMaps {

        ArrayList<DataMap> maps = new ArrayList<>();

        protected void add(long timestamp) {
            maps.add(new DataMap(timestamp));
        }

        void put(ChannelData v) {
            for (int i = 0; i < maps.size(); i++) {
                if (v.getTimestamp() == maps.get(i).getTimestamp()) {
                    maps.get(i).put(v.getName(), v);
                    return;
                }
            }
            maps.add(new DataMap(v.getTimestamp()));
            put(v);
        }

        ArrayList<ArrayList> getMeasures() {
            ArrayList<ArrayList> result = new ArrayList<>();
            for (int i = 0; i < maps.size(); i++) {
                ArrayList<ChannelData> tmp = new ArrayList<>();
                Iterator it = maps.get(i).keySet().iterator();
                while (it.hasNext()) {
                    tmp.add(maps.get(i).get(it.next()));
                }
                result.add(tmp);
            }
            return result;
        }
    }

    /**
     * @return the deviceState
     */
    public Double getDeviceState() {
        return deviceState;
    }

    /**
     * @param deviceState the deviceState to set
     */
    public void setDeviceState(Double deviceState) {
        this.deviceState = deviceState;
    }
    
    public void log(String message){
        Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), message));
    }
}
