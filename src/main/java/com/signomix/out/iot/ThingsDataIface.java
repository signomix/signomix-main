/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import java.util.List;

import com.signomix.common.iot.ChannelData;
import com.signomix.common.iot.Device;

import org.cricketmsf.Event;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface ThingsDataIface {
    public void init(String helperName, String helperName2) throws ThingsDataException;
    
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData>values) throws ThingsDataException;
    //public void putVirtualData(String userID, Device device, ScriptingAdapterIface scriptingAdapter, List<ChannelData>values) throws ThingsDataException;
    public void putData(String userID, String deviceEUI, String project, Double deviceState, String channel, ChannelData value) throws ThingsDataException;
    
    
    //public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException;
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException;
    //public List<List> getValues(String userID, String deviceEUI, String channel, String query) throws ThingsDataException;
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException;
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException;
    //public List<List> getValues(String userID, String deviceEUI, int limit) throws ThingsDataException;
    
    
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException;
    //public List getChannels(String deviceEUI) throws ThingsDataException;
    public void removeAllChannels(String deviceEUI) throws ThingsDataException;
    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException;
    public void clearAllChannelsLimit(String deviceEUI, long limit) throws ThingsDataException;
    
    
    public boolean isAuthorized(String userIF, String deviceEUI) throws ThingsDataException;
    public boolean isGroupAuthorized(String userIF, String groupEUI) throws ThingsDataException;
    public void putDevice(String userID, Device device) throws ThingsDataException;
    public void modifyDevice(String userID, Device device) throws ThingsDataException;
    public void updateHealthStatus(String id, long lastSeen, long frameCounter, String downlink, String deviceID) throws ThingsDataException;
    public void updateAlertStatus(String id, int status) throws ThingsDataException;
    public void updateDeviceState(String id, Double state) throws ThingsDataException;
    public Device getDevice(String userId, String deviceEUI, boolean withShared) throws ThingsDataException;
    public Device getDevice(String deviceEUI) throws ThingsDataException;
    public List<Device> getUserDevices(String userID, boolean withShared) throws ThingsDataException;
    public List<Device> getGroupDevices(String userID, String group) throws ThingsDataException;
    public int getUserDevicesCount(String userID) throws ThingsDataException;
    public void removeDevice(String deviceEUI) throws ThingsDataException;
    public void removeAllDevices(String userId) throws ThingsDataException;
    
    public void saveAlert(Event event) throws ThingsDataException;
    public void removeAlert(long alertId) throws ThingsDataException;
    public List getAlerts(String userId) throws ThingsDataException;
    public void removeUserAlerts(String userId) throws ThingsDataException;
    public void removeUserAlerts(String userId, long checkPoint) throws ThingsDataException;
    public void removeOutdatedAlerts(long checkPoint) throws ThingsDataException;
    public void removeUserAlertsLimit(String userId, long limit) throws ThingsDataException;
    
    public List<Device> getInactiveDevices() throws ThingsDataException;
    
    public List <DeviceGroup> getUserGroups(String userID) throws ThingsDataException;
    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException;
    public DeviceGroup getGroup(String userId, String groupEUI) throws ThingsDataException;
    public void putGroup(String userID, DeviceGroup group) throws ThingsDataException;
    public void modifyGroup(String userID, DeviceGroup group) throws ThingsDataException;
    public void removeGroup(String userID, String groupEUI) throws ThingsDataException;
    public List<List> getValuesOfGroup(String userID, String groupEUI, String[] channelNames) throws ThingsDataException;
    public List<List> getLastValuesOfGroup(String userID, String groupEUI, String[] channelNames, long interval) throws ThingsDataException;

    public List<DeviceTemplate> getTemplates() throws ThingsDataException;
}
