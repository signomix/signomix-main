/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.signomix.common.iot.Device;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.Alert;
import com.signomix.out.iot.AlertOwnerComparator;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DeviceGroup;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;

import org.cricketmsf.Event;
import org.cricketmsf.out.db.KeyValueDB;
import org.cricketmsf.out.db.KeyValueDBException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class KeyValueIotDB extends KeyValueDB implements IotDatabaseIface {

    @Override
    public void createStructure(){
    }
    @Override
    public List<Device> getUserDevices(String userID, long organizationID, boolean withShared) throws ThingsDataException {
        try {
            Map map = getAll("devices");
            ArrayList list = new ArrayList();
            Iterator itr = map.values().iterator();
            Device d;
            while (itr.hasNext()) {
                d = (Device) itr.next();
                if (d.getUserID().equals(userID) || d.getTeam().contains("," + userID + ",")) {
                    list.add(d);
                }
            }
            return list;
        } catch (NullPointerException | KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Device getDevice(String userID, long userType, String deviceEUI, boolean withShared) throws ThingsDataException {
        Device device = getDevice(deviceEUI);
        if (device != null && !(device.getUserID().equals(userID) || device.getTeam().contains("," + userID + ","))) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return device;
    }

    @Override
    public void putDevice(Device device) throws ThingsDataException {
        try {
            if (containsKey("devices", device.getEUI())) {
                throw new ThingsDataException(ThingsDataException.CONFLICT, "device " + device.getEUI() + "is already defined");
            }
            device.setEUI(device.getEUI());
            put("devices", device.getEUI(), device);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Device getDevice(String deviceEUI) throws ThingsDataException {
        try {
            Device device = (Device) get("devices", deviceEUI);
            return device;
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void updateDevice(Device device) throws ThingsDataException {
        try {
            if (!containsKey("devices", device.getEUI())) {
                throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found " + device.getEUI());
            }
            put("devices", device.getEUI(), device);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public boolean isAuthorized(String userID, long organizationID, String deviceEUI) throws ThingsDataException {
        //if("public".equalsIgnoreCase(userID)){
        //    return true;
        //}
        try {
            Device device = (Device) get("devices", deviceEUI);
            if (device != null) {
                //TODO: "public" must be team member
                if (device.isVirtual() && userID == null) {
                    return true;
                } else {
                    return device.getUserID().equals(userID) || device.userIsTeamMember(userID);
                }
            } else {
                return false;
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void addAlert(Event event) throws ThingsDataException {
        Alert alert = new Alert(event);
        try {
            put("alerts", "" + alert.getId(), alert);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public List getAlerts(String userID, boolean descending) throws ThingsDataException {
        try {
            List l = search("alerts", new AlertOwnerComparator(), userID);
            if (descending) {
                ArrayList l2 = new ArrayList();
                for (int i = l.size() - 1; i >= 0; i--) {
                    l2.add(l.get(i));
                }
                return l2;
            } else {
                return l;
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeAlert(long alertID) throws ThingsDataException {
        try {
            remove("alerts", "" + alertID);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeAlerts(String userID) throws ThingsDataException {
        List<Event> l = getAlerts(userID, false);
        try {
            for (int i = 0; i < l.size(); i++) {
                remove("alerts", "" + l.get(i).getId());
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeAlerts(String userID, long checkpoint) throws ThingsDataException {
        List<Event> alerts = getAlerts(userID, false);
        for (int i = 0; i < alerts.size(); i++) {
            if (alerts.get(i).getCreatedAt() < checkpoint) {
                removeAlert(alerts.get(i).getId());
            }
        }
    }

    @Override
    public void removeOutdatedAlerts(long checkpoint) throws ThingsDataException {
        try {
            Map<String, Event> alerts = getAll("alerts");

            for (Event alert : alerts.values()) {
                if (alert.getCreatedAt() < checkpoint) {
                    removeAlert(alert.getId());
                }
            }
        } catch (KeyValueDBException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeDevice(String deviceEUI) throws ThingsDataException {
        try {
            remove("devices", deviceEUI);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeAllDevices(String userID) throws ThingsDataException {
        List devices = getUserDevices(userID, -1, false);
        Device d;
        for (int i = 0; i < devices.size(); i++) {
            d = (Device) devices.get(i);
            removeDevice(d.getEUI());
        }
    }

    @Override
    public void addDashboard(Dashboard dashboard) throws ThingsDataException {
        try {
            if (containsKey("dashboards", dashboard.getId())) {
                throw new ThingsDataException(ThingsDataException.UNKNOWN, "this dashboard ID already exists");
            }
            put("dashboards", dashboard.getId(), dashboard);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void addDeviceTemplate(DeviceTemplate template) throws ThingsDataException {
        try {
            if (containsKey("devicetemplates", template.getEui())) {
                throw new ThingsDataException(ThingsDataException.UNKNOWN, "this template EUI already exists");
            }
            put("devicetemplates", template.getEui(), template);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeUserDashboards(String userID) throws ThingsDataException {
        List dashboards = getUserDashboards(userID, false, false);
        Dashboard d;
        for (int i = 0; i < dashboards.size(); i++) {
            d = (Dashboard) dashboards.get(i);
            removeDashboard(userID, d.getId());
        }
    }

    @Override
    public List<Dashboard> getUserDashboards(String userID, boolean withShared, boolean adminRole) throws ThingsDataException {
        try {
            Map<String, Dashboard> map = getAll("dashboards");
            ArrayList<Dashboard> result = new ArrayList<>();
            map.entrySet().stream().filter(
                    (entry) -> (userID.equals(entry.getValue().getUserID()) || entry.getValue().isTeamMember(userID))
            ).forEachOrdered(
                    (entry) -> {
                        result.add(entry.getValue());
                    }
            );
            return result;
        } catch (NullPointerException | KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeDashboard(String userID, String dashboardID) throws ThingsDataException {
        try {
            Dashboard dashboard = (Dashboard) get("dashboards", dashboardID);
            if (!dashboard.getUserID().equals(userID)) {
                throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
            }
            remove("dashboards", dashboardID);
        } catch (ClassCastException | KeyValueDBException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dashboard getDashboard(String userID, String dashboardID, boolean adminRole) throws ThingsDataException {
        try {
            Dashboard dashboard = (Dashboard) get("dashboards", dashboardID);
            //System.out.println("DASHBOARD="+);
            if (dashboard == null) {
                throw new ThingsDataException(ThingsDataException.NOT_FOUND, "not found");
            }
            boolean ok = false;
            if (dashboard.getUserID().equals(userID) || (dashboard.isShared() && "public".equals(userID))) {
                ok = true;
                //throw new DashboardException(DashboardException.NOT_AUTHORIZED, "not authorized");
            } else if (dashboard.isTeamMember(userID)) {
                ok = true;
                //throw new DashboardException(DashboardException.NOT_AUTHORIZED, "not authorized");
            }
            if (!ok) {
                throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
            }
            return dashboard;
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Dashboard getDashboardByName(String userID, String dashboardName) throws ThingsDataException {
        throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, "not implemented");
    }

    @Override
    public void updateDashboard(Dashboard dashboard) throws ThingsDataException {
        try {
            put("dashboards", dashboard.getId(), dashboard);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException {
        try {
            return containsKey("dashboards", dashboardID);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    /*@Override
    public Dashboard getDashboard(String dashboardID) throws ThingsDataException {
        try {
            return (Dashboard) get("dashboards", dashboardID);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }
     */
    @Override
    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<DeviceTemplate> getDeviceTemplates() throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getUserDevicesCount(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Device> getInactiveDevices() throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Device> getGroupDevices(String userID, long organizationID, String groupID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceGroup getGroup(String userId, String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<DeviceGroup> getUserGroups(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putGroup(DeviceGroup group) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateGroup(DeviceGroup group) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeGroup(String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getGroupChannels(String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isGroupAuthorized(String userID, long organizationID, String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDeviceStatus(String eui, Double state) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDeviceAlertStatus(String eui, int status) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDeviceStatus(String eui, long lastSeen, long frameCounter, String downlink, int alertStatus, String deviceID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    @Override
    public List<List<List>> getValuesOfGroup(String userID, long organizationID, String groupEUI, String[] channelNames, long interval,
            DataQuery dataQuery) throws ThingsDataException {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean checkAccess(String userID, long userType, String deviceEUI, long organizationID, boolean withShared)
            throws ThingsDataException {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public void updateDeviceStatus(String eui, Long lastSeen, Long lastFrame, Integer alertStatus, Double status,
            Integer statusExt, String downlink) throws ThingsDataException {
        // TODO Auto-generated method stub
        
    }
    @Override
    public Device getDevice(String deviceEUI, String secretKey) throws ThingsDataException {
        // TODO Auto-generated method stub
        return null;
    }

}
