/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.db;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.db.KeyValueDB;
import org.cricketmsf.out.db.KeyValueDBException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class KeyValueIotDataStorageDB extends KeyValueDB implements IotDataStorageIface {

    @Override
    public void putData(String userID, String deviceEUI, List<ChannelData> values) throws ThingsDataException {
        if (values.size() > 20) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "too many values");
        }
        String tableName;
        ChannelData cd;
        for (int i = 0; i < values.size(); i++) {
            cd = values.get(i);
            tableName = getTableNameForChannel(deviceEUI, cd.getName());
            try {
                put(tableName, "" + cd.getTimestamp(), cd);
            } catch (KeyValueDBException ex) {
                if (ex.getCode() == KeyValueDBException.TABLE_NOT_EXISTS) {
                    createChannel(deviceEUI, cd.getName());
                    try {
                        put(tableName, "" + cd.getTimestamp(), cd);
                    } catch (KeyValueDBException ex2) {
                        throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex2.getMessage());
                    }
                } else {
                    throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
                }
            }
        }

    }

    private String getTableNameForChannel(String deviceEUI, String channel) {
        return deviceEUI + "#" + channel;
    }

    public void createChannel(String deviceEUI, String path) throws ThingsDataException {
        //TODO: better control of the path format (allowed: [0-9][a-z][.])
        if (path.contains("#")) {
            throw new ThingsDataException(ThingsDataException.MALFORMED_PATH, path);
        }
        String tableName = getTableNameForChannel(deviceEUI, path);
        try {
            //TODO: configurable channel capacity
            addTable(tableName, 100, true);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public List<String> getDeviceChannels(String deviceEUI) throws ThingsDataException {
        String prefix = deviceEUI + "#";
        ArrayList<String> result = new ArrayList<>();
        try {
            List<String> list = getTableNames();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).startsWith(prefix)) {
                    result.add(list.get(i));
                }
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
        return result;
    }

    @Override
    public void putDeviceChannels(String deviceEUI, String channelNames) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putDeviceChannels(String deviceEUI, List<String> channelNames) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        try {
            String tableName = getTableNameForChannel(deviceEUI, channel);
            Map<String, ChannelData> map = getAll(tableName);
            ArrayList<ChannelData> list = new ArrayList<>();
            map.values().forEach(value -> list.add(value));
            return list;
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        ChannelData value = null;
        List<ChannelData> l = getAllValues(userID, deviceEUI, channel);
        value = l.get(l.size() - 1);
        return value;
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        try {
            deleteTable(getTableNameForChannel(deviceEUI, channelName));
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        ArrayList<List> result = new ArrayList<>();
        ArrayList<ChannelData> row = new ArrayList<>();
        ArrayList list = (ArrayList) getDeviceChannels(deviceEUI);
        for (int i = 0; i < list.size(); i++) {
            row.add(getLastValue(userID, deviceEUI, (String) list.get(i)));
        }
        result.add(row);
        return result;
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, String channel, String query) throws ThingsDataException {
        //TODO: get several channels - see H2DataStorageBD
        /*
        String tableName = getTableNameForChannel(deviceEUI, channel);
        int resultSize = 1;
        String[] params = query.split(";");
        for (int i = 0; i < params.length; i++) {
            if (params[i].startsWith("last ")) {
                try {
                    resultSize = Integer.parseInt(params[i].substring(params[i].indexOf(" ") + 1));
                } catch (Exception e) {
                    throw new ThingsDataException(ThingsDataException.BAD_REQUEST, "malformed query [" + query + "]");
                }
            }
        }

        List result = new ArrayList();
        if (resultSize == 1) {
            result.add(getLastValue(userID, deviceEUI, channel));
            return result;
        } else if (resultSize > 100) { //TODO: configuration
            resultSize = 100;
        }
        try {
            List list = search(tableName, new MeasurePseudoComparator(), null);
            int startIndex = list.size() - resultSize;
            if (startIndex < 0) {
                startIndex = 0;
            }
            for (int i = startIndex; i < list.size(); i++) {
                result.add(list.get(i));
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
        */
        throw new ThingsDataException(ThingsDataException.BAD_REQUEST);
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException {
        List<List> result = new ArrayList<>();
        if ("last".equalsIgnoreCase(query)) {
            result.add(getLastValues(userID, deviceEUI));
        } else {
            int index = query.indexOf(";");
            if (index > 0) {
                String channel = query.substring(0, index);
                result.add(getValues(userID, deviceEUI, channel, query.substring(index + 1)));
            } else {
                //TODO: better handling
                throw new ThingsDataException(ThingsDataException.UNKNOWN, "malformed query");
            }
        }
        return result;
    }

    @Override
    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException {
        List l = getDeviceChannels(deviceEUI);
        String name;
        String tableName;
        Iterator it;
        ChannelData data;
        for (int i = 0; i < l.size(); i++) {
            name = (String) l.get(i);
            try {
                tableName = getTableNameForChannel(deviceEUI, name);
                it = getAll(tableName).keySet().iterator();
                while (it.hasNext()) {
                    data = (ChannelData) it.next();
                    if (data.getTimestamp() < checkPoint) {
                        remove(tableName, data.getName());
                    }
                }
            } catch (KeyValueDBException ex) {
                throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
            }
        }
    }

    @Override
    public int updateDeviceChannels(Device device, Device oldDevice) throws ThingsDataException {
        if (oldDevice == null) {
            return 0;
        }
        HashMap oldChannels = oldDevice.getChannels();
        HashMap newCHannels = device.getChannels();
        oldChannels.keySet().forEach(key -> {
            if (!newCHannels.containsKey(key)) {
                try {
                    removeChannel(device.getEUI(), (String) key);
                } catch (ThingsDataException ex) {
                    Kernel.handle(Event.logWarning(this.getClass().getSimpleName() + ".modifyDevice()", "unnknown channel " + key + " of device " + device.getEUI()));
                }
            }
        });
        return 1;
    }

    @Override
    public void removeAllChannels(String deviceEUI) throws ThingsDataException {
        List l = getDeviceChannels(deviceEUI);
        String name;
        for (int i = 0; i < l.size(); i++) {
            name = (String) l.get(i);
            removeChannel(deviceEUI, name);
        }
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, int limit) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, int limit, boolean tsFormat) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
