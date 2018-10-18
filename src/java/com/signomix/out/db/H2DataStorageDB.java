/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.db.H2EmbededDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class H2DataStorageDB extends H2EmbededDB implements SqlDBIface, IotDataStorageIface, Adapter {

    private int requestLimit = 0; //no limit

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
        String indexQuery = null;
        StringBuilder sb = new StringBuilder();
        switch (tableName) {
            case "devicechannels":
                sb.append("create table devicechannels (")
                        .append("eui varchar primary key,")
                        .append("channels varchar)");
                break;
            case "devicedata":
                sb.append("create table devicedata (")
                        .append("eui varchar not null,")
                        .append("userid varchar,")
                        .append("day date,")
                        .append("dtime time,")
                        .append("tstamp timestamp not null,")
                        .append("d1 double,")
                        .append("d2 double,")
                        .append("d3 double,")
                        .append("d4 double,")
                        .append("d5 double,")
                        .append("d6 double,")
                        .append("d7 double,")
                        .append("d8 double,")
                        .append("d9 double,")
                        .append("d10 double,")
                        .append("d11 double,")
                        .append("d12 double,")
                        .append("d13 double,")
                        .append("d14 double,")
                        .append("d15 double,")
                        .append("d16 double)");
                indexQuery = "create primary key on devicedata (eui,tstamp)";
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
            if (indexQuery != null) {
                PreparedStatement pst2 = conn.prepareStatement(indexQuery);
                pst2.executeUpdate();
                pst2.close();
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException {
        String query = "select channels from devicechannels where eui=?";
        ArrayList<String> result = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String[] s = rs.getString(1).split(",");
                for (int i = 0; i < s.length; i++) {
                    result.add(s[i].toLowerCase());
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
    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException {
        String query = "merge into devicechannels (eui,channels) key (eui) values (?,?)";
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.setString(2, channelNames.toLowerCase());
            pst.executeUpdate();
            pst.close();
            conn.close();
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
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    private void clearAllChannels(String deviceEUI) throws ThingsDataException {
        String query = "delete from devicedata where eui=?";
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
            pst.close();
            conn.close();
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
            // UWAGA! Wszystkie dotychczasowe dane zostaną utracone!
            //TODO: wysłać notyfikację do usera?
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
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putData(String userID, String deviceEUI, List<ChannelData> values) throws ThingsDataException {
        if (values==null || values.isEmpty()) {
            return;
        }
        List channelNames = getDeviceChannels(deviceEUI);
        String query = "insert into devicedata (eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long timestamp = values.get(0).getTimestamp();
        java.sql.Date date = new java.sql.Date(timestamp);
        java.sql.Time time = new java.sql.Time(timestamp);
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.setString(2, userID);
            pst.setDate(3, date);
            pst.setTime(4, time);
            pst.setTimestamp(5, new java.sql.Timestamp(timestamp));
            for (int i = 1; i < 17; i++) {
                pst.setNull(i + 5, java.sql.Types.DOUBLE);
            }
            int index = -1;
            for (int i = 0; i < values.size(); i++) {
                index = channelNames.indexOf(values.get(i).getName());
                if (index >= 0) {
                    pst.setDouble(6 + index, values.get(i).getValue());
                }
            }
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16 from devicedata where eui=? and ?? is not null order by tstamp desc limit 1";
        List<String> channels = getDeviceChannels(deviceEUI);
        int channelIndex = channels.indexOf(channel);
        if (channelIndex < 0) {
            return null;
        }
        String columnName = "d" + (channelIndex + 1);
        query = query.replaceFirst("\\?\\?", columnName);
        ChannelData result = null;
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                result = new ChannelData(deviceEUI, channel, rs.getDouble(6 + channelIndex), rs.getTimestamp(5).getTime());
            }
            pst.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16 from devicedata where eui=? order by tstamp desc limit 1";
        List<String> channels = getDeviceChannels(deviceEUI);
        ArrayList<ChannelData> row = new ArrayList<>();
        ArrayList<List> result = new ArrayList<>();
        String columnName;
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                for (int i = 0; i < channels.size(); i++) {
                    columnName = "d" + (i + 1);
                    row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(6 + i), rs.getTimestamp(5).getTime()));
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

    @Override
    public List<List> getValues(String userID, String deviceEUI, int limit) throws ThingsDataException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16 from devicedata where eui=? order by tstamp desc limit ?";
        List<String> channels = getDeviceChannels(deviceEUI);
        List<List> result = new ArrayList<>();
        ArrayList<ChannelData> row;
        String columnName;
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.setInt(2, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                row = new ArrayList<>();
                for (int i = 0; i < channels.size(); i++) {
                    columnName = "d" + (i + 1);
                    row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(6 + i), rs.getTimestamp(5).getTime()));
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

    @Override
    public List<ChannelData> getValues(String userID, String deviceEUI, String channel, String resultsLimit) throws ThingsDataException {
        int limit = getLimit(resultsLimit);
        if (channel.indexOf(",") < 0 || limit != 1) {
            return getChannelValues(userID, deviceEUI, channel, limit);
        } else {
            List<ChannelData> list = new ArrayList<>();
            String[] channels = channel.split(",");
            for (String channel1 : channels) {
                try {
                    list.add(getChannelValues(userID, deviceEUI, channel1, 1).get(0));
                } catch (IndexOutOfBoundsException e) {
                }
            }
            return list;
        }
    }

    private List<ChannelData> getChannelValues(String userID, String deviceEUI, String channel, int resultsLimit) throws ThingsDataException {
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16 from devicedata where eui=? and ?? is not null order by tstamp desc limit ?";
        int limit = resultsLimit;
        if (requestLimit > 0 && requestLimit < limit) {
            limit = requestLimit;
        }
        List<String> channels = getDeviceChannels(deviceEUI);
        ArrayList<ChannelData> result = new ArrayList<>();
        int channelIndex = channels.indexOf(channel);
        if (channelIndex < 0) {
            return result;
        }
        String columnName = "d" + (channelIndex + 1);
        query = query.replaceFirst("\\?\\?", columnName);

        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            pst.setInt(2, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                result.add(0, new ChannelData(deviceEUI, channel, rs.getDouble(6 + channelIndex), rs.getTimestamp(5).getTime()));
            }
            pst.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }

    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException {
        List<List> result = new ArrayList<>();
        List<ChannelData> row = new ArrayList<>();
        String channelName = "";
        int indexOfChannel = query.indexOf("channel ");
        int limit = 1;
        if (indexOfChannel >= 0) {
            channelName = query.substring(indexOfChannel + 8, query.indexOf(" ", indexOfChannel + 8));
        }
        if (query.contains("last")) {
            limit = getLimit(query.substring(query.indexOf("last")));
        }
        result = new ArrayList();
        if (channelName.isEmpty()) {
            return getValues(userID, deviceEUI, limit);
        } else {
            result.add(getValues(userID, deviceEUI, channelName, "last " + limit));
        }
        return result;
    }

    private int getLimit(String query) {
        int result = 1;
        String[] params = query.trim().split(" ");
        if (params[0].equalsIgnoreCase("last") && params.length == 2) {
            try {
                result = Integer.parseInt(params[1]);
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }
}
