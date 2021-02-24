/**
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.notification;

import com.signomix.out.iot.Device;

/**
 *
 * @author greg
 */
public interface CommandWebHookIface {
    public boolean send(Device device, String payload, boolean hexRepresentation);
}
