/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.Service;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.DataQuery;
import com.signomix.out.iot.DataQueryException;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.db.H2RemoteDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class H2RemoteDataStorageDB extends H2RemoteDB implements SqlDBIface, IotDataStorageIface, Adapter {

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
                        .append("d16 double,")
                        .append("d17 double,")
                        .append("d18 double,")
                        .append("d19 double,")
                        .append("d20 double,")
                        .append("d21 double,")
                        .append("d22 double,")
                        .append("d23 double,")
                        .append("d24 double,")
                        .append("project varchar,")
                        .append("state double)");
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
            // ATTENTION! All actual data will be lost!
            //TODO: Send notification to the user?
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
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values) throws ThingsDataException {
        if (values == null || values.isEmpty()) {
            return;
        }
        int limit = 24;
        List channelNames = getDeviceChannels(deviceEUI);
        String query = "insert into devicedata (eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
            for (int i = 1; i <= limit; i++) {
                pst.setNull(i + 5, java.sql.Types.DOUBLE);
            }
            int index = -1;
            //if (values.size() <= limit) {
            //    limit = values.size();
            //}
            if (values.size() > limit) {
                //TODO: send notification to the user?
            }
            for (int i = 1; i <= limit; i++) {
                if (i <= values.size()) {
                    index = channelNames.indexOf(values.get(i - 1).getName());
                    if (index >= 0 && index < limit) { // TODO: there must be control of mthe number of measures while defining device, not here
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
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? and ?? is not null order by tstamp desc limit 1";
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
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? order by tstamp desc limit 1";
        List<String> channels = getDeviceChannels(deviceEUI);
        ArrayList<ChannelData> row = new ArrayList<>();
        ArrayList<List> result = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                for (int i = 0; i < channels.size(); i++) {
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

    public List<List> getValues(String userID, String deviceEUI, int limit, boolean timeseriesMode) throws ThingsDataException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String query = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24 from devicedata where eui=? order by tstamp desc limit ?";
        List<String> channels = getDeviceChannels(deviceEUI);
        List<List> result = new ArrayList<>();
        ArrayList<ChannelData> row;
        ArrayList row2;
        try (Connection conn = getConnection()) {
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
                        row.add(new ChannelData(deviceEUI, channels.get(i), rs.getDouble(6 + i), rs.getTimestamp(5).getTime()));
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
            throw new ThingsDataException(requestLimit, "DataQuery " + ex.getMessage());
        }
        if (null != dq.getGroup()) {
            return getValuesOfGroup(userID, dq.getGroup(), dq.getChannelName().split(","));
        }
        int limit = dq.getLimit();
        int averageLimit = dq.getAverage();
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
        if (!channel.contains(",")) {
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
        if ((!channel.contains(",")) && averageLimit > 0) {
            ChannelData data = new ChannelData("", 0.0, System.currentTimeMillis());
            Double sum = 0.0;
            int size = 0;
            if (result.size() > 0) {
                size = result.get(0).size();
                if (size > 0) {
                    data = (ChannelData) result.get(0).get(0);
                }
                for (int i = 0; i < size; i++) {
                    sum = sum + ((ChannelData) result.get(0).get(i)).getValue();
                }
            }
            if (newValue != null) {
                sum = sum + newValue;
                data.setValue(sum / (size + 1));
            } else {
                data.setValue(sum / size);
            }
            List<ChannelData> resultAvg = new ArrayList<>();
            resultAvg.add(data);
            result.clear();
            result.add(resultAvg);
        }
        return result;
    }

    private List<ChannelData> getChannelValues(String userID, String deviceEUI, String channel, int resultsLimit, String project, Double state) throws ThingsDataException {
        String query;
        String defaultQuery = "select eui,userid,day,dtime,tstamp,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15,d16,d17,d18,d19,d20,d21,d22,d23,d24,project,state from devicedata where eui=? and ?? is not null";
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
                result.add(0, new ChannelData(deviceEUI, channel, rs.getDouble(6 + channelIndex), rs.getTimestamp(5).getTime()));
            }
            pst.close();
            conn.close();
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
}
