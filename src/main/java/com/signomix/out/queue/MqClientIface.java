package com.signomix.out.queue;

public interface MqClientIface {

    public void sendNotification(String message);

    public void sendMailing(String message);

    public void sendAdminEmail(String message);
}
