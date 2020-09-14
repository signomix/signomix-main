/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.notification;

/**
 *
 * @author greg
 */
public interface NotificationIface {
    
    public String send(String userID, String recipient, String nodeName, String message);
    public String send(String recipient, String nodeName, String message);
    public String getChatID(String recipent);
    
}
