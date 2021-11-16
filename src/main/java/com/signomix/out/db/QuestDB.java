/**
 * Copyright (C) Grzegorz Skorupa 2021.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.cedarsoftware.util.io.JsonWriter;
import com.signomix.Service;
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.archiver.ZipArchiver;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.PostgreSqlDB;
import org.cricketmsf.out.db.SqlDBIface;

public class QuestDB extends PostgreSqlDB implements SqlDBIface, IotDbDataIface, Adapter {

    private int requestLimit = 0; //no limit

    @Override
    public void createDatabase(Connection conn, String version) {
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void createStructure() {
        try {
            Kernel.handle(Event.logInfo(this.getClass().getSimpleName(), "createStructure()"));
            addTable("devicechannels", 1, true);
            addTable("devicedata", 1, true);
            addTable("devicetemplates", 1, true);
            addTable("devices", 1, true);
            addTable("dashboards", 1, true);
            addTable("alerts", 1, true);
            addTable("groups", 1, true);
            addTable("commands", 1, true);
            addTable("commandslog", 1, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        try {
            requestLimit = Integer.parseInt(properties.getOrDefault("requestLimit", "500"));
            Kernel.getInstance().getLogger().print("\trequestLimit: " + requestLimit);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {

        String query;
        StringBuilder sb = new StringBuilder();
        switch (tableName) {
            case "devicechannels":
                sb.append("CREATE TABLE IF NOT EXISTS devicechannels (")
                        .append("eui STRING,")
                        .append("channels STRING, ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "devicedata":
                sb.append("CREATE TABLE IF NOT EXISTS  devicedata (")
                        .append("eui STRING,")
                        .append("userid STRING,")
                        .append("ts TIMESTAMP,")
                        .append("d1 DOUBLE,")
                        .append("d2 DOUBLE,")
                        .append("d3 DOUBLE,")
                        .append("d4 DOUBLE,")
                        .append("d5 DOUBLE,")
                        .append("d6 DOUBLE,")
                        .append("d7 DOUBLE,")
                        .append("d8 DOUBLE,")
                        .append("d9 DOUBLE,")
                        .append("d10 DOUBLE,")
                        .append("d11 DOUBLE,")
                        .append("d12 DOUBLE,")
                        .append("d13 DOUBLE,")
                        .append("d14 DOUBLE,")
                        .append("d15 DOUBLE,")
                        .append("d16 DOUBLE,")
                        .append("d17 DOUBLE,")
                        .append("d18 DOUBLE,")
                        .append("d19 DOUBLE,")
                        .append("d20 DOUBLE,")
                        .append("d21 DOUBLE,")
                        .append("d22 DOUBLE,")
                        .append("d23 DOUBLE,")
                        .append("d24 DOUBLE,")
                        .append("project STRING,")
                        .append("status DOUBLE) timestamp(ts) PARTITION BY DAY");
                break;
            case "devicetemplates":
                sb.append("CREATE TABLE IF NOT EXISTS  devicetemplates (")
                        .append("eui STRING,")
                        .append("appid STRING,")
                        .append("appeui STRING,")
                        .append("type STRING,")
                        .append("channels STRING,")
                        .append("code STRING,")
                        .append("decoder STRING,")
                        .append("description STRING,")
                        .append("tinterval LONG,")
                        .append("pattern STRING,")
                        .append("commandscript STRING,")
                        .append("producer STRING,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "devices":
                sb.append("CREATE TABLE IF NOT EXISTS devices (")
                        .append("eui STRING,")
                        .append("name STRING,")
                        .append("userid STRING,")
                        .append("type STRING,")
                        .append("channels STRING,")
                        .append("code STRING,")
                        .append("decoder STRING,")
                        .append("key STRING,")
                        .append("description STRING,")
                        .append("lastseen LONG,")
                        .append("tinterval LONG,")
                        .append("lastframe LONG,")
                        .append("template STRING,")
                        .append("pattern STRING,")
                        .append("downlink STRING,")
                        .append("commandscript STRING,")
                        .append("appid STRING,")
                        .append("alert INT,")
                        .append("appeui STRING,")
                        .append("devid STRING,")
                        .append("active BOOLEAN,")
                        .append("project STRING,")
                        .append("latitude DOUBLE,")
                        .append("longitude DOUBLE,")
                        .append("altitude DOUBLE,")
                        .append("status DOUBLE,")
                        .append("retention LONG,")
                        .append("groups STRING,")
                        .append("team STRING,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "dashboards":
                sb.append("CREATE TABLE IF NOT EXISTS  dashboards (")
                        .append("id STRING,")
                        .append("name STRING,")
                        .append("userid STRING,")
                        .append("title STRING,")
                        .append("widgets STRING,")
                        .append("token STRING,")
                        .append("shared BOOLEAN,")
                        .append("team STRING,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                // TODO: widgets
                break;
            case "alerts":
                // TODO: jak jest zapisany userID?
                sb.append("CREATE TABLE IF NOT EXISTS  alerts (")
                        .append("id LONG,")
                        .append("name STRING,")
                        .append("category STRING,")
                        .append("type STRING,")
                        .append("deviceeui STRING,")
                        .append("userid STRING,")
                        .append("payload STRING,")
                        .append("timepoint STRING,")
                        .append("serviceid STRING,")
                        .append("uuid STRING,")
                        .append("calculatedtimepoint LONG,")
                        .append("createdat LONG,")
                        .append("rooteventid LONG,")
                        .append("cyclic BOOLEAN,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "groups":
                sb.append("CREATE TABLE IF NOT EXISTS  groups (")
                        .append("eui STRING,")
                        .append("name STRING,")
                        .append("userid STRING,")
                        .append("channels STRING,")
                        .append("description STRING,")
                        .append("team STRING,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "commands":
                sb.append("CREATE TABLE IF NOT EXISTS  commands (")
                        .append("id LONG,")
                        .append("category STRING,")
                        .append("type STRING,")
                        .append("origin STRING,")
                        .append("payload STRING,")
                        .append("createdat LONG,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            case "commandslog":
                sb.append("CREATE TABLE IF NOT EXISTS  commandslog (")
                        .append("id LONG,")
                        .append("category STRING,")
                        .append("type STRING,")
                        .append("origin STRING,")
                        .append("payload STRING,")
                        .append("createdat LONG,")
                        .append("ts TIMESTAMP) timestamp(ts) PARTITION BY DAY");
                break;
            default:
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
        }
        query = sb.toString();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.executeUpdate();
        } catch (SQLException e) {
            String message=e.getMessage().toLowerCase();
            if (message.indexOf("table already exists")<0) {
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, message);
            }
        }
    }

    @Override
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException {
        String query = "SELECT channels FROM devicechannels LATEST BY eui,ts WHERE eui=?";
        ArrayList<String> result = new ArrayList<>();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString(1).split(",");
                for (int i = 0; i < s.length; i++) {
                    result.add(s[i].toLowerCase());
                }
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException {
        String query = "INSERT INTO devicechannels (eui,channels,ts) values (?,?,systimestamp())";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());
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
        String query = "ALTER TABLE devicedata DROP PARTITION WHERE eui=? AND timestamp < dateadd('d', "+(-1*checkPoint)+", now())";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    private void clearAllChannels(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet.");
        /*
        String query = "delete from devicedata where eui=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
        */
    }

    @Override
    public int updateDeviceChannels(Device device, Device oldDevice) throws ThingsDataException {
        int result = 0;
        ArrayList<String> newChannels = new ArrayList<>();
        device.getChannels().keySet().forEach(key -> {
            newChannels.add((String) key);
        });
        putDeviceChannels(device.getEUI(), newChannels);
        return result;
    }

    @Override
    public void removeAllChannels(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet.");
        /*
        clearAllChannels(deviceEUI);
        String query = "delete from devicechannels where eui=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
        */
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values) throws ThingsDataException {
        if (values == null || values.isEmpty()) {
            return;
        }
        int limit = 24;
        List channelNames = getDeviceChannels(deviceEUI);
        String query = "insert into devicedata (eui,userid,ts,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long timestamp = values.get(0).getTimestamp();
        java.sql.Date date = new java.sql.Date(timestamp);
        java.sql.Time time = new java.sql.Time(timestamp);
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());
            pst.setString(2, userID);
            pst.setTimestamp(3, new java.sql.Timestamp(timestamp));
            for (int i = 1; i <= limit; i++) {
                pst.setNull(i + 3, java.sql.Types.DOUBLE);
            }
            int index = -1;
            if (values.size() > limit) {
                //TODO: send notification to the user?
            }
            for (int i = 1; i <= limit; i++) {
                if (i <= values.size()) {
                    index = channelNames.indexOf(values.get(i - 1).getName());
                    if (index >= 0 && index < limit) { // TODO: there must be control of mthe number of measures while defining device, not here
                        try {
                            pst.setDouble(4 + index, values.get(i - 1).getValue());
                        } catch (NullPointerException e) {
                            pst.setNull(4 + index, Types.DOUBLE);
                        }
                    }
                }
            }
            pst.setString(28, project);
            pst.setDouble(29, deviceState);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        HashMap<String, DeviceDataDto> dataMap = new HashMap<>();
        HashMap<String, DeviceChannelDto> channelMap = new HashMap<>();
        String query;
        String dataQuery = "select eui,userid,ts,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,status from devicedata";
        String channelQuery = "select eui,channels from devicechannels";
        if ("devicechannels".equalsIgnoreCase(tableName)) {
            query = channelQuery;
            DeviceChannelDto dto;
            try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
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
            try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    dto = new DeviceDataDto();
                    dto.eui = rs.getString(1);
                    dto.userId = rs.getString(2);
                    dto.timestamp = rs.getTimestamp(3);
                    dto.d1 = rs.getDouble(4);
                    dto.d2 = rs.getDouble(5);
                    dto.d3 = rs.getDouble(6);
                    dto.d4 = rs.getDouble(7);
                    dto.d5 = rs.getDouble(8);
                    dto.d6 = rs.getDouble(9);
                    dto.d7 = rs.getDouble(10);
                    dto.d8 = rs.getDouble(11);
                    dto.d9 = rs.getDouble(12);
                    dto.d10 = rs.getDouble(13);
                    dto.d11 = rs.getDouble(14);
                    dto.d12 = rs.getDouble(15);
                    dto.d13 = rs.getDouble(16);
                    dto.d14 = rs.getDouble(17);
                    dto.d15 = rs.getDouble(18);
                    dto.d16 = rs.getDouble(19);
                    dto.d17 = rs.getDouble(20);
                    dto.d18 = rs.getDouble(21);
                    dto.d19 = rs.getDouble(22);
                    dto.d20 = rs.getDouble(23);
                    dto.d21 = rs.getDouble(24);
                    dto.d22 = rs.getDouble(25);
                    dto.d23 = rs.getDouble(26);
                    dto.d24 = rs.getDouble(27);
                    dto.project = rs.getString(28);
                    dto.status = rs.getDouble(29);
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

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getBackupFile() {
        try {
            ZipArchiver archiver = new ZipArchiver("data-", ".zip");
            Map args = new HashMap();
            args.put(JsonWriter.TYPE, true);
            args.put(JsonWriter.PRETTY_PRINT, true);
            Map map;
            map = getAll("devicechannels");
            String json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("channels.json", json);
            map = getAll("devicedata");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("data.json", json);
            return archiver.getFile();
        } catch (KeyValueDBException | IOException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return null;
        }
    }

    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        int channelIndex = getChannelIndex(deviceEUI, channel);
        if (channelIndex < 0) {
            return null;
        }
        String columnName = "d" + (channelIndex);
        String query = "select eui,userid,ts,"
                + columnName
                + " from devicedata latest by eui,ts where eui=?";
        ChannelData result = null;
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                result = new ChannelData(deviceEUI, channel, rs.getDouble(4), rs.getTimestamp(3).getTime());
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        String query = "select eui,userid,ts,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata latest by eui,ts where eui=?";
        List<String> channels = getDeviceChannels(deviceEUI);
        ArrayList<ChannelData> row = new ArrayList<>();
        ArrayList<List> result = new ArrayList<>();
        try ( Connection conn = getConnection();PreparedStatement pst= conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                for (int i = 0; i < channels.size(); i++) {
                    row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(4 + i), rs.getTimestamp(3).getTime()));
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    public List<List> getValues(String userID, String deviceEUI, int limit, boolean timeseriesMode) throws ThingsDataException {
        String query = "select eui,userid,ts,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? order by ts desc limit ?";
        List<String> channels = getDeviceChannels(deviceEUI);
        List<List> result = new ArrayList<>();
        ArrayList<ChannelData> row;
        ArrayList row2;
        try ( Connection conn = getConnection();PreparedStatement pst= conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());
            pst.setInt(2, limit);
            ResultSet rs = pst.executeQuery();
            if (timeseriesMode) {
                row2 = new ArrayList();
                row2.add("timestamp");
                for (int i = 0; i < channels.size(); i++) {
                    row2.add(channels.get(i));
                }
                result.add(row2);
            }
            while (rs.next()) {
                if (timeseriesMode) {
                    row2 = new ArrayList();
                    row2.add(rs.getTimestamp(3).getTime());
                    for (int i = 0; i < channels.size(); i++) {
                        row2.add(rs.getDouble(4 + i));
                    }
                    result.add(row2);
                } else {
                    row = new ArrayList<>();
                    for (int i = 0; i < channels.size(); i++) {
                        row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(4 + i), rs.getTimestamp(3).getTime()));
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
            return getValuesOfGroup(userID, dq.getGroup(), dq.getChannelName().split(","));
        }
        int limit = dq.getLimit();
        Double newValue = dq.getNewValue();
        Double deviceState = dq.getState();
        String project = dq.getProject();
        String channel = dq.getChannelName();
        boolean timeSeries = dq.isTimeseries();

        List<List> result = new ArrayList<>();
        if (newValue != null) {
            limit = limit - 1;
        }
        if (null == channel || "*".equals(channel)) {
            //TODO
            result.add(getValues(userID, deviceEUI, limit, timeSeries));
            return result;
        }
        boolean singleChannel = !channel.contains(",");
        if (singleChannel) {
            result.add(getChannelValues(userID, deviceEUI, channel, limit, project, deviceState)); //project
        } else {
            String[] channels = channel.split(",");
            List<ChannelData>[] temp = new ArrayList[channels.length];
            for (int i = 0; i < channels.length; i++) {
                temp[i] = getChannelValues(userID, deviceEUI, channels[i], limit, project, deviceState); //project
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

        ChannelData data = new ChannelData(channel, 0.0, System.currentTimeMillis());
        List<ChannelData> subResult = new ArrayList<>();
        Double actualValue = 0.0;
        Double tmpValue;
        int size = 0;
        if (dq.average > 0) {
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    actualValue = actualValue + ((ChannelData) result.get(0).get(i)).getValue();
                }
            }
            if (newValue != null) {
                actualValue = actualValue + newValue;
                data.setValue(actualValue / (size + 1));
            } else {
                data.setValue(actualValue / size);
            }
            subResult.add(data);
        } else if (dq.maximum > 0) {
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    tmpValue = ((ChannelData) result.get(0).get(i)).getValue();
                    if (tmpValue > actualValue) {
                        actualValue = tmpValue;
                    }
                }
            }
            if (newValue != null && newValue > actualValue) {
                actualValue = newValue;
            }
            data.setValue(actualValue);
            subResult.add(data);
        } else if (dq.minimum > 0) {
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    tmpValue = ((ChannelData) result.get(0).get(i)).getValue();
                    if (tmpValue < actualValue) {
                        actualValue = tmpValue;
                    }
                }
            }
            if (newValue != null && newValue < actualValue) {
                actualValue = newValue;
            }
            data.setValue(actualValue);
            subResult.add(data);
        } else if (dq.summary > 0) {
            if (result.size() > 0) {
                size = result.get(0).size();
                for (int i = 0; i < size; i++) {
                    actualValue = actualValue + ((ChannelData) result.get(0).get(i)).getValue();
                }
            }
            if (newValue != null) {
                actualValue = actualValue + newValue;
            }
            data.setValue(actualValue);
            subResult.add(data);
        }
        result.clear();
        result.add(subResult);
        return result;
    }

    private List<ChannelData> getChannelValues(String userID, String deviceEUI, String channel, int resultsLimit, String project, Double state) throws ThingsDataException {
        ArrayList<ChannelData> result = new ArrayList<>();
        int channelIndex = getChannelIndex(deviceEUI, channel);
        if (channelIndex < 1) {
            return result;
        }
        String columnName = "d" + (channelIndex + 1);

        String query;
        String defaultQuery = "select eui,userid,ts,"
                + columnName + ","
                + "project,status from devicedata where eui=? and "
                + columnName
                + " is not null";
        String projectQuery = " and project=?";
        String stateQuery = " and status=?";
        String orderPart = " order by ts desc limit ?";
        query = defaultQuery;
        if (null != project) {
            query = query.concat(projectQuery);
        }
        if (null != state) {
            query = query.concat(stateQuery);
        }
        query = query.concat(orderPart);
        int limit = resultsLimit;
        if (requestLimit > 0 && requestLimit < limit) {
            limit = requestLimit;
        }

        try ( Connection conn = getConnection();PreparedStatement pst= conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI.toUpperCase());

            int paramIdx = 2;
            if (null != project) {
                pst.setString(paramIdx, project);
                paramIdx++;
                if (null != state) {
                    pst.setDouble(paramIdx, state);
                    paramIdx++;
                }
            } else {
                if (null != state) {
                    pst.setDouble(paramIdx, state);
                    paramIdx++;
                }
            }
            pst.setInt(paramIdx, limit);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                result.add(0, new ChannelData(deviceEUI, channel, rs.getDouble(4), rs.getTimestamp(3).getTime()));
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
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
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
    public List<List> getValuesOfGroup(String userID, String groupEUI, String[] channelNames) throws ThingsDataException {
        List<Device> groupDevices = ((Service) Kernel.getInstance()).getThingsAdapter().getGroupDevices(userID, groupEUI);
        List<String> groupChannels = ((Service) Kernel.getInstance()).getThingsAdapter().getGroupChannels(groupEUI);
        List<List> tmp, tmpValues;
        List<List> result = new ArrayList();
        List<ChannelData> row;
        ChannelData cd;
        ArrayList<String> requestChannels = new ArrayList<>();
        for (int n = 0; n < channelNames.length; n++) {
            requestChannels.add(channelNames[n]);
        }
        int idx;
        for (int i = 0; i < groupDevices.size(); i++) {
            tmpValues = getLastValues(userID, groupDevices.get(i).getEUI());
            if (tmpValues.isEmpty()) {
                continue;
            }
            row = new ArrayList(requestChannels.size());
            for (int n = 0; n < requestChannels.size(); n++) {
                row.add(null);
            }
            tmp = tmpValues.get(0);
            for (int j = 0; j < tmp.size(); j++) {
                cd = (ChannelData) tmp.get(j);
                if (groupChannels.indexOf(cd.getName()) > -1) {
                    idx = requestChannels.indexOf(cd.getName());
                    if (idx > -1) {
                        row.set(idx, cd);
                    }
                }
            }
            result.add(row);
        }
        return result;
    }

    @Override
    public int getChannelIndex(String deviceEUI, String channel) throws ThingsDataException {
        return getDeviceChannels(deviceEUI).indexOf(channel) + 1;
    }

    @Override
    public List<Device> getUserDevices(String userID, boolean withShared) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getUserDevicesCount(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Device getDevice(String userID, String deviceEUI, boolean withShared) throws ThingsDataException {
        String query;
        if (withShared) {
            query = "select eui,name,userid,type,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,alert,devid,active,project,latitude,longitude,altitude,status,retention,groups,team from devices latest by eui,userid,ts "
                    + "where eui=? and (userid = ? or strpos(team,?)>0)";
        } else {
            query = "select eui,name,userid,type,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,alert,devid,active,project,latitude,longitude,altitude,status,retention,groups,team from devices latest by eui,userid,ts "
                    + "where eui=? and userid = ?";
        }
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, deviceEUI.toUpperCase());
            pstmt.setString(2, userID);
            if (withShared) {
                pstmt.setString(3, ","+userID+",");
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
        String query = "select eui,name,userid,type,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,alert,devid,active,project,latitude,longitude,altitude,status,retention,groups,team from devices latest by eui,ts where eui = ?";
        if (deviceEUI == null || deviceEUI.isEmpty()) {
            return null;
        }
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
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
    public DeviceTemplate getDeviceTemplte(String templateEUI) throws ThingsDataException {
        String query = "select eui,appid,appeui,type,channels,code,decoder,description,tinterval,pattern,commandscript,producer from devicetemplates latest by eui,ts where eui = ?";
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putDevice(Device device) throws ThingsDataException {
        if (getDevice(device.getUserID(), device.getEUI(), false) != null) {
            throw new ThingsDataException(ThingsDataException.CONFLICT,
                    "device " + device.getEUI() + " is already defined");
        }
        String query = "insert into devices (eui,name,userid,type,channels,code,decoder,key,description,lastseen,tinterval,lastframe,template,pattern,downlink,commandscript,appid,appeui,alert,devid,active,project,latitude,longitude,altitude,status,retention,groups,team,ts) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,systimestamp())";
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, device.getEUI());
            pstmt.setString(2, device.getName());
            pstmt.setString(3, device.getUserID());
            pstmt.setString(4, device.getType());
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
            pstmt.setInt(19, device.getAlertStatus());
            pstmt.setString(20, device.getDeviceID());
            pstmt.setBoolean(21, device.isActive());
            pstmt.setString(22, device.getProject());
            pstmt.setDouble(23, device.getLatitude());
            pstmt.setDouble(24, device.getLongitude());
            pstmt.setDouble(25, device.getAltitude());
            pstmt.setDouble(26, device.getState());
            pstmt.setLong(27, device.getRetentionTime());
            pstmt.setString(28, device.getGroups());
            pstmt.setString(29, device.getTeam());
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
        //removeFromCache(device.getGroups());
    }

    @Override
    public void updateDevice(Device device) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAuthorized(String userID, String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isGroupAuthorized(String userID, String groupEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAlert(Event alert) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List getAlerts(String userID, boolean descending) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAlert(long alertID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAlerts(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAlerts(String userID, long checkpoint) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeOutdatedAlerts(long checkpoint) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeDevice(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAllDevices(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addDashboard(Dashboard dashboard) throws ThingsDataException {
        String query = "insert into dashboards (id,name,userid,title,team,widgets,token,shared,ts) values (?,?,?,?,?,?,?,?,systimestamp())";
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboard.getId());
            pstmt.setString(2, dashboard.getName());
            pstmt.setString(3, dashboard.getUserID());
            pstmt.setString(4, dashboard.getTitle());
            pstmt.setString(5, dashboard.getTeam());
            pstmt.setString(6, dashboard.getWidgetsAsJson());
            pstmt.setString(7, dashboard.getSharedToken());
            pstmt.setBoolean(8, dashboard.isShared());
            int updated = pstmt.executeUpdate();
            if (updated < 1) {
                throw new ThingsDataException(ThingsDataException.BAD_REQUEST,
                        "DB error adding dashboard " + dashboard.getId());
            }
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDeviceTemplate(DeviceTemplate device) throws ThingsDataException {
        String query = "insert into devicetemplates (eui,appid,appeui,type,channels,code,decoder,description,tinterval,pattern,commandscript,producer,ts) values(?,?,?,?,?,?,?,?,?,?,?,?,systimestamp())";
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
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
            pstmt.execute();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUserDashboards(String userID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Dashboard> getUserDashboards(String userID, boolean withShared) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeDashboard(String userID, String dashboardID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Dashboard getDashboard(String userID, String dashboardID) throws ThingsDataException {
        boolean publicUser = "public".equalsIgnoreCase(userID);
        String query = "select id,name,userid,title,team,widgets,token,shared from dashboards latest by id,ts where id=? and (userid=? or strpos(team,?)>0";
        if (publicUser) {
            query = query.concat("or shared=true)");
        } else {
            query = query.concat(")");
        }
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, dashboardID);
            pstmt.setString(2, userID);
            pstmt.setString(3, "," + userID + ",");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateDashboard(Dashboard dashboard) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDashboardRegistered(String dashboardID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Device> getInactiveDevices() throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Device> getGroupDevices(String userID, String groupID) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException {
        String query;
        query = "select eui,name,userid,team,channels,description from groups latest by eui,ts where eui=?";

        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI.toUpperCase());
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
    public DeviceGroup getGroup(String userID, String groupEUI) throws ThingsDataException {
        String query;
        query = "select eui,name,userid,team,channels,description from groups latest by eui,userid,ts where eui=? and (userid = ? or strpos(team,?)>0)";

        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, groupEUI);
            pstmt.setString(2, userID);
            pstmt.setString(3, "," + userID + ",");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putGroup(DeviceGroup group) throws ThingsDataException {
        if (getGroup(group.getUserID(), group.getEUI()) != null) {
            throw new ThingsDataException(ThingsDataException.CONFLICT,
                    "group " + group.getEUI() + " is already defined");
        }
        String query = "insert into groups (eui,name,userid,team,channels,description,ts) values(?,?,?,?,?,?,systimestamp())";
        try ( Connection conn = getConnection();  PreparedStatement pstmt = conn.prepareStatement(query);) {
            pstmt.setString(1, group.getEUI());
            pstmt.setString(2, group.getName());
            pstmt.setString(3, group.getUserID());
            pstmt.setString(4, group.getTeam());
            pstmt.setString(5, group.getChannelsAsString());
            pstmt.setString(6, group.getDescription());
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
    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Event getFirstCommand(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAllCommands(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeCommand(long id) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearAllLogs(String deviceEUI, long checkPoint) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAllLogs(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Event> getAllLogs(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
        Device buildDevice(ResultSet rs) throws SQLException {
        // eui,name,userid,type,team,channels,code,decoder,key,description,lastseen,tinterval
        Device d = new Device();
        d.setEUI(rs.getString(1));
        d.setName(rs.getString(2));
        d.setUserID(rs.getString(3));
        d.setType(rs.getString(4));
        d.setChannels(rs.getString(5));
        d.setCode(rs.getString(6));
        d.setEncoder(rs.getString(7));
        d.setKey(rs.getString(8));
        d.setDescription(rs.getString(9));
        d.setLastSeen(rs.getLong(10));
        d.setTransmissionInterval(rs.getLong(11));
        d.setLastFrame(rs.getLong(12));
        d.setTemplate(rs.getString(13));
        d.setPattern(rs.getString(14));
        d.setDownlink(rs.getString(15));
        d.setCommandScript(rs.getString(16));
        d.setApplicationID(rs.getString(17));
        d.setApplicationEUI(rs.getString(18));
        d.setAlertStatus(rs.getInt(19));
        d.setDeviceID(rs.getString(20));
        d.setActive(rs.getBoolean(21));
        d.setProject(rs.getString(22));
        d.setLatitude(rs.getDouble(23));
        d.setLongitude(rs.getDouble(24));
        d.setAltitude(rs.getDouble(25));
        d.setState(rs.getDouble(26));
        d.setRetentionTime(rs.getLong(27));        
        d.setGroups(rs.getString(28));
        d.setTeam(rs.getString(29));
        
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
        return d;
    }

}
