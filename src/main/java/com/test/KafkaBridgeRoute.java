package com.test;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.stereotype.Component;


@Component
public class KafkaBridgeRoute extends RouteBuilder {
    
    

    @Override
    public void configure () throws Exception {

        /**
         * When Trade XML is larger than 2 MB
         */
        onException(RecordTooLargeException.class)
                .routeId("Large_TradeXml_Handler")
                .to("file://{{camel.route.largeXMLStorage}}?fileName=${threadName}-${date:now:MM-dd-yyyy-HH-mm-ss-SSS}.xml")
                .log("Received Large TradeXML").handled(true);

        /**
         * When Kafka Brokers are offline
         */
        onException(TimeoutException.class)
                .routeId("Kafka_Brokers_Offline_Handler")
                .to("file://{{camel.route.offlineStorage}}?fileName=${threadName}-${date:now:MM-dd-yyyy-HH-mm-ss-SSS}.xml")
                .log("Received Trade when broker offline").handled(true);
        
        /**
         * When XML is not valid
         
        onException(SAXParseException.class).routeId("Invalid_XML_Handler")
                .to("file://{{camel.route.invalidXMLStorage}}")
                .log("Received Invalid Trade XML").handled(true);
         */
        
        onException(TypeConversionException.class)
                .routeId("TypeConversion_Exception_Handler")
                .to("file://{{camel.route.conversionFailedStorage}}?fileName=${threadName}-${date:now:MM-dd-yyyy-HH-mm-ss-SSS}.xml")
                .log("Received and failed to convert type").handled(true);

        onException(Exception.class)
             .routeId("All_Other_Exception_Handler")
             .process(new Processor() { 
                 public void process(Exchange exchange) throws Exception { 
                     Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                     log.error("Received body with other errors", exception); 
                     } 
                 })
                .to("file://{{camel.route.allOtherFailureStorage}}?fileName=${threadName}-${date:now:MM-dd-yyyy-HH-mm-ss-SSS}.xml")
                .log("Received body with other errors").handled(true);

        /**
         * onException(Exception.class).process(new Processor() { public void
         * process(Exchange exchange) throws Exception { Exception exception =
         * (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
         * log.error("Error Message6", exception); } }).log("Received
         * body5").handled(true);
         **/

        /**
        from("sonicmq:topic:{{SonicMQ.destination}}?clientId={{SonicMQ.clientId}}&durableSubscriptionName={{SonicMQ.subscription}}")
                .routeId(
                        "Sonic.{{SonicMQ.destination}}-Kafka.{{spring.kafka.targetTradeXMLTopic}}")
                .split(xpath("/tradeGroup/trade"))
                .to("kafka:{{spring.kafka.targetTradeXMLTopic}}?brokers={{spring.kafka.bootstrap-servers}}&requestTimeoutMs={{spring.kafka.producer-requestTimeoutMs}}");
        */        

        from("sonicmq:topic:{{SonicMQ.destination}}?clientId={{SonicMQ.clientId}}&durableSubscriptionName={{SonicMQ.subscription}}")
                .routeId(
                        "Sonic.{{SonicMQ.destination}}-Kafka.{{spring.kafka.targetTradeXMLTopic}}")
                .split()
                .tokenizeXML("trade")
                .streaming()
                .parallelProcessing()
                .removeHeaders("*")
                .to("kafka:{{spring.kafka.targetTradeXMLTopic}}?brokers={{spring.kafka.bootstrap-servers}}&requestTimeoutMs={{spring.kafka.producer-requestTimeoutMs}}&clientId={{SonicMQ.clientId}}");
      
        from("file://{{camel.route.offlineStorage}}?delete=true")
                .routeId(
                        "LocalStore-Kafka.{{spring.kafka.targetTradeXMLTopic}}")
                .autoStartup(false)
                .removeHeaders("*")
                .to("kafka:{{spring.kafka.targetTradeXMLTopic}}?brokers={{spring.kafka.bootstrap-servers}}&requestTimeoutMs={{spring.kafka.producer-requestTimeoutMs}}&clientId=OffLineStoreKafkaBridge");
    }

}
