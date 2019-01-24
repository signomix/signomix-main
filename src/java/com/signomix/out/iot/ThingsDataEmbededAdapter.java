/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import com.signomix.iot.IotEvent;
import com.signomix.out.db.IotDataStorageIface;
import com.signomix.out.db.IotDatabaseIface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ThingsDataEmbededAdapter extends OutboundAdapter implements Adapter, ThingsDataIface {

    private String helperAdapterName; // IoT DB
    private String helperAdapterName2; // IoT data DB
    private boolean initialized = false;

    @Override
    public void init(String helperName, String helperName2) throws ThingsDataException {
    }

    private IotDatabaseIface getIotDB() {
        return (IotDatabaseIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
    }

    private IotDataStorageIface getDataStorage() {
        return (IotDataStorageIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName2);
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        helperAdapterName2 = properties.get("helper-name2");
        Kernel.getInstance().getLogger().print("\thelper-name2: " + helperAdapterName2);
        try {
            init(helperAdapterName, helperAdapterName2);
        } catch (ThingsDataException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public void putData(String userID, String deviceEUI, List<ChannelData> values) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        //TODO: dla urządzeń wirtualnych uruchomić skrypt preprocessora
        getDataStorage().putData(userID, deviceEUI, values);
    }

    @Override
    //REMOVE
    public void putData(String userID, String deviceEUI, String channel, ChannelData value) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        ArrayList<ChannelData> list = new ArrayList<>();
        list.add(value);
        getDataStorage().putData(userID, deviceEUI, list);
    }

    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getAllValues(userID, deviceEUI, channel);
    }

    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getLastValue(userID, deviceEUI, channel);
    }

    @Override
    public void putDevice(String userID, Device device) throws ThingsDataException {
        if (!userID.equals(device.getUserID())) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "user IDs not match");
        }
        getIotDB().putDevice(device);
        getDataStorage().updateDeviceChannels(device, null);
    }

    @Override
    public void modifyDevice(String userID, Device device) throws ThingsDataException {
        Device previous = getDevice(userID, device.getEUI(), false);
        if (previous == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        }
        //TODO: what to do when list of channels has been changed?
        getIotDB().updateDevice(device);
        if (getDataStorage().updateDeviceChannels(device, previous) > 0) {
            IotEvent event = new IotEvent(IotEvent.INFO, "all data channels have been removed because of the device channels modification");
            event.setOrigin(userID + "\t" + device.getEUI());
            Kernel.handle(event);
        }
    }

    @Override
    public void updateHealthStatus(String EUI, long lastSeen, long frameCounter, String downlink) throws ThingsDataException {
        Device dev = getDevice(EUI);
        if (dev == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        }
        dev.setLastSeen(lastSeen);
        dev.setLastFrame(frameCounter);
        dev.setDownlink(downlink);
        getIotDB().updateDevice(dev);
    }

    @Override
    public Device getDevice(String userId, String deviceEUI, boolean withShared) throws ThingsDataException {
        return getIotDB().getDevice(userId, deviceEUI, withShared);
    }

    @Override
    public Device getDevice(String deviceEUI) throws ThingsDataException {
        return getIotDB().getDevice(deviceEUI);
    }

    @Override
    public List<Device> getUserDevices(String userID, boolean withShared) throws ThingsDataException {
        return getIotDB().getUserDevices(userID, withShared);
    }
    
    @Override
    public int getUserDevicesCount(String userID) throws ThingsDataException {
        return getIotDB().getUserDevicesCount(userID);
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getLastValues(userID, deviceEUI);
    }

    /**
     * Search for MeasureValue objects in channel Ta wersja implementuje jedynie
     * query w postaci "last x" i zwraca ostatnie x obiektów zapisanych dla
     * kanału. W przypadku
     *
     * @param userID
     * @param deviceEUI
     * @param channel
     * @param query
     * @return
     * @throws ThingsDataException
     */
    @Override
    public List<List> getValues(String userID, String deviceEUI, String channel, String query) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        //TODO: if we query more than 1 channel (channels sepatated with "," then resulting channel data could be unsynchronized
        //TODO: there should be additional option to synchdonize data lists
        return getDataStorage().getValues(userID, deviceEUI, channel, query);
    }

    @Override
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException {
        return getDataStorage().getValues(userID, deviceEUI, query);
    }
    
    @Override
    public List<List> getValues(String userID, String deviceEUI, int limit) throws ThingsDataException {
        return getDataStorage().getValues(userID, deviceEUI, limit);
    }

    @Override
    public boolean isAuthorized(String userID, String deviceEUI) throws ThingsDataException {
        return getIotDB().isAuthorized(userID, deviceEUI);
    }

    @Override
    public void saveAlert(Event event) throws ThingsDataException {
        getIotDB().addAlert(event);
    }

    @Override
    public List getAlerts(String userId) throws ThingsDataException {
        return getIotDB().getAlerts(userId,true);
    }

    @Override
    public void removeAlert(long alertId) throws ThingsDataException {
        getIotDB().removeAlert(alertId);
    }

    @Override
    public void removeUserAlerts(String userId) throws ThingsDataException {
        getIotDB().removeAlerts(userId);
    }

    @Override
    public void removeDevice(String deviceEUI) throws ThingsDataException {
        removeAllChannels(deviceEUI);
        getIotDB().removeDevice(deviceEUI);
    }

    @Override
    public void removeAllDevices(String userId) throws ThingsDataException {
        getIotDB().removeAllDevices(userId);
    }

    @Override
    public void removeAllChannels(String deviceEUI) throws ThingsDataException {
        getDataStorage().removeAllChannels(deviceEUI);
    }

    @Override
    public void clearAllChannels(String deviceEUI, long checkPoint) throws ThingsDataException {
        getDataStorage().clearAllChannels(deviceEUI, checkPoint);
    }

    @Override
    public void removeUserAlerts(String userId, long checkPoint) throws ThingsDataException {
        getIotDB().removeAlerts(userId, checkPoint);
    }

    @Override
    public void removeOutdatedAlerts(long checkPoint) throws ThingsDataException {
        getIotDB().removeOutdatedAlerts(checkPoint);
    }

    @Override
    public List getChannels(String deviceEUI) throws ThingsDataException {
        return getDataStorage().getDeviceChannels(deviceEUI);
    }

    @Override
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        getDataStorage().removeChannel(deviceEUI, channelName);
    }

    /*
    @Override
    public void putVirtualData(String userID, Device device, ScriptingAdapterIface scriptingAdapter, List<ChannelData> values) throws ThingsDataException {
        ArrayList<ChannelData> finalValues = null;
        try {
            finalValues = DataProcessor.processValues((ArrayList) values, device, scriptingAdapter);
        } catch (Exception e) {
            Kernel.handle(Event.logWarning(this, e.getMessage()));
        }
        getDataStorage().putData(userID, device.getEUI(), finalValues);
    }
    */

    @Override
    public List<Device> getInactiveDevices() throws ThingsDataException {
        return getIotDB().getInactiveDevices();
    }
}
