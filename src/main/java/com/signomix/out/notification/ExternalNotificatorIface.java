/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.notification;

public interface ExternalNotificatorIface {

    public String getEndpoint();
    public String send(MessageWrapper messageWrapper);
}
