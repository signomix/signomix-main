/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.Alert;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;
import org.cricketmsf.out.db.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class H2IotDB extends H2EmbededDB implements SqlDBIface, IotDatabaseIface, Adapter {

    private int timeOffset = 0;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        try {
            timeOffset = Integer.parseInt(properties.getOrDefault("time-offset", "0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Kernel.getInstance().getLogger().print("\ttime-offset: " + timeOffset);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {

        String query;
        StringBuilder sb = new StringBuilder();

        switch (tableName) {
            case "devicetemplates":
                sb.append("create table devicetemplates (")
                        .append("eui varchar primary key,")
                        .append("appID varchar,")
                        .append("appEUI varchar,")
                        .append("type varchar,")
                        .append("channels varchar,")
                        .append("code varchar,")
                        .append("decoder varchar,")
                        .append("description varchar,")
                        .append("interval bigint,")
                        .append("pattern varchar,")
                        .append("commandscript varchar,")
                        .append("producer varchar)");
                break;
            case "devices":
                sb.append("create table devices (")
                        .append("eui varchar primary key,")
                        .append("name varchar,")
                        .append("userid varchar,")
                        .append("type varchar,")
                        .append("team varchar,")
                        .append("channels varchar,")
                        .append("code varchar,")
                        .append("decoder varchar,")
                        .append("key varchar,")
                        .append("description varchar,")
                        .append("lastseen bigint,")
                        .append("interval bigint,")
                        .append("lastframe bigint,")
                        .append("template varchar,")
                        .append("pattern varchar,")
                        .append("downlink varchar,")
                        .append("commandscript varchar,")
                        .append("appid varchar,")
                        .append("appeui varchar)");
                break;
            case "dashboards":
                sb.append("create table dashboards (")
                        .append("id varchar primary key,")
                        .append("name varchar,")
                        .append("userid varchar,")
                        .append("title varchar,")
                        .append("team varchar,")
                        .append("widgets varchar,")
                        .append("token varchar,")
                        .append("shared boolean)");
                //TODO: widgets
                break;
            case "alerts":
                //TODO: jak jest zapisany userID?
                sb.append("create table alerts (")
                        .append("id bigint primary key,")
                        .append("name varchar,")
                        .append("category varchar,")
                        .append("type varchar,")
                        .append("deviceeui varchar,")
                        .append("userid varchar,")
                        .append("payload varchar,")
                        .append("timepoint varchar,")
                        .append("serviceid varchar,")
                        .append("uuid varchar,")
                        .append("calculatedtimepoint bigint,")
                        .append("createdat bigint,")
                        .append("rooteventid bigint,")
                        .append("cyclic boolean)");
                break;
            default:
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
        }
        query = sb.toString();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Device> getUserDevices(String userID, boolean withShared) throws ThingsDataException {
        String query;
        if(withShared){
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui from devices where userid = ? or team like ?";
        }else{
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui from devices where userid = ?";            
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            if(withShared){
                pstmt.setString(2, "%,"+userID+"%,");
            }
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Device> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildDevice(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Device getDevice(String userID, String deviceEUI, boolean withShared) throws ThingsDataException {

        String query;
        if(withShared){
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui from devices where eui=? and (userid = ? or team like ?)";
        }else{
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui from devices where eui=? and userid = ?";
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, deviceEUI);
            pstmt.setString(2, userID);
            if(withShared){
                pstmt.setString(3, "%,"+userID+",%");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Device device = buildDevice(rs);
                return device;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Device getDevice(String deviceEUI) throws ThingsDataException {
        String query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui from devices where upper(eui) = ?";
        if (deviceEUI == null || deviceEUI.isEmpty()) {
            return null;
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, deviceEUI.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Device device = buildDevice(rs);
                return device;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putDevice(Device device) throws ThingsDataException {
        if (getDevice(device.getUserID(), device.getEUI(), false) != null) {
            throw new ThingsDataException(ThingsDataException.CONFLICT, "device " + device.getEUI() + " is already defined");
        }
        String query = "insert into devices (eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval,lastframe,template,pattern,downlink,commandscript,appid,appeui) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, device.getEUI().toUpperCase());
            pstmt.setString(2, device.getName());
            pstmt.setString(3, device.getUserID());
            pstmt.setString(4, device.getType());
            pstmt.setString(5, device.getTeam());
            pstmt.setString(6, device.getChannelsAsString());
            pstmt.setString(7, device.getCode());
            pstmt.setString(8, device.getEncoder());
            pstmt.setString(9, device.getKey());
            pstmt.setString(10, device.getDescription());
            pstmt.setLong(11, device.getLastSeen());
            pstmt.setLong(12, device.getTransmissionInterval());
            pstmt.setLong(13, device.getLastFrame());
            pstmt.setString(14, device.getTemplate());
            pstmt.setString(15, device.getPattern());
            pstmt.setString(16, device.getDownlink());
            pstmt.setString(17, device.getCommandScript());
            pstmt.setString(18, device.getApplicationID());
            pstmt.setString(19, device.getApplicationEUI());
            int updated = pstmt.executeUpdate();
            conn.close();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "DB error adding device " + device.getEUI());
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDevice(Device device) throws ThingsDataException {
        String query = "update devices set name=?,userid=?,type=?,team=?,channels=?,code=?,decoder=?,key=?,description=?,lastseen=?,interval=?,lastframe=?,template=?,pattern=?,downlink=?,commandscript=?,appid=?,appeui=? where eui=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, device.getName());
            pstmt.setString(2, device.getUserID());
            pstmt.setString(3, device.getType());
            pstmt.setString(4, device.getTeam());
            pstmt.setString(5, device.getChannelsAsString());
            pstmt.setString(6, device.getCode());
            pstmt.setString(7, device.getEncoder());
            pstmt.setString(8, device.getKey());
            pstmt.setString(9, device.getDescription());
            pstmt.setLong(10, device.getLastSeen());
            pstmt.setLong(11, device.getTransmissionInterval());
            pstmt.setLong(12, device.getLastFrame());
            pstmt.setString(13, device.getTemplate());
            pstmt.setString(14, device.getPattern());
            pstmt.setString(15, device.getDownlink());
            pstmt.setString(16, device.getCommandScript());
            pstmt.setString(17, device.getApplicationID());
            pstmt.setString(18, device.getApplicationEUI());
            pstmt.setString(19, device.getEUI());
            //TODO: last frame
            int updated = pstmt.executeUpdate();
            conn.close();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "DB error updating device " + device.getEUI());
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDevice(Device device) throws ThingsDataException {
        removeDevice(device.getEUI());
    }

    @Override
    public void removeDevice(String deviceEUI) throws ThingsDataException {
        String query = "delete from devices where eui=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, deviceEUI);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllDevices(String userID) throws ThingsDataException {
        String query = "delete from devices where userid=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAuthorized(String userID, String deviceEUI) throws ThingsDataException {
        //System.out.println("H2IotDB.isAuthorized() userID"+userID+",deviceEUI:"+deviceEUI);
        //if("public".equalsIgnoreCase(userID)){
        //    return true;
        //}

        //TODO: access to virtual devices must be authorized for the Service
        String query;
        String query1 = "select eui from devices where eui = ? and (userid=? or team like ?)";
        String query2 = "select eui from devices where eui = ? and type = 'VIRTUAL'";
        if (userID == null) {
            query = query2;
        } else {
            query = query1;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, deviceEUI);
            if (userID != null) {
                pstmt.setString(2, userID);
                pstmt.setString(3, "%," + userID + ",%");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void addAlert(Event event) throws ThingsDataException {
        Alert alert = new Alert(event);
        String query = "insert into alerts (id,name,category,type,deviceeui,userid,payload,timepoint,serviceid,uuid,calculatedtimepoint,createdat,rooteventid,cyclic) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, alert.getId());
            pstmt.setString(2, alert.getName());
            pstmt.setString(3, alert.getCategory());
            pstmt.setString(4, alert.getType());
            pstmt.setString(5, alert.getDeviceEUI());
            pstmt.setString(6, alert.getUserID());
            pstmt.setString(7, alert.getPayload().toString());
            pstmt.setString(8, alert.getTimePoint());
            pstmt.setString(9, alert.getServiceId());
            pstmt.setString(10, alert.getServiceUuid().toString());
            pstmt.setLong(11, alert.getCalculatedTimePoint());
            pstmt.setLong(12, alert.getCreatedAt());
            pstmt.setLong(13, alert.getRootEventId());
            pstmt.setBoolean(14, alert.isCyclic());
            int updated = pstmt.executeUpdate();
            conn.close();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "DB error adding alert " + alert.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List getAlerts(String userID, boolean descending) throws ThingsDataException {
        String query = "select id,name,category,type,deviceeui,userid,payload,timepoint,serviceid,uuid,calculatedtimepoint,createdat,rooteventid,cyclic from alerts where userid = ? order by id";
        if (descending) {
            query = query.concat(" desc");
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Alert> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildAlert(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeAlert(long alertID) throws ThingsDataException {
        String query = "delete from alerts where id=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, alertID);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAlerts(String userID) throws ThingsDataException {
        String query = "delete from alerts where userid=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAlerts(String userID, long checkpoint) throws ThingsDataException {
        String query = "delete from alerts where userid=? and createdat < ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            pstmt.setLong(2, checkpoint);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeOutdatedAlerts(long checkpoint) throws ThingsDataException {
        String query = "delete from alerts where createdat < ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, checkpoint);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDashboard(Dashboard dashboard) throws ThingsDataException {
        String query = "insert into dashboards (id,name,userid,title,team,widgets,token,shared) values (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dashboard.getId());
            pstmt.setString(2, dashboard.getName());
            pstmt.setString(3, dashboard.getUserID());
            pstmt.setString(4, dashboard.getTitle());
            pstmt.setString(5, dashboard.getTeam());
            pstmt.setString(6, dashboard.getWidgetsAsJson());
            pstmt.setString(7, dashboard.getSharedToken());
            pstmt.setBoolean(8, dashboard.isShared());
            int updated = pstmt.executeUpdate();
            conn.close();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "DB error adding dashboard " + dashboard.getId());
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDeviceTemplate(DeviceTemplate device) throws ThingsDataException {
        String query = "insert into devicetemplates (eui,appid,appeui,type,channels,code,decoder,description,interval,pattern,commandscript,producer) values(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, device.getEUI());
            pstmt.setString(2, device.getApplicationID());
            pstmt.setString(3, device.getApplicationEUI());
            pstmt.setString(4, device.getType());
            pstmt.setString(5, device.getChannelsAsString());
            pstmt.setString(6, device.getCode());
            pstmt.setString(7, device.getEncoder());
            pstmt.setString(8, device.getDescription());
            pstmt.setLong(9, device.getTransmissionInterval());
            pstmt.setString(10, device.getPattern());
            pstmt.setString(11, device.getCommandScript());
            pstmt.setString(12, device.getProducer());

            int updated = pstmt.executeUpdate();
            conn.close();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "DB error adding device template" + device.getEUI());
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Device buildDevice(ResultSet rs) throws SQLException {
        //eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval
        Device d = new Device();
        d.setEUI(rs.getString(1));
        d.setName(rs.getString(2));
        d.setUserID(rs.getString(3));
        d.setType(rs.getString(4));
        d.setTeam(rs.getString(5));
        d.setChannels(rs.getString(6));
        d.setCode(rs.getString(7));
        d.setEncoder(rs.getString(8));
        d.setKey(rs.getString(9));
        d.setDescription(rs.getString(10));
        d.setLastSeen(rs.getLong(11));
        try {
            d.setTransmissionInterval(rs.getLong(12));
            d.setLastFrame(rs.getLong(13));
            d.setTemplate(rs.getString(14));
            d.setPattern(rs.getString(15));
            d.setDownlink(rs.getString(16));
            d.setCommandScript(rs.getString(17));
            d.setApplicationID(rs.getString(18));
            d.setApplicationEUI(rs.getString(19));
        } catch (Exception e) {
        }
        return d;
    }

    DeviceTemplate buildDeviceTemplate(ResultSet rs) throws SQLException {
        //eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval
        DeviceTemplate d = new DeviceTemplate();
        d.setEUI(rs.getString(1));
        d.setApplicationID(rs.getString(2));
        d.setApplicationEUI(rs.getString(3));
        d.setType(rs.getString(4));
        d.setChannels(rs.getString(5));
        d.setCode(rs.getString(6));
        d.setEncoder(rs.getString(7));
        d.setDescription(rs.getString(8));
        d.setTransmissionInterval(rs.getLong(9));
        d.setPattern(rs.getString(10));
        d.setCommandScript(rs.getString(11));
        d.setProducer(rs.getString(12));
        return d;
    }

    Alert buildAlert(ResultSet rs) throws SQLException {
        //id,name,category,type,deviceeui,userid,payload,timepoint,serviceid,uuid,calculatedtimepoint,createdat,rooteventid,cyclic        Device d = new Device();
        Alert a = new Alert();
        a.setId(rs.getLong(1));
        a.setName(rs.getString(2));
        a.setCategory(rs.getString(3));
        a.setType(rs.getString(4));
        a.setOrigin(rs.getString(6) + "\t" + rs.getString(5));
        a.setPayload(rs.getString(7));
        a.setTimePoint(rs.getString(8));
        a.setServiceId(rs.getString(9));
        a.setServiceUuid(UUID.fromString(rs.getString(10)));
        a.setCalculatedTimePoint(rs.getLong(11));
        a.setCreatedAt(rs.getLong(12));
        a.setRootEventId(rs.getLong(13));
        a.setCyclic(rs.getBoolean(14));
        return a;
    }

    Dashboard buildDashboard(ResultSet rs) throws SQLException {
        //id,name,userid,title,team,widgets,token,shared
        Dashboard a = new Dashboard();
        a.setId(rs.getString(1));
        a.setName(rs.getString(2));
        a.setUserID(rs.getString(3));
        a.setTitle(rs.getString(4));
        a.setTeam(rs.getString(5));
        a.setWidgetsFromJson(rs.getString(6));
        a.setSharedToken(rs.getString(7));
        a.setShared(rs.getBoolean(8));
        return a;
    }

    @Override
    public void removeUserDashboards(String userID) throws ThingsDataException {
        String query = "delete from dashboards where userid=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Dashboard> getUserDashboards(String userID, boolean withShared) throws ThingsDataException {
        String query;
        if (withShared) {
            query = "select id,name,userid,title,team,widgets,token,shared from dashboards where userid=? or team like ?";
        } else {
            query = "select id,name,userid,title,team,widgets,token,shared from dashboards where userid=?";
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            if (withShared) {
                pstmt.setString(2, "%," + userID + ",%");
            }
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Dashboard> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildDashboard(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeDashboard(String userID, String dashboardID) throws ThingsDataException {
        String query = "delete from dashboards where userid=? and id=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            pstmt.setString(2, dashboardID);
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dashboard getDashboard(String userID, String dashboardID) throws ThingsDataException {
        boolean publicUser = "public".equalsIgnoreCase(userID);
        String query = "select id,name,userid,title,team,widgets,token,shared from dashboards where id=? and (userid=? or team like ? ";
        if (publicUser) {
            query = query.concat("or shared=true)");
        } else {
            query = query.concat(")");
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dashboardID);
            pstmt.setString(2, userID);
            pstmt.setString(3, "%," + userID + ",%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return buildDashboard(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void updateDashboard(Dashboard dashboard) throws ThingsDataException {
        String query = "update dashboards set name=?,userid=?,title=?,team=?,widgets=?,token=?,shared=? where id=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dashboard.getName());
            pstmt.setString(2, dashboard.getUserID());
            pstmt.setString(3, dashboard.getTitle());
            pstmt.setString(4, dashboard.getTeam());
            pstmt.setString(5, dashboard.getWidgetsAsJson());
            pstmt.setString(6, dashboard.getSharedToken());
            pstmt.setBoolean(7, dashboard.isShared());
            pstmt.setString(8, dashboard.getId());
            int updated = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException {
        String query = "select id from dashboards where id=?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dashboardID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Dashboard getDashboard(String dashboardID) throws ThingsDataException {
        String query = "select id,name,userid,title,team,widgets,token,shared from dashboards where id=?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dashboardID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return buildDashboard(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    /*
    @Override
    public void updateStructure(Connection conn, String from, String to) throws KeyValueDBException {
        int fromVersion = 1;
        int toVersion = -1;
        try {
            fromVersion = Integer.parseInt(from);
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            toVersion = Integer.parseInt(to);
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            throw new KeyValueDBException(KeyValueDBException.CANNOT_WRITE, "cannot update database structure of " + this.getClass().getSimpleName());
        }
        for (int i = fromVersion; i < toVersion; i++) {
            updateTo(conn, i + 1);
        }
        super.updateStructure(conn, from, to);
    }
     */
    @Override
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
        String query = "";

        switch (versionNumber) {
            case 2:
                query = "alter table devices add lastframe bigint default -1;";
                break;
            case 3:
                StringBuilder sb = new StringBuilder();
                sb.append("alter table devices add template varchar default ''; ")
                        .append("alter table devices add pattern varchar default ''; ")
                        .append("alter table devices add downlink varchar default ''; ")
                        .append("alter table devices add commandscript varchar default ''; ")
                        .append("alter table devices add appid varchar default ''; ")
                        .append("alter table devices add appeui varchar default ''; ")
                        .append("drop table if exists devicetemplates; ")
                        .append("create table devicetemplates (")
                        .append("eui varchar primary key,")
                        .append("appid varchar,")
                        .append("appeui varchar,")
                        .append("type varchar,")
                        .append("channels varchar,")
                        .append("code varchar,")
                        .append("decoder varchar,")
                        .append("description varchar,")
                        .append("interval bigint,")
                        .append("pattern varchar,")
                        .append("commandscript varchar,")
                        .append("producer varchar)");
                query = sb.toString();
        }
        try {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException {
        String query = "select eui,appid,appeui,type,channels,code,decoder,description,interval,pattern,commandscript,producer from devicetemplates where upper(eui) = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, templateEUI.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DeviceTemplate device = buildDeviceTemplate(rs);
                return device;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<DeviceTemplate> getDeviceTemplates() throws ThingsDataException {
        String query = "select eui,aooid,appeui,type,channels,code,decoder,description,interval,pattern,commandscript,producer,application from devicetemplates";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<DeviceTemplate> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildDeviceTemplate(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public int getUserDevicesCount(String userID) throws ThingsDataException {
        String query = "select count(eui) from devices where userid = ?";
        int counter = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<DeviceTemplate> list = new ArrayList<>();
            if (rs.next()) {
                counter = Integer.parseInt(rs.getString(1));
            }
            return counter;
        } catch (SQLException | NumberFormatException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<Device> getInactiveDevices() throws ThingsDataException {
        String query
                = "SELECT eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,interval from devices "
                + "where interval > 0 and (datediff(S,dateadd(S, lastseen/1000, DATE '1970-01-01'),now())-" + timeOffset + ") > interval/1000;";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Device> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildDevice(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }
}
