package com.test;

import org.apache.camel.CamelContext;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.MetricRegistry;

@SpringBootApplication
@EnableAutoConfiguration
public class SampleApplication {

    public static void main (final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    MetricRegistry metricRegistry () {
        return new MetricRegistry();
    }

    @Bean
    CamelContextConfiguration contextConfiguration () {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart (final CamelContext context) {
                // LOG.info("Configuring Camel metrics on all routes");
                final MetricsRoutePolicyFactory fac = new MetricsRoutePolicyFactory();
                fac.setMetricsRegistry(metricRegistry());
                context.addRoutePolicyFactory(fac);
            }

            @Override
            public void afterApplicationStart (
                    final CamelContext camelContext) {
                // noop
            }
        };
    }

}

