package edu.chop.dgd.Process;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by jayaramanp on 12/8/17.
 */




public class JMSBean {

    ActiveMQConnectionFactory jmsConnectionFactory;

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

}
