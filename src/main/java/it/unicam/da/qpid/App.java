package it.unicam.da.qpid;

import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.amqp.AMQPUtils;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import io.radanalytics.streaming.amqp.JavaAMQPBodyFunction;

//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;

import org.apache.qpid.proton.message.Message;
import org.apache.spark.api.java.function.Function;

import scala.Option;
import scala.Tuple2;

public class App {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf().setMaster("local").setAppName("AppDA");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(5000));    	
        
        
        Function<Message, Option<String>> converter = new JavaAMQPBodyFunction<>();
		
		JavaReceiverInputDStream<String>  receiveStream =
		        AMQPUtils.createStream(jssc,
		        		"127.0.0.1",
		                5672,
		                Option.apply("guest"),
		                Option.apply("guest"),
		                "queue", converter, StorageLevel.MEMORY_ONLY());
		
		/*List<String> receivedMessage = new ArrayList<>();
        receiveStream.foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
            	receivedMessage.addAll(rdd.collect());
            }
        });*/
        
        JavaPairDStream<String, Integer> counts = receiveStream
    		    .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
    		    .mapToPair(word -> new Tuple2<>(word, 1))
    		    .reduceByKey((a, b) -> a + b);
        
        counts.print();
        
      
        jssc.start();
        
		
        Producer p1 = new Producer();
		p1.sendData();
		
		
        try {
			jssc.awaitTerminationOrTimeout(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jssc.stop();
		
		
		EdgeNode node = new EdgeNode();
		//receive 3 messages from the queue
		node.receiveData();
		
		/*for(String s : receivedMessage) {
			System.out.println(s);
		}*/
		
	}

}
