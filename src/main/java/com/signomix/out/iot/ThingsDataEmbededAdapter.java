/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.iot;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.signomix.common.iot.ChannelData;
import com.signomix.common.iot.Device;
import com.signomix.event.IotEvent;
import com.signomix.out.db.IotDataStorageIface;
import com.signomix.out.db.IotDatabaseIface;

import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ThingsDataEmbededAdapter extends OutboundAdapter implements Adapter, ThingsDataIface {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ThingsDataEmbededAdapter.class);

    private String helperAdapterName; // IoT DB
    private String helperAdapterName2; // IoT data DB
    private boolean initialized = false;
    String monitoringDeviceEui;

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
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
        monitoringDeviceEui=(String) Kernel.getInstance().getProperties().get("monitoring_device");
    }

    @Override
    public void putData(String userID, String deviceEUI, String project, Double deviceState, List<ChannelData> values) throws ThingsDataException {
        if (!isAuthorized(userID, -1, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        //TODO: dla urządzeń wirtualnych uruchomić skrypt preprocessora
        getDataStorage().putData(userID, deviceEUI, project, deviceState, values);
    }

    @Override
    //REMOVE
    public void putData(String userID, String deviceEUI, String project, Double deviceState, String channel, ChannelData value) throws ThingsDataException {
        if (!isAuthorized(userID, -1, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        ArrayList<ChannelData> list = new ArrayList<>();
        list.add(value);
        getDataStorage().putData(userID, deviceEUI, project, deviceState, list);
    }

    /*
    @Override
    public List<ChannelData> getAllValues(String userID, String deviceEUI, String channel) throws ThingsDataException {
        if (!isAuthorized(userID, deviceEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getAllValues(userID, deviceEUI, channel);
    }
     */
    @Override
    public ChannelData getLastValue(String userID, String deviceEUI, String channel) throws ThingsDataException {
        if (!isAuthorized(userID, -1, deviceEUI)) {
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
        //Device previous = getDevice(userID, device.getEUI(), false);
        Device previous = getDevice(userID, device.getEUI(), true);
        if (previous == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        }else{
            device.setUserID(previous.getUserID()); //UserID override protection if modified by the admin.
        }
        //TODO: what to do when list of channels has been changed?
        getIotDB().updateDevice(device);
        if (getDataStorage().updateDeviceChannels(device, previous) > 0) {
            IotEvent event = new IotEvent(IotEvent.INFO, "all data channels have been removed because of the device channels modification");
            event.setOrigin(userID + "\t" + device.getEUI());
            Kernel.getInstance().dispatchEvent(event);
        }
    }

    @Override
    public void updateHealthStatus(String EUI, long lastSeen, long frameCounter, String downlink, String deviceID) throws ThingsDataException {
        Device dev = getDevice(EUI);
        if (dev == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        } else if(null!=monitoringDeviceEui){
            logger.debug("virtual data to {} {}",EUI,monitoringDeviceEui);
            if (!EUI.equalsIgnoreCase(monitoringDeviceEui)) {
                String cmd = Base64.getEncoder().encodeToString("datareceived".getBytes());
                Kernel.getInstance().dispatchEvent(new IotEvent(IotEvent.PLATFORM_MONITORING, cmd));
            }
        }
        dev.setLastSeen(lastSeen);
        dev.setLastFrame(frameCounter);
        dev.setDownlink(downlink);
        dev.setAlertStatus(Device.OK);
        dev.setDeviceID(deviceID);
        getIotDB().updateDevice(dev); 
        //TODO: getIotDB().setDeviceStatus(EUI, lastSeen, frameCounter, downlink, Device.OK, deviceID);
    }

    @Override
    public void updateAlertStatus(String EUI, int newAlertStatus) throws ThingsDataException {
        Device dev = getDevice(EUI);
        if (dev == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        }
        dev.setAlertStatus(newAlertStatus);
        getIotDB().updateDevice(dev);
        //TODO: getIotDB().setDeviceAlertStatus(EUI, newAlertStatus);
    }

    @Override
    public void updateDeviceState(String EUI, Double newState) throws ThingsDataException {
        Device dev = getDevice(EUI);
        if (dev == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "device not found");
        }
        dev.setState(newState);
        getIotDB().updateDevice(dev);
        //TODO: getIotDB().setDeviceStatus(EUI, newState);
    }

    @Override
    public Device getDevice(String userId, String deviceEUI, boolean withShared) throws ThingsDataException {
        return getIotDB().getDevice(userId, deviceEUI, withShared);
    }
    @Override
    public boolean checkAccess(String userId, String deviceEUI, long organizationID, boolean withShared) throws ThingsDataException {
        return getIotDB().checkAccess(userId, deviceEUI, organizationID, withShared);
    }

    @Override
    public Device getDevice(String deviceEUI) throws ThingsDataException {
        return getIotDB().getDevice(deviceEUI);
    }

    @Override
    public List<Device> getUserDevices(String userID, long organizationID, boolean withShared) throws ThingsDataException {
        return getIotDB().getUserDevices(userID, organizationID, withShared);
    }

    @Override
    public List<Device> getGroupDevices(String userID, long organizationID, String group) throws ThingsDataException {
        return getIotDB().getGroupDevices(userID, organizationID, group);
    }

    @Override
    public int getUserDevicesCount(String userID) throws ThingsDataException {
        return getIotDB().getUserDevicesCount(userID);
    }

    @Override
    public List<DeviceTemplate> getTemplates() throws ThingsDataException {
        return getIotDB().getDeviceTemplates();
    }

    @Override
    public List<List> getLastValues(String userID, String deviceEUI) throws ThingsDataException {
        if (!isAuthorized(userID, -1, deviceEUI)) {
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
     *
     * @Override public List<List> getValues(String userID, String deviceEUI,
     * String channel, String query) throws ThingsDataException { if
     * (!isAuthorized(userID, deviceEUI)) { throw new
     * ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not
     * authorized"); } //TODO: if we query more than 1 channel (channels
     * sepatated with "," then resulting channel data could be unsynchronized
     * //TODO: there should be additional option to synchdonize data lists
     * return getDataStorage().getValues(userID, deviceEUI, channel, query); }
     */
    @Override
    public List<List> getValues(String userID, String deviceEUI, String query) throws ThingsDataException {
        return getDataStorage().getValues(userID, deviceEUI, query);
    }

    @Override
    public boolean isAuthorized(String userID, long organizationID, String deviceEUI) throws ThingsDataException {
        return getIotDB().isAuthorized(userID, organizationID, deviceEUI);
    }

    @Override
    public boolean isGroupAuthorized(String userID, long organizationID, String groupEUI) throws ThingsDataException {
        return getIotDB().isGroupAuthorized(userID, organizationID, groupEUI);
    }

    @Override
    public void saveAlert(Event event) throws ThingsDataException {
        getIotDB().addAlert(event);
    }

    @Override
    public List getAlerts(String userId) throws ThingsDataException {
        return getIotDB().getAlerts(userId, true);
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
    public void removeChannel(String deviceEUI, String channelName) throws ThingsDataException {
        getDataStorage().removeChannel(deviceEUI, channelName);
    }

    @Override
    public List<Device> getInactiveDevices() throws ThingsDataException {
        return getIotDB().getInactiveDevices();
    }

    @Override
    public List<DeviceGroup> getUserGroups(String userID) throws ThingsDataException {
        return getIotDB().getUserGroups(userID);
    }

    @Override
    public DeviceGroup getGroup(String groupEUI) throws ThingsDataException {
        return getIotDB().getGroup(groupEUI);
    }

    @Override
    public DeviceGroup getGroup(String userId, String groupEUI) throws ThingsDataException {
        return getIotDB().getGroup(userId, groupEUI);
    }

    @Override
    public void putGroup(String userID, DeviceGroup group) throws ThingsDataException {
        if (userID.equals(group.getUserID()) || group.userIsTeamMember(userID)) {
            getIotDB().putGroup(group);
        } else {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "user IDs not match");
        }
    }

    @Override
    public void modifyGroup(String userID, DeviceGroup group) throws ThingsDataException {
        DeviceGroup previous = getGroup(userID, group.getEUI());
        if (previous == null) {
            throw new ThingsDataException(ThingsDataException.NOT_FOUND, "group not found");
        }else{
            group.setUserID(previous.getUserID()); //UserID override protection if modified by the admin.
        }
        //TODO: what to do when list of channels has been changed?
        getIotDB().updateGroup(group);
    }

    @Override
    public void removeGroup(String userID, String groupEUI) throws ThingsDataException {
        DeviceGroup group = getIotDB().getGroup(userID, groupEUI);
        if (userID.equals(group.getUserID()) || group.userIsTeamMember(userID)) {
            getIotDB().removeGroup(groupEUI);
        } else {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "user IDs not match");
        }
    }

    @Override
    public List<List<List>> getValuesOfGroup(String userID, long organizationID, String groupEUI, String[] channelNames) throws ThingsDataException {
        if (!isGroupAuthorized(userID, organizationID, groupEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getValuesOfGroup(userID, organizationID, groupEUI, channelNames, 0,"");
    }
    @Override
    public List<List<List>> getLastValuesOfGroup(String userID, long organizationID, String groupEUI, String[] channelNames, long interval, String dataQuery) throws ThingsDataException {
        if (!isGroupAuthorized(userID, organizationID, groupEUI)) {
            throw new ThingsDataException(ThingsDataException.NOT_AUTHORIZED, "not authorized");
        }
        return getDataStorage().getValuesOfGroup(userID, organizationID, groupEUI, channelNames, interval, dataQuery);
    }

    @Override
    public void clearAllChannelsLimit(String deviceEUI, long limit) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeUserAlertsLimit(String userId, long limit) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
