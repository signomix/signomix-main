package com.signomix.out.notification.dto;

import java.util.UUID;
import org.cricketmsf.microsite.user.User;

public class MessageWrapper {
    public UUID uuid;
    public String type;
    public String eui;
    public String subject;
    public String message;
    public User user;
    
    public MessageWrapper(){
        uuid=UUID.randomUUID();
    }
}
