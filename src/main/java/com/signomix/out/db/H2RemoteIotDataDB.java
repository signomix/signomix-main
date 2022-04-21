/**
 * Copyright (C) Grzegorz Skorupa 2021.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.cedarsoftware.util.io.JsonWriter;
import com.signomix.out.db.dto.DeviceChannelDto;
import com.signomix.out.db.dto.DeviceDataDto;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.iot.Alert;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DataQueryException;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.DeviceGroup;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.archiver.ZipArchiver;
import org.cricketmsf.out.db.H2RemoteDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;
import org.slf4j.LoggerFactory;

public class H2RemoteIotDataDB extends H2RemoteDB
        implements SqlDBIface, IotDbDataIface, ActuatorCommandsDBIface, IotDatabaseIface, IotDataStorageIface,
        Adapter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(H2RemoteIotDataDB.class);
    private int requestLimit = 0; // no limit
    private int timeOffset = 0;
    private static final int MAX_CONNECTIONS = 400;
    private long defaultGroupInterval = 60 * 60 * 1000; // 60 minutes

    @Override
    public void createDatabase(Connection conn, String version) {
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public void start() throws KeyValueDBException {
        super.start();
        cp.setMaxConnections(MAX_CONNECTIONS);
    }

    @Override
    public void createStructure() {
        Kernel.handle(Event.logInfo(this.getClass().getSimpleName(), "createStructure()"));
        String query;
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS devicetemplates (").append("eui varchar primary key,")
                .append("appid varchar,")
                .append("appeui varchar,").append("type varchar,").append("channels varchar,")
                .append("code varchar,").append("decoder varchar,").append("description varchar,")
                .append("tinterval bigint,").append("pattern varchar,").append("commandscript varchar,")
                .append("producer varchar);");
        sb.append("CREATE TABLE IF NOT EXISTS devices (")
                .append("eui varchar primary key,").append("name varchar,")
                .append("userid varchar,").append("type varchar,").append("team varchar,")
                .append("channels varchar,").append("code varchar,").append("decoder varchar,")
                .append("key varchar,").append("description varchar,").append("lastseen bigint,")
                .append("tinterval bigint,").append("lastframe bigint,").append("template varchar,")
                .append("pattern varchar,").append("downlink varchar,").append("commandscript varchar,")
                .append("appid varchar,").append("groups varchar,").append("alert number,")
                .append("appeui varchar,").append("devid varchar,").append("active boolean,")
                .append("project varchar,").append("latitude double,").append("longitude double,")
                .append("altitude double,").append("state double,").append("retention bigint,")
                .append("administrators varchar, framecheck boolean);");
        sb.append("CREATE TABLE IF NOT EXISTS dashboards (").append("id varchar primary key,")
                .append("name varchar,")
                .append("userid varchar,").append("title varchar,").append("team varchar,")
                .append("widgets varchar,").append("token varchar,").append("shared boolean,")
                .append("administrators varchar);");
        sb.append("CREATE TABLE IF NOT EXISTS alerts (").append("id bigint primary key,").append("name varchar,")
                .append("category varchar,").append("type varchar,").append("deviceeui varchar,")
                .append("userid varchar,").append("payload varchar,").append("timepoint varchar,")
                .append("serviceid varchar,").append("uuid varchar,").append("calculatedtimepoint bigint,")
                .append("createdat bigint,").append("rooteventid bigint,").append("cyclic boolean);");
        sb.append("CREATE TABLE IF NOT EXISTS devicechannels (").append("eui varchar primary key,")
                .append("channels varchar);");
        sb.append("CREATE TABLE IF NOT EXISTS devicedata (").append("eui varchar not null,").append("userid varchar,")
                .append("day date,").append("dtime time,").append("tstamp timestamp not null,").append("d1 double,")
                .append("d2 double,").append("d3 double,").append("d4 double,").append("d5 double,")
                .append("d6 double,").append("d7 double,").append("d8 double,").append("d9 double,")
                .append("d10 double,").append("d11 double,").append("d12 double,").append("d13 double,")
                .append("d14 double,").append("d15 double,").append("d16 double,").append("d17 double,")
                .append("d18 double,").append("d19 double,").append("d20 double,").append("d21 double,")
                .append("d22 double,").append("d23 double,").append("d24 double,").append("project varchar,")
                .append("state double,")
                .append("PRIMARY KEY (eui,tstamp) );");
        sb.append("CREATE TABLE IF NOT EXISTS groups (").append("eui varchar primary key,").append("name varchar,")
                .append("userid varchar,").append("team varchar,").append("channels varchar,")
                .append("description varchar,")
                .append("administrators varchar);");
        sb.append("CREATE TABLE IF NOT EXISTS commands (")
                .append("id bigint,")
                .append("category varchar,")
                .append("type varchar,")
                .append("origin varchar,")
                .append("payload varchar,")
                .append("createdat bigint);");
        sb.append("CREATE INDEX IF NOT EXISTS idxcommands on commands(origin);");
        sb.append("CREATE TABLE IF NOT EXISTS commandslog (")
                .append("id bigint,")
                .append("category varchar,")
                .append("type varchar,")
                .append("origin varchar,")
                .append("payload varchar,")
                .append("createdat bigint);");
        sb.append("CREATE INDEX IF NOT EXISTS idxcommandslog on commandslog(origin);");
        query = sb.toString();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "createStructure() " + e.getMessage()));
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        try {
            timeOffset = Integer.parseInt(properties.getOrDefault("time-offset", "0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Kernel.getInstance().getLogger().print("\ttime-offset: " + timeOffset);
        try {
            requestLimit = Integer.parseInt(properties.getOrDefault("requestLimit", "500"));
            Kernel.getInstance().getLogger().print("\trequestLimit: " + requestLimit);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public File getBackupFile() {
        try {
            ZipArchiver archiver = new ZipArchiver("data-", ".zip");
            Map args = new HashMap();
            args.put(JsonWriter.TYPE, true);
            args.put(JsonWriter.PRETTY_PRINT, true);
            Map map;
            map = getAll("devicetemplates");
            String json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("devicetenplates.json", json);
            map = getAll("devices");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("devices.json", json);
            map = getAll("dashboards");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("dashboards.json", json);
            map = getAll("alerts");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("alerts.json", json);
            map = getAll("groups");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("groups.json", json);
            map = getAll("devicechannels");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("channels.json", json);
            map = getAll("devicedata");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("data.json", json);
            map = getAll("commands");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("commands.json", json);
            map = getAll("commandslog");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("commandslog.json", json);
            return archiver.getFile();
        } catch (KeyValueDBException | IOException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return null;
        }
    }

    @Override
    public List<Device> getUserDevices(String userID, boolean withShared) throws ThingsDataException {
        String query;
        if (withShared) {
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid, active, project, latitude,longitude,altitude,state,retention,administrators,framecheck from devices where userid = ? or team like ? or administrators like ?";
        } else {
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project, latitude,longitude,altitude,state,retention,administrators,framecheck from devices where userid = ?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            if (withShared) {
                pstmt.setString(2, "%," + userID + ",%");
                pstmt.setString(3, "%," + userID + ",%");
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
    public List<Device> getGroupDevices(String userID, String groupID) throws ThingsDataException {
        List<Device> devices;
        DeviceGroup group = getGroup(userID, groupID);
        if (null == group
                || (!group.isOpen() && !userID.equals(group.getUserID()) && !group.userIsTeamMember(userID))) {
            return new ArrayList();
        }
        String query;
        query = "select * from devices where groups like ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, "%," + groupID + ",%");
            ResultSet rs = pstmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                devices.add(buildDevice(rs));
            }
            return devices;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Device getDevice(String userID, String deviceEUI, boolean withShared) throws ThingsDataException {

        String query;
        if (withShared) {
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project,latitude,longitude,altitude,state,retention,administrators,framecheck from devices where upper(eui)=upper(?) and (userid = ? or team like ? or administrators like ?)";
        } else {
            query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project,latitude,longitude,altitude,state,retention,administrators,framecheck from devices where upper(eui)=upper(?) and userid = ?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, deviceEUI);
            pstmt.setString(2, userID);
            if (withShared) {
                pstmt.setString(3, "%," + userID + ",%");
                pstmt.setString(4, "%," + userID + ",%");
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
        String query = "select eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project,latitude,longitude,altitude,state,retention,administrators,framecheck from devices where upper(eui) = upper(?)";
        if (deviceEUI == null || deviceEUI.isEmpty()) {
            return null;
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, deviceEUI);
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
            throw new ThingsDataException(ThingsDataException.CONFLICT,
                    "device " + device.getEUI() + " is already defined");
        }
        String query = "insert into devices (eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project,latitude,longitude,altitude,state,retention,administrators,framecheck) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, device.getEUI());
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
            pstmt.setString(20, device.getGroups());
            pstmt.setInt(21, device.getAlertStatus());
            pstmt.setString(22, device.getDeviceID());
            pstmt.setBoolean(23, device.isActive());
            pstmt.setString(24, device.getProject());
            pstmt.setDouble(25, device.getLatitude());
            pstmt.setDouble(26, device.getLongitude());
            pstmt.setDouble(27, device.getAltitude());
            pstmt.setDouble(28, device.getState());
            pstmt.setLong(29, device.getRetentionTime());
            pstmt.setString(30, device.getAdministrators());
            pstmt.setBoolean(31, device.isCheckFrames());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding device " + device.getEUI());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDevice(Device device) throws ThingsDataException {
        //Device previous = getDevice(device.getEUI());
        String query = "update devices set name=?,userid=?,type=?,team=?,channels=?,code=?,decoder=?,key=?,description=?,lastseen=?,tinterval=?,lastframe=?,template=?,pattern=?,downlink=?,commandscript=?,appid=?,appeui=?,groups=?,alert=?,devid=?,active=?,project=?,latitude=?,longitude=?,altitude=?,state=?, retention=?, administrators=?, framecheck=? where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
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
            pstmt.setString(19, device.getGroups());
            pstmt.setInt(20, device.getAlertStatus());
            pstmt.setString(21, device.getDeviceID());
            pstmt.setBoolean(22, device.isActive());
            pstmt.setString(23, device.getProject());
            pstmt.setDouble(24, device.getLatitude());
            pstmt.setDouble(25, device.getLongitude());
            pstmt.setDouble(26, device.getAltitude());
            pstmt.setDouble(27, device.getState());
            pstmt.setLong(28, device.getRetentionTime());
            pstmt.setString(29, device.getAdministrators());
            pstmt.setBoolean(30, device.isCheckFrames());
            pstmt.setString(31, device.getEUI());
            // TODO: last frame
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error updating device " + device.getEUI());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDevice(String deviceEUI) throws ThingsDataException {
        String query = "delete from devices where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, deviceEUI);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllDevices(String userID) throws ThingsDataException {
        String query = "delete from devices where userid=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAuthorized(String userID, String deviceEUI) throws ThingsDataException {
        // TODO: Should be access to virtual devices authorized for the Service?
        String query;
        String query1 = "select eui from devices where upper(eui) = upper(?) and (userid=? or team like ? or administrators like ?)";
        String query2 = "select eui from devices where upper(eui) = upper(?) and type = 'VIRTUAL'";
        if (userID == null) {
            query = query2;
        } else {
            query = query1;
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, deviceEUI);
            if (userID != null) {
                pstmt.setString(2, userID);
                pstmt.setString(3, "%," + userID + ",%");
                pstmt.setString(4, "%," + userID + ",%");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public boolean isGroupAuthorized(String userID, String groupEUI) throws ThingsDataException {
        String query = "select eui from groups where upper(eui) = upper(?) and (userid=? or team like ? or administrators like ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI);
            if (userID != null) {
                pstmt.setString(2, userID);
                pstmt.setString(3, "%," + userID + ",%");
                pstmt.setString(4, "%," + userID + ",%");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
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
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setLong(1, alert.getId());
            pstmt.setString(2, alert.getName());
            pstmt.setString(3, alert.getCategory());
            pstmt.setString(4, alert.getType());
            pstmt.setString(5, alert.getDeviceEUI());
            pstmt.setString(6, alert.getUserID());
            pstmt.setString(7, (null != alert.getPayload()) ? alert.getPayload().toString() : "");
            pstmt.setString(8, alert.getTimePoint());
            pstmt.setString(9, alert.getServiceId());
            pstmt.setString(10, alert.getServiceUuid().toString());
            pstmt.setLong(11, alert.getCalculatedTimePoint());
            pstmt.setLong(12, alert.getCreatedAt());
            pstmt.setLong(13, alert.getRootEventId());
            pstmt.setBoolean(14, alert.isCyclic());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding alert " + alert.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List getAlerts(String userID, boolean descending) throws ThingsDataException {
        String query = "select id,name,category,type,deviceeui,userid,payload,timepoint,serviceid,uuid,calculatedtimepoint,createdat,rooteventid,cyclic from alerts where userid = ? order by id ";
        if (descending) {
            query = query.concat(" desc");
        }
        query = query.concat(" limit ?");
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            pstmt.setLong(2, requestLimit);
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
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setLong(1, alertID);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAlerts(String userID) throws ThingsDataException {
        String query = "delete from alerts where userid=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
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
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            pstmt.setLong(2, checkpoint);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeOutdatedAlerts(long checkpoint) throws ThingsDataException {
        String query = "delete from alerts where createdat < ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setLong(1, checkpoint);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDashboard(Dashboard dashboard) throws ThingsDataException {
        String query = "insert into dashboards (id,name,userid,title,team,widgets,token,shared,administrators) values (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboard.getId());
            pstmt.setString(2, dashboard.getName());
            pstmt.setString(3, dashboard.getUserID());
            pstmt.setString(4, dashboard.getTitle());
            pstmt.setString(5, dashboard.getTeam());
            pstmt.setString(6, dashboard.getWidgetsAsJson());
            pstmt.setString(7, dashboard.getSharedToken());
            pstmt.setBoolean(8, dashboard.isShared());
            pstmt.setString(9, dashboard.getAdministrators());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding dashboard " + dashboard.getId());
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDeviceTemplate(DeviceTemplate device) throws ThingsDataException {
        String query = "insert into devicetemplates (eui,appid,appeui,type,channels,code,decoder,description,tinterval,pattern,commandscript,producer) values(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, device.getEui());
            pstmt.setString(2, device.getAppid());
            pstmt.setString(3, device.getAppeui());
            pstmt.setString(4, device.getType());
            pstmt.setString(5, device.getChannels());
            pstmt.setString(6, device.getCode());
            pstmt.setString(7, device.getDecoder());
            pstmt.setString(8, device.getDescription());
            pstmt.setInt(9, device.getInterval());
            pstmt.setString(10, device.getPattern());
            pstmt.setString(11, device.getCommandScript());
            pstmt.setString(12, device.getProducer());

            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding device template" + device.getEui());
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Device buildDevice(ResultSet rs) throws SQLException {
        // eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval
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
        d.setTransmissionInterval(rs.getLong(12));
        d.setLastFrame(rs.getLong(13));
        d.setTemplate(rs.getString(14));
        d.setPattern(rs.getString(15));
        d.setDownlink(rs.getString(16));
        d.setCommandScript(rs.getString(17));
        d.setApplicationID(rs.getString(18));
        d.setApplicationEUI(rs.getString(19));
        d.setGroups(rs.getString(20));
        d.setAlertStatus(rs.getInt(21));
        d.setDeviceID(rs.getString(22));
        d.setActive(rs.getBoolean(23));
        d.setProject(rs.getString(24));
        d.setLatitude(rs.getDouble(25));
        d.setLongitude(rs.getDouble(26));
        d.setAltitude(rs.getDouble(27));
        d.setState(rs.getDouble(28));
        d.setRetentionTime(rs.getLong(29));
        d.setAdministrators(rs.getString(30));
        d.setCheckFrames(rs.getBoolean(31));
        return d;
    }

    DeviceTemplate buildDeviceTemplate(ResultSet rs) throws SQLException {
        // eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval
        DeviceTemplate d = new DeviceTemplate();
        d.setEui(rs.getString(1));
        d.setAppid(rs.getString(2));
        d.setAppeui(rs.getString(3));
        d.setType(rs.getString(4));
        d.setChannels(rs.getString(5));
        d.setCode(rs.getString(6));
        d.setDecoder(rs.getString(7));
        d.setDescription(rs.getString(8));
        d.setInterval(rs.getInt(9));
        d.setPattern(rs.getString(10));
        d.setCommandScript(rs.getString(11));
        d.setProducer(rs.getString(12));
        return d;
    }

    Alert buildAlert(ResultSet rs) throws SQLException {
        // id,name,category,type,deviceeui,userid,payload,timepoint,serviceid,uuid,calculatedtimepoint,createdat,rooteventid,cyclic
        // Device d = new Device();
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
        // id,name,userid,title,team,widgets,token,shared
        Dashboard a = new Dashboard();
        a.setId(rs.getString(1));
        a.setName(rs.getString(2));
        a.setUserID(rs.getString(3));
        a.setTitle(rs.getString(4));
        a.setTeam(rs.getString(5));
        a.setWidgetsFromJson(rs.getString(6));
        a.setSharedToken(rs.getString(7));
        a.setShared(rs.getBoolean(8));
        a.setAdministrators(rs.getString(9));
        return a;
    }

    DeviceGroup buildGroup(ResultSet rs) throws SQLException {
        // eui,name,userid,team,description
        DeviceGroup d = new DeviceGroup();
        d.setEUI(rs.getString(1));
        d.setName(rs.getString(2));
        d.setUserID(rs.getString(3));
        d.setTeam(rs.getString(4));
        d.setChannels(rs.getString(5));
        d.setDescription(rs.getString(6));
        d.setAdministrators(rs.getString(7));
        return d;
    }

    @Override
    public void removeUserDashboards(String userID) throws ThingsDataException {
        String query = "delete from dashboards where userid=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Dashboard> getUserDashboards(String userID, boolean withShared) throws ThingsDataException {
        String query;
        if (withShared) {
            query = "select id,name,userid,title,team,widgets,token,shared,administrators from dashboards where userid=? or team like ? or administrators like ?";
        } else {
            query = "select id,name,userid,title,team,widgets,token,shared,administrators from dashboards where userid=?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            if (withShared) {
                pstmt.setString(2, "%," + userID + ",%");
                pstmt.setString(3, "%," + userID + ",%");
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
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            pstmt.setString(2, dashboardID);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dashboard getDashboard(String userID, String dashboardID) throws ThingsDataException {
        boolean publicUser = "public".equalsIgnoreCase(userID);
        String query = "select id,name,userid,title,team,widgets,token,shared,administrators from dashboards where id=? and (userid=? or team like ? or administrators like ? ";
        if (publicUser) {
            query = query.concat("or shared=true)");
        } else {
            query = query.concat(")");
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboardID);
            pstmt.setString(2, userID);
            pstmt.setString(3, "%," + userID + ",%");
            pstmt.setString(4, "%," + userID + ",%");
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
    public Dashboard getDashboardByName(String userID, String dashboardName) throws ThingsDataException {
        boolean publicUser = "public".equalsIgnoreCase(userID);
        String query = "select id,name,userid,title,team,widgets,token,shared,administrators from dashboards where name=? and (userid=? or team like ? or administrators like ? ";
        if (publicUser) {
            query = query.concat("or shared=true)");
        } else {
            query = query.concat(")");
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboardName);
            pstmt.setString(2, userID);
            pstmt.setString(3, "%," + userID + ",%");
            pstmt.setString(4, "%," + userID + ",%");
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
        String query = "update dashboards set name=?,userid=?,title=?,team=?,widgets=?,token=?,shared=?,administrators=? where id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboard.getName());
            pstmt.setString(2, dashboard.getUserID());
            pstmt.setString(3, dashboard.getTitle());
            pstmt.setString(4, dashboard.getTeam());
            pstmt.setString(5, dashboard.getWidgetsAsJson());
            pstmt.setString(6, dashboard.getSharedToken());
            pstmt.setBoolean(7, dashboard.isShared());
            pstmt.setString(8, dashboard.getAdministrators());
            pstmt.setString(9, dashboard.getId());
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException {
        String query = "select id from dashboards where id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
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
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
    }

    @Override
    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException {
        String query = "select eui,appid,appeui,type,channels,code,decoder,description,tinterval,pattern,commandscript,producer from devicetemplates where upper(eui) = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, templateEUI);
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
        String query = "select eui,appid,appeui,type,channels,code,decoder,description,tinterval,pattern,commandscript,producer from devicetemplates";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
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
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
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
        String query = "SELECT eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,groups,alert,devid,active,project,latitude,longitude,altitude,state,retention,administrators,framecheck from devices "
                + "where tinterval > 0 and alert < 2 and (datediff(S,dateadd(S, lastseen/1000, DATE '1970-01-01'),now())-"
                + timeOffset + ") > tinterval/1000;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
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
    public DeviceGroup getGroup(String userID, String groupEUI) throws ThingsDataException {
        String query;
        query = "select eui,name,userid,team,channels,description,administrators from groups where eui=? and (userid = ? or team like ? or administrators like ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI);
            pstmt.setString(2, userID);
            pstmt.setString(3, "%," + userID + ",%");
            pstmt.setString(4, "%," + userID + ",%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DeviceGroup group = buildGroup(rs);
                return group;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException {
        String query;
        query = "select eui,name,userid,team,channels,description,administrators from groups where eui=?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DeviceGroup group = buildGroup(rs);
                return group;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<DeviceGroup> getUserGroups(String userID) throws ThingsDataException {
        String query;
        query = "select eui,name,userid,team,channels,description,administrators from groups where userid = ? or team like ? or administrators like ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, userID);
            pstmt.setString(2, "%," + userID + ",%");
            pstmt.setString(3, "%," + userID + ",%");
            ResultSet rs = pstmt.executeQuery();
            ArrayList<DeviceGroup> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildGroup(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putGroup(DeviceGroup group) throws ThingsDataException {
        if (getGroup(group.getUserID(), group.getEUI()) != null) {
            throw new ThingsDataException(ThingsDataException.CONFLICT,
                    "group " + group.getEUI() + " is already defined");
        }
        String query = "insert into groups (eui,name,userid,team,channels,description,administrators) values(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, group.getEUI());
            pstmt.setString(2, group.getName());
            pstmt.setString(3, group.getUserID());
            pstmt.setString(4, group.getTeam());
            pstmt.setString(5, group.getChannelsAsString());
            pstmt.setString(6, group.getDescription());
            pstmt.setString(7, group.getAdministrators());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding group " + group.getEUI());
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGroup(DeviceGroup group) throws ThingsDataException {
        String query = "update groups set name=?,userid=?,team=?,channels=?,description=?,administrators=? where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getUserID());
            pstmt.setString(3, group.getTeam());
            pstmt.setString(4, group.getChannelsAsString());
            pstmt.setString(5, group.getDescription());
            pstmt.setString(6, group.getAdministrators());
            pstmt.setString(7, group.getEUI());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error updating group " + group.getEUI());
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeGroup(String groupEUI) throws ThingsDataException {
        String query = "delete from groups where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI);
            int updated = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getGroupChannels(String groupEUI) throws ThingsDataException {
        List<String> channels;
        // return ((Service) Kernel.getInstance()).getDataStorageAdapter().
        String query = "select channels from groups where eui=?";
        channels = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, groupEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString(1).toLowerCase().split(",");
                channels = Arrays.asList(s);
            }
            return channels;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Get last registered measuresfrom groupDevices dedicated to the specified
     * group
     *
     * @param userID
     * @param groupEUI
     * @param channelNames
     * @return
     * @throws ThingsDataException
     */
    @Override
    public List<List> getValuesOfGroup(String userID, String groupEUI, String[] channelNames, long interval)
            throws ThingsDataException {
        return getGroupLastValues(userID, groupEUI, channelNames, interval);
    }

    @Override
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException {
        List<String> channels;
        String query = "select channels from devicechannels where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString(1).toLowerCase().split(",");
                channels = Arrays.asList(s);
                String channelStr = "";
                for (int i = 0; i < channels.size(); i++) {
                    channelStr = channelStr + channels.get(i) + ",";
                }
                Kernel.getInstance()
                        .dispatchEvent(Event.logInfo(this, "CHANNELS READ: " + deviceEUI + " " + channelStr));
                return channels;
            } else {
                return new ArrayList<>();
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException {
        String query = "merge into devicechannels (eui,channels) key (eui) values (?,?)";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.setString(2, channelNames.toLowerCase());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void putDeviceChannels(String deviceEUI, List<String> channelNames) throws ThingsDataException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < channelNames.size(); i++) {
            sb.append(channelNames.get(i));
            if (i < channelNames.size() - 1) {
                sb.append(",");
            }
        }
        putDeviceChannels(deviceEUI, sb.toString());
    }

    @Override
    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException {
        String query = "delete from devicedata where eui=? and tstamp<?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    private void clearAllChannels(String deviceEUI) throws ThingsDataException {
        String query = "delete from devicedata where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public int updateDeviceChannels(Device device, Device oldDevice) throws ThingsDataException {
        int result = 0;
        ArrayList<String> newChannels = new ArrayList<>();
        device.getChannels().keySet().forEach(key -> {
            newChannels.add((String) key);
        });
        if (oldDevice != null && !device.getChannelsAsString().equals(oldDevice.getChannelsAsString())) {
            // ATTENTION! All actualValue data will be lost!
            // TODO: Send notification to the user?
            removeAllChannels(device.getEUI());
            result = 1;
        }
        putDeviceChannels(device.getEUI(), newChannels);
        return result;
    }

    @Override
    public void removeAllChannels(String deviceEUI) throws ThingsDataException {
        clearAllChannels(deviceEUI);
        String query = "delete from devicechannels where eui=?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values)
            throws ThingsDataException {
        System.out.println("saving data");
        if (values == null || values.isEmpty()) {
            System.out.println("no values");
            return;
        }
        int limit = 24;
        List channelNames = getDeviceChannels(deviceEUI);
        String query = "insert into devicedata (eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long timestamp = values.get(0).getTimestamp();
        java.sql.Date date = new java.sql.Date(timestamp);
        java.sql.Time time = new java.sql.Time(timestamp);
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.setString(2, userID);
            pst.setDate(3, date);
            pst.setTime(4, time);
            pst.setTimestamp(5, new java.sql.Timestamp(timestamp));
            for (int i = 1; i <= limit; i++) {
                pst.setNull(i + 5, java.sql.Types.DOUBLE);
            }
            int index = -1;
            // if (values.size() <= limit) {
            // limit = values.size();
            // }
            if (values.size() > limit) {
                // TODO: send notification to the user?
            }
            for (int i = 1; i <= limit; i++) {
                if (i <= values.size()) {
                    index = channelNames.indexOf(values.get(i - 1).getName());
                    if (index >= 0 && index < limit) { // TODO: there must be control of mthe number of measures while
                        // defining device, not here
                        try {
                            pst.setDouble(6 + index, values.get(i - 1).getValue());
                        } catch (NullPointerException e) {
                            pst.setNull(6 + index, Types.DOUBLE);
                        }
                    }
                }
            }
            pst.setString(30, project);
            pst.setDouble(31, deviceState);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        HashMap<String, DeviceDataDto> dataMap = new HashMap<>();
        HashMap<String, DeviceChannelDto> channelMap = new HashMap<>();
        String query;
        String dataQuery = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,state from devicedata";
        String channelQuery = "select eui,channels from devicechannels";
        if ("devicechannels".equalsIgnoreCase(tableName)) {
            query = channelQuery;
            DeviceChannelDto dto;
            try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    dto = new DeviceChannelDto();
                    dto.eui = rs.getString(1);
                    dto.channels = rs.getString(2);
                    channelMap.put(dto.eui, dto);
                }
                return channelMap;
            } catch (SQLException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            }
        } else if ("devicedata".equalsIgnoreCase(tableName)) {
            query = dataQuery;
            DeviceDataDto dto;
            try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    dto = new DeviceDataDto();
                    dto.eui = rs.getString(1);
                    dto.userId = rs.getString(2);
                    dto.day = rs.getDate(3);
                    dto.dtime = rs.getTime(4);
                    dto.timestamp = rs.getTimestamp(5);
                    dto.d1 = getDouble(rs, 6);
                    dto.d2 = getDouble(rs, 7);
                    dto.d3 = getDouble(rs, 8);
                    dto.d4 = getDouble(rs, 9);
                    dto.d5 = getDouble(rs, 10);
                    dto.d6 = getDouble(rs, 11);
                    dto.d7 = getDouble(rs, 12);
                    dto.d8 = getDouble(rs, 13);
                    dto.d9 = getDouble(rs, 14);
                    dto.d10 = getDouble(rs, 15);
                    dto.d11 = getDouble(rs, 16);
                    dto.d12 = getDouble(rs, 17);
                    dto.d13 = getDouble(rs, 18);
                    dto.d14 = getDouble(rs, 19);
                    dto.d15 = getDouble(rs, 20);
                    dto.d16 = getDouble(rs, 21);
                    dto.d17 = getDouble(rs, 22);
                    dto.d18 = getDouble(rs, 23);
                    dto.d19 = getDouble(rs, 24);
                    dto.d20 = getDouble(rs, 25);
                    dto.d21 = getDouble(rs, 26);
                    dto.d22 = getDouble(rs, 27);
                    dto.d23 = getDouble(rs, 28);
                    dto.d24 = getDouble(rs, 29);
                    dto.project = rs.getString(30);
                    dto.status = rs.getDouble(31);
                    dataMap.put(dto.eui, dto);
                }
                return channelMap;
            } catch (SQLException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            }
        } else {
            return null;
        }

    }

    private Double getDouble(ResultSet rs, int index) throws SQLException {
        double d = rs.getDouble(index);
        if (!rs.wasNull()) {
            return d;
        } else {
            return null;
        }
    }

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        int channelIndex = getChannelIndex(deviceEUI, channel);
        if (channelIndex < 0) {
            return null;
        }
        String columnName = "d" + (channelIndex);
        String query = "select eui,userid,day,dtime,tstamp," + columnName
                + " from devicedata where eui=? order by tstamp desc limit 1";
        ChannelData result = null;
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            Double d;
            if (rs.next()) {
                d = rs.getDouble(6);
                if (!rs.wasNull()) {
                    result = new ChannelData(deviceEUI, channel, d, rs.getTimestamp(5).getTime());
                }
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? order by tstamp desc limit 1";
        List<String> channels = getDeviceChannels(deviceEUI);
        ArrayList<ChannelData> row = new ArrayList<>();
        ArrayList<List> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            double d;
            if (rs.next()) {
                for (int i = 0; i < channels.size(); i++) {
                    d = rs.getDouble(6 + i);
                    if (!rs.wasNull()) {
                        row.add(new ChannelData(deviceEUI, channels.get(i), d,
                                rs.getTimestamp(5).getTime()));
                    }
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }


    public List<List> getValues(String userID, String deviceEUI, int limit, DataQuery dataQuery)
            throws ThingsDataException {
                String query=buildSqlQuery(-1, dataQuery);
        List<String> channels = getDeviceChannels(deviceEUI);
        List<List> result = new ArrayList<>();
        ArrayList<ChannelData> row;
        ArrayList row2;
        System.out.println("SQL QUERY: "+query);
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            int paramIdx = 2;
            if (null != dataQuery.getProject()) {
                pst.setString(paramIdx, dataQuery.getProject());
                paramIdx++;
                if (null != dataQuery.getState()) {
                    pst.setDouble(paramIdx, dataQuery.getState());
                    paramIdx++;
                }
            } else {
                if (null != dataQuery.getState()) {
                    pst.setDouble(paramIdx, dataQuery.getState());
                    paramIdx++;
                }
            }
            if (null != dataQuery.getFromTs() && null != dataQuery.getToTs()) {
                System.out.println("fromTS: "+dataQuery.getFromTs().getTime());
                pst.setTimestamp(paramIdx, dataQuery.getFromTs());
                paramIdx++;
                System.out.println("toTS: "+dataQuery.getToTs().getTime());
                pst.setTimestamp(paramIdx, dataQuery.getToTs());
                paramIdx++;
            }
            pst.setInt(paramIdx, limit);

            ResultSet rs = pst.executeQuery();
            if (dataQuery.isTimeseries()) {
                row2 = new ArrayList();
                row2.add("timestamp");
                for (int i = 0; i < channels.size(); i++) {
                    row2.add(channels.get(i));
                }
                result.add(row2);
            }
            double d;
            while (rs.next()) {
                if (dataQuery.isTimeseries()) {
                    row2 = new ArrayList();
                    row2.add(rs.getTimestamp(5).getTime());
                    for (int i = 0; i < channels.size(); i++) {
                        d = rs.getDouble(6 + i);
                        if (!rs.wasNull()) {
                            row2.add(d);
                        } else {
                            row2.add(null);
                        }
                    }
                    result.add(row2);
                } else {
                    row = new ArrayList<>();
                    for (int i = 0; i < channels.size(); i++) {
                        d = rs.getDouble(6 + i);
                        if (!rs.wasNull()) {
                            row.add(new ChannelData(deviceEUI, channels.get(i), d,
                                    rs.getTimestamp(5).getTime()));
                        }
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<List> getDeviceMeasures(String userID, String deviceEUI, String dataQuery) throws ThingsDataException {
        DataQuery dq;
        try {
            dq = DataQuery.parse(dataQuery);
        } catch (DataQueryException ex) {
            throw new ThingsDataException(ex.getCode(), "DataQuery " + ex.getMessage());
        }
        if (null != dq.getGroup()) {
            return getValuesOfGroup(userID, dq.getGroup(), dq.getChannelName().split(","), defaultGroupInterval);
        }
        
        int limit = dq.getLimit();
        if (dq.average > 0) {
            limit = dq.average;
        }
        if (dq.minimum > 0) {
            limit = dq.minimum;
        }
        if (dq.maximum > 0) {
            limit = dq.maximum;
        }
        if (dq.summary > 0) {
            limit = dq.summary;
        }
        List<List> result = new ArrayList<>();
        if (dq.getNewValue() != null) {
            limit = limit - 1;
        }
        
        if (null == dq.getChannelName() || "*".equals(dq.getChannelName())) {
            // TODO
            result.add(getValues(userID, deviceEUI, limit, dq));
            return result;
        }
        boolean singleChannel = !dq.getChannelName().contains(",");
        if (singleChannel) {
            result.add(getChannelValues(userID, deviceEUI, dq.getChannelName(), limit, dq)); // project
        } else {
            String[] channels = dq.getChannelName().split(",");
            List<ChannelData>[] temp = new ArrayList[channels.length];
            for (int i = 0; i < channels.length; i++) {
                temp[i] = getChannelValues(userID, deviceEUI, channels[i], limit, dq); // project
            }
            List<ChannelData> values;
            for (int i = 0; i < limit; i++) {
                values = new ArrayList<>();
                for (int j = 0; j < channels.length; j++) {
                    if (temp[j].size() > i) {
                        values.add(temp[j].get(i));
                    }
                }
                if (values.size() > 0) {
                    result.add(values);
                }
            }
        }
        if (!singleChannel) {
            return result;
        }

        ChannelData data = new ChannelData(dq.getChannelName(), 0.0, System.currentTimeMillis());
        data.setNullValue();
        List<ChannelData> subResult = new ArrayList<>();
        Double actualValue = null;
        Double tmpValue;
        int size = 0;
        if (dq.average > 0) {
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    if (i == 0) {
                        actualValue = ((ChannelData) result.get(0).get(i)).getValue();
                    } else {
                        actualValue = actualValue + ((ChannelData) result.get(0).get(i)).getValue();
                    }
                }
            }
            if (dq.getNewValue() != null) {
                if (null != actualValue) {
                    actualValue = actualValue + dq.getNewValue();
                } else {
                    actualValue = dq.getNewValue();
                }
                data.setValue(actualValue / (size + 1));
            } else {
                if (size > 0) {
                    data.setValue(actualValue / size);
                }
            }
            subResult.add(data);
            result.clear();
            result.add(subResult);
        } else if (dq.maximum > 0) {
            actualValue = Double.MIN_VALUE;
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    tmpValue = ((ChannelData) result.get(0).get(i)).getValue();
                    if (tmpValue.compareTo(actualValue) > 0) {
                        actualValue = tmpValue;
                    }
                }
            }
            if (dq.getNewValue() != null && dq.getNewValue() > actualValue) {
                actualValue = dq.getNewValue();
            }
            if (actualValue.compareTo(Double.MIN_VALUE) > 0) {
                data.setValue(actualValue);
            }
            subResult.add(data);
            result.clear();
            result.add(subResult);
        } else if (dq.minimum > 0) {
            actualValue = Double.MAX_VALUE;
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    tmpValue = ((ChannelData) result.get(0).get(i)).getValue();
                    if (tmpValue.compareTo(actualValue) < 0) {
                        actualValue = tmpValue;
                    }
                }
            }
            if (dq.getNewValue() != null && dq.getNewValue() < actualValue) {
                actualValue = dq.getNewValue();
            }
            if (actualValue.compareTo(Double.MAX_VALUE) < 0) {
                data.setValue(actualValue);
            }
            subResult.add(data);
            result.clear();
            result.add(subResult);
        } else if (dq.summary > 0) {
            actualValue = null;
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    if (i == 0) {
                        actualValue = ((ChannelData) result.get(0).get(i)).getValue();
                    } else {
                        actualValue = actualValue + ((ChannelData) result.get(0).get(i)).getValue();
                    }
                }
            }
            if (dq.getNewValue() != null) {
                if (null == actualValue) {
                    actualValue = actualValue + dq.getNewValue();
                } else {
                    actualValue = dq.getNewValue();
                }
            }
            if (null != actualValue) {
                data.setValue(actualValue);
            }
            subResult.add(data);
            result.clear();
            result.add(subResult);
        }

        return result;
    }

    private String buildSqlQuery(int channelIndex, DataQuery dq) {
        String query;
        String defaultQuery;
        if (channelIndex >= 0) {
            String columnName = "d" + (channelIndex);
            defaultQuery = "select eui,userid,day,dtime,tstamp," + columnName + ","
                    + "project,state from devicedata where eui=?";
        } else {
            defaultQuery = "select eui,userid,day,dtime,tstamp,"
            +"d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24"
            +" from devicedata where eui=?";
        }
        String projectQuery = " and project=?";
        String stateQuery = " and state=?";
        String wherePart = " and tstamp>=? and tstamp<=?";
        String orderPart = " order by tstamp desc limit ?";
        query = defaultQuery;
        if (null != dq.getProject()) {
            query = query.concat(projectQuery);
        }
        if (null != dq.getState()) {
            query = query.concat(stateQuery);
        }
        if (null != dq.getFromTs() && null != dq.getToTs()) {
            query = query.concat(wherePart);
        }
        query = query.concat(orderPart);
        return query;
    }

    private List<ChannelData> getChannelValues(String userID, String deviceEUI, String channel, int resultsLimit,
            DataQuery dataQuery) throws ThingsDataException {
        ArrayList<ChannelData> result = new ArrayList<>();
        int channelIndex = getChannelIndex(deviceEUI, channel);
        if (channelIndex < 1) {
            return result;
        }
        String query = buildSqlQuery(channelIndex, dataQuery);
        int limit = resultsLimit;
        if (requestLimit > 0 && requestLimit < limit) {
            limit = requestLimit;
        }
        System.out.println("QUERY: " + query);
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "getChannelValues QUERY: " + query));
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);

            int paramIdx = 2;
            if (null != dataQuery.getProject()) {
                pst.setString(paramIdx, dataQuery.getProject());
                paramIdx++;
                if (null != dataQuery.getState()) {
                    pst.setDouble(paramIdx, dataQuery.getState());
                    paramIdx++;
                }
            } else {
                if (null != dataQuery.getState()) {
                    pst.setDouble(paramIdx, dataQuery.getState());
                    paramIdx++;
                }
            }
            if (null != dataQuery.getFromTs() && null != dataQuery.getToTs()) {
                pst.setTimestamp(paramIdx, dataQuery.getFromTs());
                paramIdx++;
                pst.setTimestamp(paramIdx, dataQuery.getToTs());
                paramIdx++;
            }
            pst.setInt(paramIdx, limit);

            ResultSet rs = pst.executeQuery();
            Double d;
            while (rs.next()) {
                d = rs.getDouble(6);
                if (!rs.wasNull()) {
                    result.add(0, new ChannelData(deviceEUI, channel, d, rs.getTimestamp(5).getTime()));
                }
            }
            return result;
        } catch (SQLException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "problematic query = " + query));
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException {
        return getDeviceMeasures(userID, deviceEUI, query);
    }

    @Override
    public int getChannelIndex(String deviceEUI, String channel) throws ThingsDataException {
        return getDeviceChannels(deviceEUI).indexOf(channel) + 1;
    }

    @Override
    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        String query = "insert into commands (id,category,type,origin,payload,createdat) values (?,?,?,?,?,?);";
        // String query2 = "update commands set id=?,payload=?,createdat=? where
        // category=? and type=? and origin=?";
        String query2 = "merge into commands key (category,type,origin) values (?,?,?,?,?,?)";
        String command = (String) commandEvent.getPayload();
        boolean overwriteMode = false;
        if (command.startsWith("&")) {
            overwriteMode = false;
        } else if (command.startsWith("#")) {
            query = query2;
            overwriteMode = true;
        }
        command = command.substring(1);
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, commandEvent.getId());
            pst.setString(2, commandEvent.getCategory());
            pst.setString(3, commandEvent.getType());
            pst.setString(4, deviceEUI);
            pst.setString(5, command);
            pst.setLong(6, commandEvent.getCreatedAt());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(e.getErrorCode(), e.getMessage());
        }
        /*
         * if (!overwriteMode) {
         * try (Connection conn = getConnection()) {
         * PreparedStatement pst;
         * pst = conn.prepareStatement(query);
         * pst.setLong(1, commandEvent.getId());
         * pst.setString(2, commandEvent.getCategory());
         * pst.setString(3, commandEvent.getType());
         * pst.setString(4, deviceEUI);
         * pst.setString(5, (String) commandEvent.getPayload());
         * pst.setLong(6, commandEvent.getCreatedAt());
         * pst.executeUpdate();
         * pst.close();
         * conn.close();
         * } catch (SQLException e) {
         * throw new ThingsDataException(e.getErrorCode(), e.getMessage());
         * }
         * } else {
         * try (Connection conn = getConnection()) {
         * PreparedStatement pst;
         * pst = conn.prepareStatement(query2);
         * pst.setLong(1, commandEvent.getId());
         * pst.setString(2, (String) commandEvent.getPayload());
         * pst.setLong(3, commandEvent.getCreatedAt());
         * pst.setString(4, commandEvent.getCategory());
         * pst.setString(5, commandEvent.getType());
         * pst.setString(6, commandEvent.getOrigin());
         * pst.executeUpdate();
         * pst.close();
         * conn.close();
         * } catch (SQLException e) {
         * throw new ThingsDataException(e.getErrorCode(), e.getMessage());
         * }
         * }
         */
    }

    @Override
    public Event getFirstCommand(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commands where origin like ? order by createdat limit 1";
        Event result = null;
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                result = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                result.setId(rs.getLong(1));
                result.setCreatedAt(rs.getLong(5));
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException {
        String query = "delete from commands where origin like ? and createdat<?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.setLong(2, checkPoint);
            // pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeAllCommands(String deviceEUI) throws ThingsDataException {
        String query = "delete from commands where origin like ?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commands where origin like ? order by createdat";
        ArrayList<Event> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            Event ev;
            while (rs.next()) {
                ev = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                ev.setId(rs.getLong(1));
                ev.setCreatedAt(rs.getLong(5));
                result.add(ev.clone());
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException {
        String query = "insert into commandslog (id,category,type,origin,payload,createdat) values (?,?,?,?,?,?);";
        String command = (String) commandEvent.getPayload();
        if (command.startsWith("#") || command.startsWith("&")) {
            command = command.substring(1);
        }
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, commandEvent.getId());
            pst.setString(2, commandEvent.getCategory());
            pst.setString(3, commandEvent.getType());
            pst.setString(4, deviceEUI);
            pst.setString(5, command);
            pst.setLong(6, commandEvent.getCreatedAt());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void clearAllLogs(String deviceEUI, long checkPoint) throws ThingsDataException {
        String query = "delete from commandslog where origin like ? and createdat<?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.setLong(2, checkPoint);
            // pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeAllLogs(String deviceEUI) throws ThingsDataException {
        String query = "delete from commandslog where origin like ?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<Event> getAllLogs(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commandslog where origin like ? order by createdat";
        ArrayList<Event> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            Event ev;
            while (rs.next()) {
                ev = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                ev.setId(rs.getLong(1));
                ev.setCreatedAt(rs.getLong(5));
                result.add(ev.clone());
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeCommand(long id) throws ThingsDataException {
        String query = "delete from commands where id=?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<List> getGroupLastValues(String userID, String groupEUI, String[] channelNames, long secondsBack)
            throws ThingsDataException {
        List<String> requestChannels = Arrays.asList(channelNames);
        try {
            String group = "%," + groupEUI + ",%";
            long timestamp = System.currentTimeMillis() - secondsBack;
            String deviceQuery = "SELECT eui,channels FROM devices WHERE groups like ?;";
            HashMap<String, List> devices = new HashMap<>();
            String query;
            query = "SELECT "
                    + "eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 "
                    + "FROM devicedata "
                    + "WHERE eui IN "
                    + "(SELECT eui FROM devices WHERE groups like ?) "
                    + "and (tstamp>?) "
                    + "order by eui,tstamp desc;";
            List<String> groupChannels = getGroupChannels(groupEUI);
            if (requestChannels.size() == 0) {
                logger.error("empty channelNames");
                requestChannels = groupChannels;
            }
            List<List> result = new ArrayList<>();
            List<ChannelData> row = new ArrayList<>();
            List<ChannelData> tmpResult = new ArrayList<>();
            ChannelData cd;
            logger.debug("{} {} {} {} {}", groupEUI, group, groupChannels.size(), requestChannels.size(), query);
            try (Connection conn = getConnection();
                    PreparedStatement pstd = conn.prepareStatement(deviceQuery);
                    PreparedStatement pst = conn.prepareStatement(query);) {
                pstd.setString(1, group);
                ResultSet rs = pstd.executeQuery();
                while (rs.next()) {
                    devices.put(rs.getString(1), Arrays.asList(rs.getString(2).split(",")));
                }
                pst.setString(1, group);
                pst.setTimestamp(2, new Timestamp(timestamp));
                rs = pst.executeQuery();
                int channelIndex;
                String channelName;
                String devEui;
                double d;
                while (rs.next()) {
                    for (int i = 0; i < groupChannels.size(); i++) {
                        devEui = rs.getString(1);
                        channelName = groupChannels.get(i);
                        channelIndex = devices.get(devEui).indexOf(channelName);
                        d = rs.getDouble(6 + channelIndex);
                        if (!rs.wasNull()) {
                            tmpResult.add(new ChannelData(devEui, channelName, d,
                                    rs.getTimestamp(5).getTime()));
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
            }
            if (tmpResult.isEmpty()) {
                return result;
            }
            long processedTimestamp = 0;
            String prevEUI = "";
            long prevTimestamp = 0;
            int idx;
            for (int i = 0; i < tmpResult.size(); i++) {
                cd = tmpResult.get(i);
                logger.info("ChannelData: {} {} {}", cd.getDeviceEUI(), cd.getName(), cd.getTimestamp());
                if (!cd.getDeviceEUI().equalsIgnoreCase(prevEUI)) {
                    if (!row.isEmpty()) {
                        result.add(row);
                    }
                    row = new ArrayList();
                    for (int j = 0; j < channelNames.length; j++) {
                        row.add(null);
                    }
                    idx = requestChannels.indexOf(cd.getName());
                    if (idx > -1) {
                        row.set(idx, cd);
                    }
                    prevEUI = cd.getDeviceEUI();
                    prevTimestamp = cd.getTimestamp();
                } else {
                    if (prevTimestamp == cd.getTimestamp()) {
                        idx = requestChannels.indexOf(cd.getName());
                        if (idx > -1) {
                            row.set(idx, cd);
                        }
                    } else {
                        // skip prevous measures
                    }
                }
            }
            if (!row.isEmpty()) {
                result.add(row);
            }
            return result;
        } catch (Exception e) {
            StackTraceElement[] ste = e.getStackTrace();
            logger.error("requestChannels[{}]", requestChannels.size());
            logger.error("channelNames[{}]", channelNames.length);
            logger.error(e.getMessage());
            for (int i = 0; i < ste.length; i++) {
                logger.error("{}.{}:{}", e.getStackTrace()[i].getClassName(), e.getStackTrace()[i].getMethodName(),
                        e.getStackTrace()[i].getLineNumber());
            }
            return null;
        }
    }

    @Override
    public void setDeviceStatus(String eui, Double state) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDeviceAlertStatus(String eui, int status) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDeviceStatus(String eui, long lastSeen, long frameCounter, String downlink, int alertStatus,
            String deviceID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
