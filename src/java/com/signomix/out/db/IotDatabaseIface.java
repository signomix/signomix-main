/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.db;

import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.Device;
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
    
    public List<Device> getUserDevices(String userID) throws ThingsDataException;
    public int getUserDevicesCount(String userID) throws ThingsDataException;
    public Device getDevice(String userID, String deviceEUI) throws ThingsDataException;
    public Device getDevice(String deviceEUI) throws ThingsDataException;
    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException;
    public List<DeviceTemplate> getDeviceTemplates() throws ThingsDataException;
    public void putDevice(Device device) throws ThingsDataException;
    public void updateDevice(Device device) throws ThingsDataException;
    public void removeDevice(Device device) throws ThingsDataException;
    public boolean isAuthorized(String userID, String deviceEUI) throws ThingsDataException;
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
    public Dashboard getDashboard(String dashboardID) throws ThingsDataException;
    public void updateDashboard(Dashboard dashboard) throws ThingsDataException;
    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException;
}
