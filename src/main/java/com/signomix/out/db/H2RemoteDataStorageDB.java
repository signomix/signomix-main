/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.cedarsoftware.util.io.JsonWriter;
import com.signomix.Service;
import com.signomix.out.db.dto.DeviceChannelDto;
import com.signomix.out.db.dto.DeviceDataDto;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DataQueryException;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.archiver.ZipArchiver;
import org.cricketmsf.out.db.H2RemoteDB;
import org.cricketmsf.out.db.KeyValueDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class H2RemoteDataStorageDB extends H2RemoteDB implements SqlDBIface, IotDataStorageIface, Adapter {

    private static final String DEVICE_CHANNEL_CACHE = "device_channel_cache";
    private int requestLimit = 0; // no limit
    private KeyValueDBIface cache = null;
    @Deprecated private boolean useCache = false;
    private static int MAX_CONNECTIONS = 100;

    private KeyValueDBIface getCache() {
        if (useCache && null == cache) {
            cache = ((Service) Service.getInstance()).database;
        }
        return cache;
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
        useCache = Boolean.parseBoolean(properties.getOrDefault("use-cache", "false"));
        Kernel.getInstance().getLogger().print("\tuse-cache: " + useCache);

    }

    @Override
    public void start() throws KeyValueDBException {
        super.start();
        cp.setMaxConnections(MAX_CONNECTIONS);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {
        String query;
        String indexQuery = null;
        StringBuilder sb = new StringBuilder();
        switch (tableName) {
            case "devicechannels":
                sb.append("create table devicechannels (").append("eui varchar primary key,").append("channels varchar)");
                break;
            case "devicedata":
                sb.append("create table devicedata (").append("eui varchar not null,").append("userid varchar,")
                        .append("day date,").append("dtime time,").append("tstamp timestamp not null,").append("d1 double,")
                        .append("d2 double,").append("d3 double,").append("d4 double,").append("d5 double,")
                        .append("d6 double,").append("d7 double,").append("d8 double,").append("d9 double,")
                        .append("d10 double,").append("d11 double,").append("d12 double,").append("d13 double,")
                        .append("d14 double,").append("d15 double,").append("d16 double,").append("d17 double,")
                        .append("d18 double,").append("d19 double,").append("d20 double,").append("d21 double,")
                        .append("d22 double,").append("d23 double,").append("d24 double,").append("project varchar,")
                        .append("state double)");
                indexQuery = "create primary key on devicedata (eui,tstamp)";
                break;
            default:
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
        }
        query = sb.toString();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.executeUpdate();
            pst.close();
            if (indexQuery != null) {
                PreparedStatement pst2 = conn.prepareStatement(indexQuery);
                pst2.executeUpdate();
                pst2.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException {
        List<String> channels;
        if (useCache) {
            try {
                channels = (List) getCache().get(DEVICE_CHANNEL_CACHE, deviceEUI);
                if (null != channels) {
                    String channelStr = "";
                    for (int i = 0; i < channels.size(); i++) {
                        channelStr = channelStr + channels.get(i) + ",";
                    }
                    return channels;
                } else {

                }
            } catch (KeyValueDBException ex) {
                // TODO: logger
                ex.printStackTrace();
            }
        }
        String query = "select channels from devicechannels where eui=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString(1).toLowerCase().split(",");
                channels = Arrays.asList(s);
                if (useCache) {
                    try {
                        getCache().put(DEVICE_CHANNEL_CACHE, deviceEUI, channels);
                    } catch (KeyValueDBException ex) {
                        ex.printStackTrace();
                    }
                }
                String channelStr = "";
                for (int i = 0; i < channels.size(); i++) {
                    channelStr = channelStr + channels.get(i) + ",";
                }
                Kernel.getInstance().dispatchEvent(Event.logInfo(this, "CHANNELS READ: " + deviceEUI + " " + channelStr));
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
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
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
        removeChannelsFromCache(deviceEUI);
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
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    private void clearAllChannels(String deviceEUI) throws ThingsDataException {
        String query = "delete from devicedata where eui=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
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
        removeChannelsFromCache(device.getEUI());
        return result;
    }

    @Override
    public void removeAllChannels(String deviceEUI) throws ThingsDataException {
        clearAllChannels(deviceEUI);
        String query = "delete from devicechannels where eui=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
        removeChannelsFromCache(deviceEUI);
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    private void removeChannelsFromCache(String deviceEUI) {
        if(!useCache){
            return;
        }
        try {
            getCache().remove(DEVICE_CHANNEL_CACHE, deviceEUI);
        } catch (KeyValueDBException ex) {
            // TODO:logger
        }
    }

    @Override
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values)
            throws ThingsDataException {
        if (values == null || values.isEmpty()) {
            return;
        }
        int limit = 24;
        List channelNames = getDeviceChannels(deviceEUI);
        String query = "insert into devicedata (eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long timestamp = values.get(0).getTimestamp();
        java.sql.Date date = new java.sql.Date(timestamp);
        java.sql.Time time = new java.sql.Time(timestamp);
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
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
                    dto.day = rs.getDate(3);
                    dto.dtime = rs.getTime(4);
                    dto.timestamp = rs.getTimestamp(5);
                    dto.d1 = rs.getDouble(6);
                    dto.d2 = rs.getDouble(7);
                    dto.d3 = rs.getDouble(8);
                    dto.d4 = rs.getDouble(9);
                    dto.d5 = rs.getDouble(10);
                    dto.d6 = rs.getDouble(11);
                    dto.d7 = rs.getDouble(12);
                    dto.d8 = rs.getDouble(13);
                    dto.d9 = rs.getDouble(14);
                    dto.d10 = rs.getDouble(15);
                    dto.d11 = rs.getDouble(16);
                    dto.d12 = rs.getDouble(17);
                    dto.d13 = rs.getDouble(18);
                    dto.d14 = rs.getDouble(19);
                    dto.d15 = rs.getDouble(20);
                    dto.d16 = rs.getDouble(21);
                    dto.d17 = rs.getDouble(22);
                    dto.d18 = rs.getDouble(23);
                    dto.d19 = rs.getDouble(24);
                    dto.d20 = rs.getDouble(25);
                    dto.d21 = rs.getDouble(26);
                    dto.d22 = rs.getDouble(27);
                    dto.d23 = rs.getDouble(28);
                    dto.d24 = rs.getDouble(29);
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

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
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
        //String query = "select eui,userid,day,dtime,tstamp," + columnName + " from devicedata where eui=? and "
        //        + columnName + " is not null order by tstamp desc limit 1";
        String query = "select eui,userid,day,dtime,tstamp," + columnName + " from devicedata where eui=? order by tstamp desc limit 1";
        ChannelData result = null;
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
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
        try ( Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                for (int i = 0; i < channels.size(); i++) {
                    row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(6 + i),
                            rs.getTimestamp(5).getTime()));
                }
                result.add(row);
            }
            pst.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    public List<List> getValues(String userID, String deviceEUI, int limit, boolean timeseriesMode)
            throws ThingsDataException {
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? order by tstamp desc limit ?";
        List<String> channels = getDeviceChannels(deviceEUI);
        List<List> result = new ArrayList<>();
        ArrayList<ChannelData> row;
        ArrayList row2;
        try ( Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
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
                    row2.add(rs.getTimestamp(5).getTime());
                    for (int i = 0; i < channels.size(); i++) {
                        row2.add(rs.getDouble(6 + i));
                    }
                    result.add(row2);
                } else {
                    row = new ArrayList<>();
                    for (int i = 0; i < channels.size(); i++) {
                        row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(6 + i),
                                rs.getTimestamp(5).getTime()));
                    }
                    result.add(row);
                }
            }
            pst.close();
            conn.close();
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
            return getValuesOfGroup(userID, dq.getGroup(), dq.getChannelName().split(","),0);
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
            // TODO
            result.add(getValues(userID, deviceEUI, limit, timeSeries));
            return result;
        }
        boolean singleChannel = !channel.contains(",");
        if (singleChannel) {
            result.add(getChannelValues(userID, deviceEUI, channel, limit, project, deviceState)); // project
        } else {
            String[] channels = channel.split(",");
            List<ChannelData>[] temp = new ArrayList[channels.length];
            for (int i = 0; i < channels.length; i++) {
                temp[i] = getChannelValues(userID, deviceEUI, channels[i], limit, project, deviceState); // project
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
            if (newValue != null) {
                if (null != actualValue) {
                    actualValue = actualValue + newValue;
                } else {
                    actualValue = newValue;
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
            if (newValue != null && newValue > actualValue) {
                actualValue = newValue;
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
            if (newValue != null && newValue < actualValue) {
                actualValue = newValue;
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
            if (newValue != null) {
                if (null == actualValue) {
                    actualValue = actualValue + newValue;
                } else {
                    actualValue = newValue;
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

    private List<ChannelData> getChannelValues(String userID, String deviceEUI, String channel, int resultsLimit,
            String project, Double state) throws ThingsDataException {
        ArrayList<ChannelData> result = new ArrayList<>();
        int channelIndex = getChannelIndex(deviceEUI, channel);
        if (channelIndex < 1) {
            return result;
        }
        String columnName = "d" + (channelIndex);

        String query;
        //String defaultQuery = "select eui,userid,day,dtime,tstamp," + columnName + ","
        //        + "project,state from devicedata where eui=? and " + columnName + " is not null";
        String defaultQuery = "select eui,userid,day,dtime,tstamp," + columnName + ","
                + "project,state from devicedata where eui=?";
        String projectQuery = " and project=?";
        String stateQuery = " and state=?";
        String orderPart = " order by tstamp desc limit ?";
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

        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "getChannelValues QUERY: " + query));
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, deviceEUI);

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
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
        String query = "";
        switch (versionNumber) {
            case 2:
                query = "alter table devicedata add (d17 double, d18 double, d19 double, d20 double, d21 double, d22 double, d23 double, d24 double);";
                break;
            case 3:
                query = "alter table devicedata add project varchar;";
                break;
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
        List<Device> groupDevices = ((Service) Kernel.getInstance()).getThingsAdapter().getGroupDevices(userID,
                groupEUI);
        List<String> groupChannels = ((Service) Kernel.getInstance()).getThingsAdapter().getGroupChannels(groupEUI);
        List<List> tmp, tmpValues;
        List<List> result = new ArrayList();
        List<ChannelData> row;
        ChannelData cd;
        List<String> requestChannels = Arrays.asList(channelNames);
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
}
