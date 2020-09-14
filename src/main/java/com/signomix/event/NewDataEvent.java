/*
 * Copyright 2020 NMG S.A.
 */
package com.signomix.event;

import org.cricketmsf.Event;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.event.EventIface;

public class NewDataEvent extends EventDecorator implements EventIface {

    public NewDataEvent(Event event) {
        super(event);
    }
    
}
