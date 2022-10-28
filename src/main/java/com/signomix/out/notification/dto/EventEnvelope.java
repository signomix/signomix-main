package com.signomix.out.notification.dto;

import java.util.UUID;
import org.cricketmsf.microsite.user.User;

public class EventEnvelope {

    public static final String DEFAULT = "DEFAULT";
    public static final String USER = "USER";
    public static final String DEVICE = "DEVICE";
    public static final String DASHBOARD = "DASHBOARD";
    public static final String DATA = "DATA";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String GROUP = "GROUP";
    public static final String APPLICATION = "APPLICATION";

    public UUID uuid;
    public String type;
    public String id;
    public String payload;
    public long timestamp;

    public EventEnvelope() {
        uuid = UUID.randomUUID();
        timestamp=System.currentTimeMillis();
    }
}
