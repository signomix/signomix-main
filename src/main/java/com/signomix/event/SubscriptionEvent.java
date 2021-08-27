/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.event;

import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.event.EventIface;

public class SubscriptionEvent extends EventDecorator implements EventIface {

    public String userId;
    public String subscriptionName;
    public String userEmail;
    public String userName;
    public String language;
    public boolean subscribe;
    public SubscriptionEvent(String userId, String userName, String userEmail, String subscriptionName, String language, boolean subscribe) {
        super();
        this.userId=userId;
        this.userEmail=userEmail;
        this.userName=userName;
        this.subscriptionName=subscriptionName;
        this.language=language;
        this.subscribe=subscribe;
    }
    
}
