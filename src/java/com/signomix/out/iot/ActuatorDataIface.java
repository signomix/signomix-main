/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import java.util.List;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface ActuatorDataIface {
    public void init(String helperName) throws ThingsDataException;
    public List getCommands(String deviceEUI) throws ThingsDataException;
    public void removeAllCommands(String deviceEUI) throws ThingsDataException;
    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException;
    public void clearAllCommandsLimit(String deviceEUI, long limit) throws ThingsDataException;
}
