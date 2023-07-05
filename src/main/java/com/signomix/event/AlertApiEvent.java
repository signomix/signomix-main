/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.event;

import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.event.EventIface;

public class AlertApiEvent extends EventDecorator implements EventIface {

    public String alertId;
    public String userId;
    public String limit;
    public String offset;
    public AlertApiEvent(String alertId, String userId, String limit, String offset) {
        super();
        this.alertId=alertId;
        this.userId=userId;
        this.limit=limit;
        this.offset=offset;
    }
    
}
