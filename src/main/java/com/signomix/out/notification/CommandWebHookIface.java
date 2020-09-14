/**
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.notification;

/**
 *
 * @author greg
 */
public interface CommandWebHookIface {
    public boolean send(String deviceEUI, String deviceKey, String payload, boolean hexRepresentation);
}
