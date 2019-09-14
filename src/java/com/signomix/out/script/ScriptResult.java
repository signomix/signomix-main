/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.script;

import com.signomix.iot.IotEvent;
import com.signomix.out.iot.ChannelData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.cricketmsf.Event;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ScriptResult {

    //ArrayList<MeasureValue> measures;
    HashMap<String, ChannelData> measures;
    HashMap<String, ArrayList> dataEvents;
    ArrayList<Event> events;
    ListOfMaps output=new ListOfMaps();
    
    private boolean listsUsed = false;

    public ScriptResult() {
        dataEvents = new HashMap<>();
        measures = new HashMap<>();
        events = new ArrayList<>();
        output=new ListOfMaps();
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

    public void addDataEvent(String deviceName, String userID, ChannelData data) {
        String payload = deviceName + ":" + data.getName() + ":" + data.getValue() + ":" + data.getTimestamp();
        IotEvent event = new IotEvent(IotEvent.VIRTUAL_DATA, payload);
        event.setOrigin(userID); //to be informed who created the event
        ArrayList<Event> list = dataEvents.get(deviceName);
        if (null == list) {
            list = new ArrayList<>();
        }
        list.add(event);
        dataEvents.put(deviceName, list);
    }

    public void addCommand(String deviceName, String payload) {
        events.add(new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "COMMAND", null, payload));
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

    public long getModulo(long value, long divider) {
        return value % divider;
    }

    public long parseDate(String dateString) {
        return 0;
    }
    
    public void putData(String eui, String name, Double value, long timestamp){
        System.out.println("Adding data "+timestamp);
        output.put(new ChannelData(eui, name, value, timestamp));
        listsUsed=true;
    }
    
    public ArrayList<ArrayList> getOutput(){
        if(listsUsed){
            return output.getMeasures();
        }else{
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
        
        protected void add(long timestamp){
            maps.add(new DataMap(timestamp));
        }
        
        void put(ChannelData v){
            for(int i=0; i<maps.size(); i++){
                if(v.getTimestamp()==maps.get(i).getTimestamp()){
                    maps.get(i).put(v.getName(), v);
                    return;
                }
            }
            System.out.println("!!!! new map created "+v.getTimestamp());
            maps.add(new DataMap(v.getTimestamp()));
            put(v);
        }

        ArrayList<ArrayList> getMeasures() {
            ArrayList<ArrayList> result = new ArrayList<>();
            for (int i = 0; i < maps.size(); i++) {
                ArrayList<ChannelData> tmp = new ArrayList<>();
                Iterator it=maps.get(i).keySet().iterator();
                while (it.hasNext()) {
                    tmp.add(maps.get(i).get(it.next()));
                }
                result.add(tmp);
            }
            return result;
        }
    }
}
