/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.db;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import java.util.List;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface IotDataStorageIface extends KeyValueDBIface {
    
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException;
    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException;
    public void putDeviceChannels(String deviceEUI, List<String> channelNames) throws ThingsDataException;
    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException;
    public int updateDeviceChannels(Device device, Device oldDevice) throws ThingsDataException;
    public void removeAllChannels(String deviceEUI) throws ThingsDataException;
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException;

    public void putData(String userID, String deviceEUI, List<ChannelData>values) throws ThingsDataException;
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException;
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException;
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException;
    public List<List> getValues(String userID, String deviceEUI, String channel, String query) throws ThingsDataException;
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException;
    public List<List> getValues(String userID, String deviceEUI, int limit) throws ThingsDataException;
    public List<List> getValues(String userID, String deviceEUI, int limit, boolean tsFormat) throws ThingsDataException;
}
