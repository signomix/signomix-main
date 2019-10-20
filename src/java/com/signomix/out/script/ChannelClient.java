/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.script;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import java.util.List;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ChannelClient {

    ThingsDataIface thingsAdapter;
    String userID;
    String deviceID;

    public ChannelClient(String userID, String deviceID, ThingsDataIface thingsAdapter) {
        this.thingsAdapter = thingsAdapter;
        this.userID = userID;
        this.deviceID = deviceID;
    }

    public ChannelData getLastData(String channel) {
        try {
            return thingsAdapter.getLastValue(userID, deviceID, channel);
        } catch (ThingsDataException ex) {
            return null;
        }
    }

    public ChannelData getAverageValue(String channel, int scope) {
        return getAverageValue(channel, scope, null);
    }

    public ChannelData getAverageValue(String channel, int scope, Double newValue) {
        try {
            List<List> result;
            if(newValue==null){
                result =thingsAdapter.getValues(userID, deviceID, "channel "+channel+" average " + scope);
            }else{
                result =thingsAdapter.getValues(userID, deviceID, "channel "+channel+" average " + scope + " new "+newValue);
            }
            if(result!=null){
                if(result.get(0).size()>0){
                    return (ChannelData)result.get(0).get(0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
