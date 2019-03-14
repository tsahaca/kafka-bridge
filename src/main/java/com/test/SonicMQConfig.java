package com.test;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jndi.JndiTemplate;
//import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
//import javax.jms.ConnectionFactory;
import progress.message.jclient.ConnectionFactory;
//import javax.jms.Destination;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.jms.support.destination.JndiDestinationResolver;


@Configuration
public class SonicMQConfig {
    
    @Value("${SonicMQ.brokerUrl}")
    private String brokerUrl;
      
    @Value("${SonicMQ.jmsUser}")
    private String jmsUser;

    @Value("${SonicMQ.jmsPassword}")
    private String jmsPassword;

    @Value("${SonicMQ.domain}")
    private String domain;

    @Value ("${SonicMQ.connectionFactory}")
    private String connectionFactory;

    @Value ("${SonicMQ.destination}")
    private String destination;
    
    @Value ("${SonicMQ.clientId}")
    private String clientId;
    
    @Value ("${SonicMQ.subscription}")
    private String subscription;
    
        
    @Bean
    public JndiTemplate jndiTemplate() {
        
        return new JndiTemplate(getJndiProperties());
    }
    
    
    
    private Properties getJndiProperties(){
        final Properties environment = new Properties();
        
        environment.put("java.naming.factory.initial", "com.sonicsw.jndi.mfcontext.MFContextFactory");
        environment.put("com.sonicsw.jndi.mfcontext.idleTimeout", "60000");
        environment.put("com.sonicsw.jndi.mfcontext.requestTimeout", "20000");
        environment.put("com.sonicsw.jndi.mfcontext.connectTimeout", "4000");
        environment.put("com.sonicsw.jndi.mfcontext.socketConnectTimeout","1500");
        
        environment.put("java.naming.provider.url", brokerUrl);
        environment.put("java.naming.security.principal", jmsUser);
        environment.put("java.naming.security.credentials", jmsPassword);
        environment.put("com.sonicsw.jndi.mfcontext.domain", domain);
       
        return environment;
    }
    
    @Bean
    public ConnectionFactory jndiObjectFactoryBean() {
        
        final JndiTemplate template = jndiTemplate();
        
        try{
            
            return (ConnectionFactory) template.lookup(connectionFactory);
            
        } catch(Exception ex){
            ex.printStackTrace(); 
        }
        
        return null;
        
        
    }
    
    @Bean JndiDestinationResolver jndiDestinationResolver() {
    	final JndiDestinationResolver jndiResolver = new JndiDestinationResolver();
    	jndiResolver.setJndiTemplate(jndiTemplate());
    	return jndiResolver;
    }
    
    
    @Bean()
    public UserCredentialsConnectionFactoryAdapter userCredentialConnectionFactoryBean() {
        final UserCredentialsConnectionFactoryAdapter userCredFac= new UserCredentialsConnectionFactoryAdapter();
        
        userCredFac.setTargetConnectionFactory(jndiObjectFactoryBean());
        userCredFac.setUsername(jmsUser);
        userCredFac.setPassword(jmsPassword);
                
        return  userCredFac;
    }
    
    @Bean 
    public JmsConfiguration sonicMQJMSConfig() {
    	final JmsConfiguration jmsConfig = new JmsConfiguration();
    	jmsConfig.setConnectionFactory(userCredentialConnectionFactoryBean());
    	jmsConfig.setDestinationResolver(jndiDestinationResolver());
    	jmsConfig.setConcurrentConsumers(1);
    	return jmsConfig;
    }
    
    @Bean 
    public JmsComponent sonicMQComponent() {
    	final JmsComponent jmsComponent = new JmsComponent();
    	jmsComponent.setConfiguration(sonicMQJMSConfig());
    	return jmsComponent;
    }
      
}
