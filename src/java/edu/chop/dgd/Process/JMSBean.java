package edu.chop.dgd.Process;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;

/**
 * Created by jayaramanp on 12/8/17.
 */


public class JMSBean {

    ActiveMQConnectionFactory jmsConnectionFactory = new ActiveMQConnectionFactory();

    public void submit(String message) throws Exception {
        Connection connection = jmsConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue oligojobQueue = session.createQueue("antholigojobs");
        MessageProducer producer = session.createProducer(oligojobQueue);
        TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
        connection.stop();

    }


    public void submit(HashMap<String, String> mapMessage) throws Exception {
        Connection connection = jmsConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue oligojobQueue = session.createQueue("antholigojobs");
        MessageProducer producer = session.createProducer(oligojobQueue);
        MapMessage mapMessageToTransmit = session.createMapMessage();
        for(String key : mapMessage.keySet()){
            mapMessageToTransmit.setObject(key, mapMessage.get(key));
        }

        producer.send(mapMessageToTransmit);
        connection.stop();

    }

}
