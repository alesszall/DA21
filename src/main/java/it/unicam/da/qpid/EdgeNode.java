package it.unicam.da.qpid;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class EdgeNode {
	
	public void receiveData()  {
		
		try {
			// The configuration for the Qpid InitialContextFactory has been supplied in
	        // a jndi.properties file in the classpath, which results in it being picked
	        // up automatically by the InitialContext constructor.
	        Context context = new InitialContext();

	        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
	        Destination queue = (Destination) context.lookup("myQueueLookup");

	        Connection connection = factory.createConnection("guest","guest");
	        connection.start();

	        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	        MessageConsumer messageConsumer = session.createConsumer(queue);
	        
	        for(int i = 0; i < 3; i++) {
	        	//Receive messages
				TextMessage requestMessage = (TextMessage) messageConsumer.receive();
				System.out.println("[EdgeNode] Received: " + requestMessage.getText());
	        }
			
			connection.close();
			
		} catch (Exception exp) {
			System.out.println("[EdgeNode] Caught exception, exiting.");
	        exp.printStackTrace(System.out);
	        System.exit(1);
	        }
	}

}
