/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import java.util.List;

import com.signomix.common.iot.ChannelData;
import com.signomix.common.iot.Device;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DeviceGroup;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.application.Application;

import org.cricketmsf.Event;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface IotDbDataIface extends KeyValueDBIface {

    public void createStructure();

    public List<Device> getUserDevices(String userID, long organizationID, boolean withShared) throws ThingsDataException;

    public int getUserDevicesCount(boolean fullData, String userID) throws ThingsDataException;

    public Device getDevice(String userID, long userType, String deviceEUI, boolean withShared) throws ThingsDataException;

    public Device getDevice(String deviceEUI) throws ThingsDataException;
    public Device getDevice(String deviceEUI, String secretKey) throws ThingsDataException;

    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException;

    public List<DeviceTemplate> getDeviceTemplates() throws ThingsDataException;

    public void putDevice(Device device) throws ThingsDataException;

    public void updateDevice(Device device) throws ThingsDataException;

    public boolean isAuthorized(String userID, long organizationID, String deviceEUI) throws ThingsDataException;

    public boolean isGroupAuthorized(String userID, long organizationID, String groupEUI) throws ThingsDataException;

    public void addAlert(Event alert) throws ThingsDataException;

    public List getAlerts(String userID, boolean descending) throws ThingsDataException;

    public void removeAlert(long alertID) throws ThingsDataException;

    public void removeAlerts(String userID) throws ThingsDataException;

    public void removeAlerts(String userID, long checkpoint) throws ThingsDataException;

    public void removeOutdatedAlerts(long checkpoint) throws ThingsDataException;

    public void removeDevice(String deviceEUI) throws ThingsDataException;

    public void removeAllDevices(String userID) throws ThingsDataException;

    public void addDashboard(Dashboard dashboard) throws ThingsDataException;

    public void addDeviceTemplate(DeviceTemplate template) throws ThingsDataException;

    public void removeUserDashboards(String userID) throws ThingsDataException;

    public List<Dashboard> getUserDashboards(String userID, boolean withShared, boolean adminRole) throws ThingsDataException;

    public void removeDashboard(String userID, String dashboardID) throws ThingsDataException;

    public Dashboard getDashboard(String userID, String dashboardID, boolean adminRole) throws ThingsDataException;

    public Dashboard getDashboardByName(String userID, String dashboardName) throws ThingsDataException;

    public void updateDashboard(Dashboard dashboard) throws ThingsDataException;

    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException;

    public List<Device> getInactiveDevices() throws ThingsDataException;

    public List<Device> getGroupDevices(String userID, long organizationID, String groupID) throws ThingsDataException;

    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException;

    public DeviceGroup getGroup(String userId, String groupEUI) throws ThingsDataException;

    public List<DeviceGroup> getUserGroups(String userID) throws ThingsDataException;

    public void putGroup(DeviceGroup group) throws ThingsDataException;

    public void updateGroup(DeviceGroup group) throws ThingsDataException;

    public void removeGroup(String groupEUI) throws ThingsDataException;

    public List<String> getGroupChannels(String groupEUI) throws ThingsDataException;

    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException;

    public int getChannelIndex(String deviceEUI, String channel) throws ThingsDataException;

    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException;

    public void putDeviceChannels(String deviceEUI, List<String> channelNames) throws ThingsDataException;

    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException;

    public int updateDeviceChannels(Device device, Device oldDevice) throws ThingsDataException;

    public void removeAllChannels(String deviceEUI) throws ThingsDataException;

    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException;

    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values) throws ThingsDataException;

    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException; //T

    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException; //T

    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException; //T
    public List<List<List>> getGroupLastValues(String userID, long organizationID, String groupEUI, String[] channelNames, long secondsBack) throws ThingsDataException; //T
    public List<List<List>> getGroupLastValues(String userID, long organizationID, String groupEUI, String[] channelNames, DataQuery dQuery) throws ThingsDataException;
    public List<List<List>> getValuesOfGroup(String userID, long organizationID, String groupEUI, String[] channelNames, long interval, DataQuery dQuery) throws ThingsDataException;

    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException; //TT

    public List<List> getDeviceMeasures(String userID, String deviceEUI, String query) throws ThingsDataException;

    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException;

    /**
     *
     * @param deviceEUI
     * @return
     * @throws ThingsDataException
     */
    public Event getFirstCommand(String deviceEUI) throws ThingsDataException;

    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException;

    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException;

    public void removeAllCommands(String deviceEUI) throws ThingsDataException;

    public void removeCommand(long id) throws ThingsDataException;

    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException;

    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException;

    public void clearAllLogs(String deviceEUI, long checkPoint) throws ThingsDataException;

    public void removeAllLogs(String deviceEUI) throws ThingsDataException;

    public List<Event> getAllLogs(String deviceEUI) throws ThingsDataException;


    public Application addApplication(Application application) throws ThingsDataException;
    public void updateApplication(Application application) throws ThingsDataException;
    public void removeApplication(long id) throws ThingsDataException;
    public Application getApplication(long id) throws ThingsDataException;
    public Application getApplication(String name) throws ThingsDataException;
    public List<Application> getAllApplications() throws ThingsDataException;
    public List<Application> getApplications(long organizationId) throws ThingsDataException;
}
