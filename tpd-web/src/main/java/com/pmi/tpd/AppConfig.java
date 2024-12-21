package com.pmi.tpd;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.pmi.tpd.WebAppInitializer.WebConfig;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.IUncheckedOperation;
import com.pmi.tpd.core.CoreConfig;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.core.ScheduleConfig;
import com.pmi.tpd.core.avatar.AvatarConfiguration;
import com.pmi.tpd.core.bootstrap.BootstrapOperation;
import com.pmi.tpd.core.bootstrap.Bootstrapper;
import com.pmi.tpd.core.bootstrap.IBootstrapLockService;
import com.pmi.tpd.core.mail.MailConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@Import({ CoreConfig.class, InitializeConfig.class, JacksonConfig.class, ScheduleConfig.class, WebSecurityConfig.class,
        MailConfiguration.class, StartupConfig.class, MetricsConfig.class, WebConfig.class, WebsocketConfig.class,
        AvatarConfiguration.class })
public class AppConfig {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    /** */
    @Inject
    private Environment environment;

    /** log the current active profiles. */
    @PostConstruct
    public void initApp() {
        LOGGER.debug("Looking for Spring profiles...");
        if (environment.getActiveProfiles().length == 0) {
            LOGGER.info("No Spring profile configured, running with default configuration.");
        } else {
            for (final String profile : environment.getActiveProfiles()) {
                LOGGER.info("Application Starting with detected Spring profile: {}", profile);
            }
        }
    }

    @Bean
    BootstrapOperation bootstrapOperation() {
        return new BootstrapOperation();
    }

    @Bean
    public Bootstrapper bootstrapper(final IApplicationProperties propertiesService,
        final IUncheckedOperation<?> bootstrapOperation,
        final IBootstrapLockService lockService) {
        return new Bootstrapper(propertiesService, bootstrapOperation, lockService);
    }

    /**
     * @param eventPublisher
     * @param pluginSystemLifecycle
     * @return
     */
    @Bean
    public ComponentManager componentManager(@Nonnull final IEventPublisher eventPublisher) {
        return new ComponentManager(eventPublisher);
    }

    /**
     * @param applicationConfiguration
     * @return
     */
    @Bean
    public IGlobalApplicationProperties globalApplicationProperties(
        final IApplicationConfiguration applicationConfiguration) {
        return new GlobalApplicationProperties(applicationConfiguration);
    }

}
