package com.signomix.out.notification;

import com.signomix.out.notification.dto.MessageEnvelope;
import com.cedarsoftware.util.io.JsonWriter;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;
import org.cricketmsf.out.dispatcher.DispatcherIface;

public class JmsNotificator extends OutboundAdapter implements ExternalNotificatorIface, OutboundAdapterIface, Adapter {

    private boolean configured = false;
    private String url;
    private String queueName;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        url = properties.getOrDefault("url", "");
        this.properties.put("url", url);
        queueName = properties.getOrDefault("queue", "");
        this.properties.put("queue", queueName);
        if (!queueName.isBlank() && !url.isBlank()) {
            configured = true;
        }
    }

    @Override
    public boolean isReady() {
        return configured;
    }

    @Override
    public String send(MessageEnvelope messageWrapper) {
        Connection connection = null;

        try {
            //ConnectionFactory connectionFactory = new JmsConnectionFactory("amqp://localhost:5672");
            ConnectionFactory connectionFactory = new JmsConnectionFactory(url);

            // Step 1. Create an amqp qpid 1.0 connection
            connection = connectionFactory.createConnection();
            // Step 2. Create a session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Step 3. Create a producer
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);
            
            Map jargs = new HashMap();
            jargs.put(JsonWriter.TYPE, false);
            String json = JsonWriter.objectToJson(messageWrapper, jargs);

            producer.send(session.createTextMessage(json));
            connection.start();
            connection.close();
        } catch (JMSException ex) {
        } finally {

        }
        return "";
    }  

}
