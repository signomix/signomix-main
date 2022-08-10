package com.signomix.out.db;

import com.signomix.common.DeviceStatusLoaderIface;
import com.signomix.common.iot.Device;

public class H2DeviceStatusLoader implements DeviceStatusLoaderIface {

    private IotDbDataIface dao;

    public H2DeviceStatusLoader(IotDbDataIface dao) {
        this.dao = dao;
    }

    @Override
    public void updateDevice(Device device) {
        try {
            device = dao.getDeviceStatus(device.getEUI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
