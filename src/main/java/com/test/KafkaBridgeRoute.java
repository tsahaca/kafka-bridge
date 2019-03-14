
package com.test;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.camel.TypeConversionException;
import org.xml.sax.SAXParseException;


@Component
public class KafkaBridgeRoute extends RouteBuilder {
	
	@Autowired
	private JmsComponent sonicMQComponent;
	   

    @Override
    public void configure() throws Exception {
    	
    	final CamelContext context = this.getContext();
    	context.addComponent("sonicmq", sonicMQComponent);
    	
    	onException(RecordTooLargeException.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	//log.error("Error Message", exception);
            	handleErrorMessage("Error Message ="+ exception.getMessage());
                log.info("handling ex " +  exchange.getIn().getBody(String.class));
            }
        }).log("Received body").handled(true);
    	
    	onException(TimeoutException.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	//log.error("Error Message2", exception);
            	handleErrorMessage("Error Message2 ="+ exception.getMessage());
                log.info("handling ex2 " +  exchange.getIn().getBody(String.class));
            }
        }).log("Received body2").handled(true);

    	onException(SAXParseException.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	//log.error("Error Message3", exception);
            	handleErrorMessage("Error Message3 ="+ exception.getMessage());
                log.info("handling ex3" +  exchange.getIn().getBody(String.class));
            }
        }).log("Received body3").handled(true);
    	
    	onException(SAXParseException.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	//log.error("Error Message3", exception);
            	handleErrorMessage("Error Message4 ="+ exception.getMessage());
                log.info("handling ex4" +  exchange.getIn().getBody(String.class));
            }
        }).log("Received body4").handled(true);

    	onException(TypeConversionException.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	//log.error("Error Message3", exception);
            	handleErrorMessage("Error Message5 ="+ exception.getMessage());
                log.info("handling ex5" +  exchange.getIn().getBody(String.class));
            }
        }).log("Received body5").handled(true);

    	onException(Exception.class).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
            	Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            	log.error("Error Message6", exception);
            	//handleErrorMessage("Error Message6="+ exception.getMessage());
            }
        }).log("Received body5").handled(true);
    	

    	
    	from("sonicmq:topic:SampleT1?clientId={{SonicMQ.clientId}}&durableSubscriptionName={{SonicMQ.subscription}}")
    	.split(xpath("/tradeGroup/trade"))
        .to("kafka:test?brokers={{spring.kafka.bootstrap-servers}}&requestTimeoutMs={{spring.kafka.producer-requestTimeoutMs}}");
    	//.to("kafka:test?brokers={{spring.kafka.bootstrap-servers}}");
    	
    	/**
    	from("timer:bar")
        .setBody(constant("Hello from Sonic MQ to Kafka Brokers Durable Subscriber"))
        .to("sonicmq:topic:SampleT1");	
    	*/
    }
    
    private void handleErrorMessage(String message) {
    	log.info(message);
    }

}
