/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.out.iot.ThingsDataException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Event;
import org.cricketmsf.out.db.KeyValueDB;
import org.cricketmsf.out.db.KeyValueDBException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class KeyValueCommandsDB extends KeyValueDB implements ActuatorCommandsDBIface {

    @Override
    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        try {
            put("commands", "" + commandEvent.getId(), commandEvent);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Event getLastCommand(String deviceEUI) throws ThingsDataException {
        return getLastCommand(deviceEUI, false);
    }
    
    private Event getLastCommand(String deviceEUI, boolean preview) throws ThingsDataException {
        Event command = null;
        Event ev;
        try {
            Map<String, Event> commands = getAll("commands");
            Iterator it = commands.values().iterator();
            while (it.hasNext()) {
                ev = (Event) it.next();
                if (deviceEUI.equals(ev.getOrigin())) {
                    command = ev;
                }
            }
            if (command != null && !preview) {
                remove("commands", "" + command.getId());
                putCommandLog(deviceEUI, command);
            }
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }

        return command;
    }

    @Override
    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        return getLastCommand(deviceEUI, true);
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
    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException {
        try {
            put("commandslog", "" + commandEvent.getId(), commandEvent);
        } catch (KeyValueDBException ex) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Event getLastLog(String deviceEUI) throws ThingsDataException {
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

    @Override
    public void removeCommand(long id) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
