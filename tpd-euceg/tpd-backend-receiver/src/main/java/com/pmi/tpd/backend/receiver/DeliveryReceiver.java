package com.pmi.tpd.backend.receiver;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;

import org.eu.ceg.AppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.DefaultEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.IEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.IReceiver;
import com.pmi.tpd.euceg.backend.core.delivery.RejectedMessageException;
import com.pmi.tpd.euceg.backend.core.delivery.mock.MessageHandlerFactory;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsMessageReceiver;
import com.pmi.tpd.euceg.backend.core.spi.SimpleKeyProvider;
import com.pmi.tpd.spring.context.ConfigFileLoader;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;

public class DeliveryReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryReceiver.class);

    @Inject
    private IReceiver receiver;

    public void start() {
        this.receiver.start();
        LOGGER.info("Started");
    }

    public void shutdown() {
        this.receiver.shutdown();
        LOGGER.info("Shutting down");
    }

    private static AnnotationConfigApplicationContext applicationContext;

    public static void main(final String[] args) throws IOException {
        LOGGER.info("Starting...");
        applicationContext = new AnnotationConfigApplicationContext(Initializer.class, Configurer.class);

        final DeliveryReceiver receiver = applicationContext.getBean(DeliveryReceiver.class);
        receiver.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                receiver.shutdown();
            }
        });

    }

    @Configuration
    @EnableConfigurationProperties({ BackendProperties.class })
    public static class Initializer implements ApplicationContextAware {

        @Override
        public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
            final ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
            try {
                ConfigFileLoader.load(env);
            } catch (final IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Configuration
    public static class Configurer {

        @Inject
        public BackendProperties properties;

        @Value("${app.keystore.location}")
        public Resource keyStoreLocation;

        @Value("${app.keystore.password}")
        public String password;

        @Bean
        public IKeyProvider keyProvider() {
            return new SimpleKeyProvider(keyStoreLocation, password);
        }

        @Bean
        public IEncryptionProvider encryptionProvider(final IKeyProvider keyProvider) {
            final DefaultEncryptionProvider encryptionProvider = new DefaultEncryptionProvider(keyProvider);
            encryptionProvider.setBackendProperties(properties);
            return encryptionProvider;
        }

        @Bean
        public IReceiver receiver(final IEncryptionProvider encryptionProvider) {
            final JmsMessageReceiver<?> receiver = new JmsMessageReceiver<>(new MessageCreator(encryptionProvider));
            receiver.setBackendProperties(properties);
            return receiver;
        }

        @Bean
        public DeliveryReceiver deliveryReceiver() {
            return new DeliveryReceiver();
        }

    }

    private static class MessageCreator extends BaseDeliveryReceiverMessageCreator {

        private final MessageHandlerFactory handlerFactory = new MessageHandlerFactory();

        public MessageCreator(final IEncryptionProvider encryptionProvider) {
            super(encryptionProvider);
        }

        @Override
        protected AppResponse doCreateResponse(final Object incomingPayload,
            final String conversationId,
            final Path workingDirectory) throws RejectedMessageException {
            return handlerFactory.createHandler(conversationId, incomingPayload).createRespone().orElseThrow();
        }

    }

}
