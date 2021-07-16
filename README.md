# Unicam DA2021 - Apache Qpid

A Maven project in which producers of data send messages to an edge node through [AMQP](https://www.amqp.org/) (Advanced Message Queuing Protocol). A Broker-J is defined as a AMQP supporting MOM (message-oriented middleware) between producers and the edge node that communicate with "store and forward" mechanism from queues via Qpid JMS. Moreover, an AMQP connector for [Apache Spark Streaming](http://spark.apache.org/streaming/) is instantiated in order to ingest data (inbound messages) as a stream from the broker queue and analyze them with some simple Spark jobs in local mode.

# Project References

The following dependencies have been added in the POM file for making Qpid JMS and the AMQP connector respectively available within the project. 

```
<dependency>
  	<groupId>org.apache.qpid</groupId>
  	<artifactId>qpid-jms-client</artifactId>
  	<version>0.58.0</version>
</dependency>
  	
<dependency>
    <groupId>io.radanalytics</groupId>
    <artifactId>spark-streaming-amqp_2.11</artifactId>
    <version>0.3.0</version>
</dependency>
```

Additionally, Scala dependencies have been installed for the connector to function correctly. In order to run Spark and avoid errors with the imported projects elements (e.g. with interface Vertx), Java 8 is needed.

# Components

As shown in the JNDI configuration file, a queue named "queue" is expected to be defined in the broker listening on TCP port 5672. The edge node and a number of producers create an authenticated connection with the broker and consequently a message producer/consumer related to the queue for sending/receiving messages is instantiated. At the same time, the [Apache Spark Streaming connector for AMQP](https://github.com/radanalyticsio/streaming-amqp) takes as parameters an instance of a StreamingContext, the IP address, port, user, password and queue name of the remote AMQP node to connect, a message converter (a callback function which is called for every received AMQP message for converting it in the user desidered format that will be stored into the RDDs micro batches) and the Spark storage level to use and returns an InputDStream.

## JNDI configuration file

```
connectionfactory.myFactoryLookup = amqp://localhost:5672

queue.myQueueLookup = queue
```

## Producer

```
MessageProducer messageProducer = session.createProducer(queue);
TextMessage requestMessage = session.createTextMessage(messageToSend);
messageProducer.send(requestMessage, DeliveryMode.NON_PERSISTENT, 
    Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
```

## EdgeNode

```
MessageConsumer messageConsumer = session.createConsumer(queue);
TextMessage requestMessage = (TextMessage) messageConsumer.receive();
```

## Apache Spark Streaming connector for AMQP

```
Function<Message, Option<String>> converter = new JavaAMQPBodyFunction<>();

JavaReceiverInputDStream<String>  receiveStream =
	AMQPUtils.createStream(jssc,
		"127.0.0.1",
		5672,
		Option.apply("guest"),
		Option.apply("guest"),
		"queue", converter, StorageLevel.MEMORY_ONLY());
```