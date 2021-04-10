package com.signomix.in.dispatcher;

/**
 *
 * @author greg
 */
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.cricketmsf.in.event.EventHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.event.EventListenerIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitEventListener extends InboundAdapter implements Adapter, EventListenerIface {

    private static final Logger logger = LoggerFactory.getLogger(RabbitEventListener.class);

    private static final String EXCHANGE_NAME = "logs";

    private String brokerURL;
    private String userName;
    private String password;
    private String exchangeName;
    Channel channel;
    private boolean ready = false;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
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
            //registerEventTypes(eventTypes);
            logger.info("\tevents: {}", eventTypes);
            //logger.info("\tevent-types-configured: {}", eventMap.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(brokerURL);
            factory.setUsername(userName);
            factory.setPassword(password);
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchangeName, "");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String serializedEvent = new String(delivery.getBody(), "UTF-8");
                logger.info("HANDLING "+serializedEvent);
                Event event = Event.fromJson(serializedEvent);
                new Thread(
                        new EventHandler(event)
                ).start();
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
            ready = true;
        } catch (IOException | TimeoutException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void destroy() {
        try {
            channel.close();
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
        } catch (TimeoutException ex) {
            logger.warn(ex.getMessage());
        }
    }
}
