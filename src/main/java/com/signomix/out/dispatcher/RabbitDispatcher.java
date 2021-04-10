package com.signomix.out.dispatcher;

import org.cricketmsf.exception.DispatcherException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.dispatcher.DispatcherIface;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class RabbitDispatcher extends OutboundAdapter implements Adapter, DispatcherIface {

    private static final Logger logger = LoggerFactory.getLogger(RabbitDispatcher.class);

    private String brokerURL;
    private String userName;
    private String password;
    private boolean debug = false;
    private String exchangeName = "events";
    private ConcurrentHashMap eventMap;
    Channel channel;
    private boolean ready = false;

    @Override
    public void dispatch(Event event) throws DispatcherException {
        if (eventMap.containsKey(event.getCategory() + "/*") || eventMap.containsKey(event.getCategory() + "/" + event.getType())) {
            try {
                channel.exchangeDeclare(exchangeName, "fanout");
                channel.basicPublish(exchangeName, "", null, event.toJson().getBytes("UTF-8"));
                logger.info("PUBLISHING EVENT");
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        } else {
            throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        eventMap = new ConcurrentHashMap();
        brokerURL = properties.get("url");
        this.properties.put("url", brokerURL);
        logger.info("\turl: {}", brokerURL);
        userName = properties.get("user");
        this.properties.put("user", userName);
        logger.info("\tuser: {}", userName);
        password = properties.get("password");
        this.properties.put("password", password);
        logger.info("\tpassword: {}", password);
        try {
            exchangeName = properties.getOrDefault("exchange", "events");
            this.properties.put("exchange", exchangeName);
            logger.info("\texchange: {}", exchangeName);
            String eventTypes = properties.getOrDefault("events", "");
            registerEventTypes(eventTypes);
            logger.info("\tevents: {}", eventTypes);
            logger.info("\tevents-configured: {}", eventMap.size());
        } catch (Exception e) {
            System.out.println("ERROR");
            e.printStackTrace();
        }
    }

    private void registerEventType(String category, String type) throws DispatcherException {
        if (type == null || type.isEmpty()) {
            eventMap.put(category + "/*", null);
        } else {
            eventMap.put(category + "/" + type, null);
        }
    }

    @Override
    public void registerEventTypes(String pathsConfig) {
        String[] paths = pathsConfig.split(";");
        for (String path : paths) {
            if (!path.isEmpty()) {
                eventMap.put(path, "");
            }
        }
    }

    @Override
    public void dispatch(EventDecorator event) throws DispatcherException {
        throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
    }

    @Override
    public DispatcherIface getDispatcher() {
        return this;
    }

    @Override
    public void start() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(brokerURL);
        factory.setUsername(userName);
        factory.setPassword(password);
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            ready = true;
        } catch (IOException | TimeoutException ex) {
            logger.info(ex.getMessage());
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }
    
    @Override
    public void destroy(){
        try {
            channel.close();
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
        } catch (TimeoutException ex) {
            logger.warn(ex.getMessage());
        }
    }
    
}
