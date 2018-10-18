/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import com.signomix.out.db.ActuatorCommandsDBIface;
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
public class ActuatorDataEmbededAdapter extends OutboundAdapter implements Adapter, ActuatorDataIface {

    private String helperAdapterName; // IoT DB
    private boolean initialized = false;

    private ActuatorCommandsDBIface getActuatorDB() {
        return (ActuatorCommandsDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
    }


    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        try {
            init(helperAdapterName);
        } catch (ThingsDataException e) {
            e.printStackTrace();
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public void init(String helperName) throws ThingsDataException {
    }

    @Override
    public List getCommands(String deviceEUI) throws ThingsDataException {
        return getActuatorDB().getAllCommands(deviceEUI);
    }

    @Override
    public void removeAllCommands(String deviceEUI) throws ThingsDataException {
        getActuatorDB().removeAllCommands(deviceEUI);
        getActuatorDB().removeAllLogs(deviceEUI);
    }

    @Override
    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException {
        getActuatorDB().clearAllCommands(deviceEUI, checkPoint);
        getActuatorDB().clearAllLogs(deviceEUI, checkPoint);
    }

}
