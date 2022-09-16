/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.common.iot.Device;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DeviceGroup;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface IotDatabaseIface extends KeyValueDBIface {
    
    public void createStructure();

    public List<Device> getUserDevices(String userID, long organizationID, boolean withShared) throws ThingsDataException;

    public int getUserDevicesCount(String userID) throws ThingsDataException;

    public Device getDevice(String userID, long userType, String deviceEUI, boolean withShared) throws ThingsDataException;
    public boolean checkAccess(String userID, long userType, String deviceEUI, long organizationID, boolean withShared) throws ThingsDataException;

    public Device getDevice(String deviceEUI) throws ThingsDataException;
    public Device getDevice(String deviceEUI, String secretKey) throws ThingsDataException;

    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException;

    public List<DeviceTemplate> getDeviceTemplates() throws ThingsDataException;

    public void putDevice(Device device) throws ThingsDataException;

    public void updateDevice(Device device) throws ThingsDataException;
    public void updateDeviceStatus(String eui, Long lastSeen, Long lastFrame, Integer alertStatus, Double status, Integer statusExt, String downlink) throws ThingsDataException;

    //public void removeDevice(Device device) throws ThingsDataException;

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

    public List<Dashboard> getUserDashboards(String userID, boolean withShared) throws ThingsDataException;

    public void removeDashboard(String userID, String dashboardID) throws ThingsDataException;

    public Dashboard getDashboard(String userID, String dashboardID) throws ThingsDataException;
    
    public Dashboard getDashboardByName(String userID, String dashboardName) throws ThingsDataException;

    //public Dashboard getDashboard(String dashboardID) throws ThingsDataException;

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
    
    public List<List<List>> getValuesOfGroup(String userID, long organizationID, String groupEUI, String[] channelNames, long interval, DataQuery dataQuery) throws ThingsDataException;

    public void setDeviceStatus(String eui, Double state) throws ThingsDataException;
    public void setDeviceStatus(String eui, long lastSeen, long frameCounter, String downlink, int alertStatus, String deviceID) throws ThingsDataException;
    
    public void setDeviceAlertStatus(String eui, int status) throws ThingsDataException;
}
