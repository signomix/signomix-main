/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.notification;

import com.signomix.out.notification.dto.MessageWrapper;

public interface ExternalNotificatorIface {

    public boolean isConfigured();
    public String send(MessageWrapper messageWrapper);
}
