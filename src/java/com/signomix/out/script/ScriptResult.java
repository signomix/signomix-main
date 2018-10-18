/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.script;

import com.signomix.iot.IotEvent;
import com.signomix.out.iot.ChannelData;
import java.util.ArrayList;
import java.util.HashMap;
import org.cricketmsf.Event;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ScriptResult {

    //ArrayList<MeasureValue> measures;
    HashMap<String,ChannelData> measures;
    ArrayList<Event> events;

    public ScriptResult() {
        measures = new HashMap<>();
        events = new ArrayList<>();
    }
    
    public void putData(ChannelData v){
        measures.put(v.getName(), v);
    }
    
    public void removeData(String channelName){
        measures.remove(channelName);
    }
    
    public void addEvent(String type, String message){
        events.add(new IotEvent(type, message));
    }
    
    public void addDataEvent(String deviceName, String userID, ChannelData data){
        IotEvent event=new IotEvent(IotEvent.VIRTUAL_DATA, deviceName+":"+data.toString());
        event.setOrigin(userID); //to be informed who created the event
        events.add(event);
    }
    
    public void addCommand(String deviceName, String payload){
        events.add(new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "COMMAND", null, payload));
    }
    
    public ArrayList<ChannelData> getMeasures(){
        ArrayList<ChannelData> result = new ArrayList<>();
        measures.keySet().forEach(key -> {
            result.add(measures.get(key));
        });
        return result;
    }
    
    public ArrayList<Event> getEvents(){
        return events;
    }
}
