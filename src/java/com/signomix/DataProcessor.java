/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import com.signomix.iot.IotEvent;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.script.ScriptAdapterException;
import com.signomix.out.script.ScriptResult;
import com.signomix.out.script.ScriptingAdapterIface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DataProcessor {

    public static ArrayList<ChannelData> processValues(ArrayList<ChannelData> listOfValues, Device device, ScriptingAdapterIface scriptingAdapter, long dataTimestamp) throws Exception {
        ScriptResult scriptResult = null;
        try {
            scriptResult = scriptingAdapter.processData(listOfValues, device.getCodeUnescaped(), device.getEUI(), device.getUserID(), dataTimestamp);
        } catch (ScriptAdapterException e) {
            throw new Exception(e.getMessage());
        }
        if (scriptResult == null) {
            throw new Exception("preprocessor script returns null result");
        }
        ArrayList<ChannelData> finalValues = scriptResult.getMeasures();
        ArrayList<Event> events = scriptResult.getEvents();
        //Event ev;
        HashMap<String, String> recipients;
        for (int i = 0; i < events.size(); i++) {
            //ev = events.get(i);
            if (IotEvent.VIRTUAL_DATA.equals(events.get(i).getType())) {
                Event newEvent = events.get(i).clone();
                newEvent.setOrigin(device.getUserID());
                Kernel.handle(newEvent);
            }else if(Event.CATEGORY_GENERIC.equals(events.get(i).getCategory())){
                Event newEvent = events.get(i).clone();
                newEvent.setOrigin(device.getEUI());
                Kernel.handle(newEvent);
            }else {
                recipients = new HashMap<>();
                recipients.put(device.getUserID(), "");
                if (device.getTeam() != null) {
                    String[] r = device.getTeam().split(",");
                    for (int j = 0; j < r.length; j++) {
                        if (!r[j].isEmpty()) {
                            recipients.put(r[j], "");
                        }
                    }
                }
                Iterator itr = recipients.keySet().iterator();
                while (itr.hasNext()) {
                    Event newEvent = events.get(i).clone();
                    newEvent.setOrigin(itr.next() + "\t" + device.getEUI());
                    Kernel.handle(newEvent);
                    //System.out.println("IOT EVENT: " + newEvent.toString());
                }
            }
        }
        return finalValues;
    }

}
