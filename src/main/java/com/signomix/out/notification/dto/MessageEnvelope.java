package com.signomix.out.notification.dto;

import java.util.UUID;
import org.cricketmsf.microsite.user.User;

public class MessageEnvelope {

    public static final String GENERAL = "GENERAL";
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ALERT = "ALERT";
    public static final String DEVICE_LOST = "DEVICE_LOST";
    public static final String PLATFORM_DEVICE_LIMIT_EXCEEDED = "PLATFORM_DEVICE_LIMIT_EXCEEDED";
    public static final String ADMIN_EMAIL = "ADMIN_EMAIL";
    public static final String DIRECT_EMAIL = "DIRECT_EMAIL";
    public static final String MAILING = "MAILING";
    public static final String NEXTMAILING = "NEXTMAILING";

    public UUID uuid;
    public String type;
    public String eui;
    public String subject;
    public String message;
    public User user;

    public MessageEnvelope() {
        uuid = UUID.randomUUID();
    }
}
