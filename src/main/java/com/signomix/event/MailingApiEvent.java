/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.event;

import java.util.HashMap;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.event.EventIface;

public class MailingApiEvent extends EventDecorator implements EventIface {

    private HashMap<String,String> data;

    public MailingApiEvent(String documentId, String target) {
        super();
        data=new HashMap<>();
        data.put("documentId", documentId);
        data.put("target", target);
    }
    
    @Override
    public HashMap<String,String> getData(){
        return data;
    }
    
}
