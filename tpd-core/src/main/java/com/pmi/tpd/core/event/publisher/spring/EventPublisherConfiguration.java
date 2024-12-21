package com.pmi.tpd.core.event.publisher.spring;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

@Configuration
public class EventPublisherConfiguration {

    /** */
    public static final String EVENT_PUBLISHER_BEAN_NAME = "eventPublisher";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherConfiguration.class);

    @PostConstruct
    public void initEventPublishing() {
        LOGGER.info("Event Publisher is initializing...");

    }

    /**
     * <p>
     * eventPublisher.
     * </p>
     *
     * @return a {@link EventPublisherFactoryBean} object.
     */
    @Bean(EVENT_PUBLISHER_BEAN_NAME)
    public static EventPublisherFactoryBean eventPublisher(final ITransactionSynchronizer synchronizer) {
        return new EventPublisherFactoryBean(synchronizer);
    }

}
