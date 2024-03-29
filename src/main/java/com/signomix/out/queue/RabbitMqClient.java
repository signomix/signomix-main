package com.signomix.out.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.EventEnvelope;
import com.signomix.out.notification.dto.MessageEnvelope;

public class RabbitMqClient extends OutboundAdapter implements MessageBrokerIface, Adapter {

    private Connection connection;
    private Channel channel;
    private boolean ready = false;

    private String host = "rabbitmq";
    private int port = 15672;
    private String userName = "user";
    private String password = "user";
    private String notificationQueue = "notifications";
    private String eventsExchange = "events";

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        host = properties.getOrDefault("host", host);
        if (host.startsWith("$")) {
            this.host = System.getenv(host.substring(1));
        }
        userName = properties.getOrDefault("user", userName);
        if (userName.startsWith("$")) {
            this.userName = System.getenv(userName.substring(1));
        }
        password = properties.getOrDefault("password", password);
        if (password.startsWith("$")) {
            this.password = System.getenv(password.substring(1));
        }
        notificationQueue = properties.getOrDefault("queue-notifications", notificationQueue);
        if (notificationQueue.startsWith("$")) {
            this.notificationQueue = System.getenv(notificationQueue.substring(1));
        }
        /* adminEmailQueue = properties.getOrDefault("queue-admin-email", adminEmailQueue);
        if (adminEmailQueue.startsWith("$")) {
            this.adminEmailQueue = System.getenv(adminEmailQueue.substring(1));
        } */
        try {
            String sPort = properties.getOrDefault("port", "" + port);
            if (sPort.startsWith("$")) {
                sPort = System.getenv(sPort.substring(1));
            }
            port = Integer.parseInt(sPort);
        } catch (NumberFormatException ex) {

        }
        Kernel.getInstance().getLogger().print("\thost: " + host);
        Kernel.getInstance().getLogger().print("\tport: " + port);
        Kernel.getInstance().getLogger().print("\tuser: " + userName);
        Kernel.getInstance().getLogger().print("\tqueue-notification: " + notificationQueue);
        init();
    }

    private void init() {
        // Kernel.getInstance().getLogger().print("initializing");
        Kernel.getInstance().getLogger().print("connecting MQ " + host + " " + port);
        while (!isReady()) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setPort(port);
                factory.setUsername(userName);
                factory.setPassword(password);
                factory.setVirtualHost("/"); // WARNING: Virtual host configured as "/" must be set in Java without "/"
                connection = factory.newConnection();
                channel = connection.createChannel();
                ready = true;
            } catch (TimeoutException | IOException ex) {
                Kernel.getInstance().getLogger().print("connection error: " + ex.getMessage() + " " + ex.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public String send(EventEnvelope envelope) {
        if (!ready) {
            init();
        }
        String encodedMessage;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            encodedMessage = objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, ex.getMessage()));
            return null;
        }
        try {
            channel.basicPublish(eventsExchange, "#", null, encodedMessage.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String send(MessageEnvelope envelope) {
        if (!ready) {
            init();
        }
        String encodedMessage;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            encodedMessage = objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, ex.getMessage()));
            return null;
        }
        switch (envelope.type) {
            case "GENERAL":
            case "INFO":
            case "WARNING":
            case "ALERT":
            case "DEVICE_LOST":
            case "PLATFORM_DEVICE_LIMIT_EXCEEDED":
            case "DIRECT_EMAIL":
                sendNotification(encodedMessage);
                break;
            /* case "ADMIN_EMAIL":
                sendAdminEmail(encodedMessage);
                break; */
            default:
                break;
        }
        return null;
    }

    // @Override
    private void sendNotification(String message) {
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, message));
        while (!ready) {
        }
        try {
            channel.basicPublish("", notificationQueue, null, message.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "message sent"));
    }

    // @Override
    /* private void sendAdminEmail(String message) {
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, message));
        while (!ready) {
        }
        try {
            channel.basicPublish("", adminEmailQueue, null, message.getBytes());
        } catch (IOException ex) {

        }
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "message sent"));
    } */

    @Override
    public void destroy() {
        try {
            channel.close();
            connection.close();
        } catch (TimeoutException | IOException ex) {

        }
    }
}
