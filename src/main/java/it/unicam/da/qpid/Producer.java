package it.unicam.da.qpid;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Producer {
	
	public void sendData() {
		
		try {
			String [] data = {"Detected presence: ID 11111", "Detected presence: ID 22222", "Detected presence: ID 33333"};
            // The configuration for the Qpid InitialContextFactory has been supplied in
            // a jndi.properties file in the classpath, which results in it being picked
            // up automatically by the InitialContext constructor.
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination queue = (Destination) context.lookup("myQueueLookup");

            Connection connection = factory.createConnection("guest","guest");
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer messageProducer = session.createProducer(queue);
            
            for(String s : data) {
            	TextMessage requestMessage = session.createTextMessage(s);
                messageProducer.send(requestMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            }

            connection.close();
            
		} catch (Exception exp) {
			System.out.println("[Producer] Caught exception, exiting.");
			exp.printStackTrace(System.out);
			System.exit(1);
		}
	}
	
}
