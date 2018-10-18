/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.out.iot.ThingsDataException;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface ActuatorCommandsDBIface extends KeyValueDBIface {

    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException;

    public Event getLastCommand(String deviceEUI) throws ThingsDataException;

    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException;

    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException;

    public void removeAllCommands(String deviceEUI) throws ThingsDataException;

    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException;

    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException;

    public Event getLastLog(String deviceEUI) throws ThingsDataException;

    public void clearAllLogs(String deviceEUI, long checkPoint) throws ThingsDataException;

    public void removeAllLogs(String deviceEUI) throws ThingsDataException;

    public List<Event> getAllLogs(String deviceEUI) throws ThingsDataException;
}
