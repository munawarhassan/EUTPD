package com.pmi.tpd.core;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.env.Environment;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.DefaultTimeZoneHelper;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.context.ITimeZoneHelper;
import com.pmi.tpd.api.context.SystemClock;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.aop.ExceptionRewriteAdvice;
import com.pmi.tpd.core.audit.AuditConfiguration;
import com.pmi.tpd.core.context.ContextConfiguration;
import com.pmi.tpd.core.elasticsearch.ElasticSearchConfigurer;
import com.pmi.tpd.core.euceg.EucegConfiguration;
import com.pmi.tpd.core.event.publisher.spring.EventPublisherConfiguration;
import com.pmi.tpd.core.maintenance.MaintenanceConfiguration;
import com.pmi.tpd.core.server.DefaultApplicationStatusService;
import com.pmi.tpd.keystore.spring.KeyStoreConfigurer;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.spring.i18n.ClasspathI18nMessageSource;
import com.pmi.tpd.spring.i18n.MessageSourceI18nService;

/**
 * <p>
 * CoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@Import({ ClusterConfig.class, ElasticSearchConfigurer.class, JpaConfig.class, EventPublisherConfiguration.class,
        ContextConfiguration.class, MaintenanceConfiguration.class, SecurityConfig.class, AuditConfiguration.class,
        DaoCoreConfig.class, KeyStoreConfigurer.class, UserSecurityConfig.class, UpgradeConfig.class,
        EucegConfiguration.class })
@EnableAspectJAutoProxy
public class CoreConfig implements EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreConfig.class);

    /** */
    private RelaxedPropertyResolver serverProps;

    @PostConstruct
    public void initCore() {
        LOGGER.info("Core Configuration is initializing...");
    }

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.serverProps = new RelaxedPropertyResolver(environment, "server.");
    }

    /**
     * @return Returns a {@link MessageSourceI18nService} instance.
     * @throws IOException
     *                     in case of I/O errors.
     */
    @Bean
    public static MessageSourceI18nService i18nService() throws IOException {
        return new MessageSourceI18nService(new ClasspathI18nMessageSource(), ApplicationConstants.getDefaultLocale());
    }

    /**
     * @return
     */
    public static ConversionServiceFactoryBean conversionService() {
        return new ConversionServiceFactoryBean();
    }

    /**
     * @param i18nService
     * @return
     */
    @Bean
    public static ExceptionRewriteAdvice exceptionRewriteAdvice(final I18nService i18nService) {
        return new ExceptionRewriteAdvice(i18nService);
    }

    /**
     * @return
     */
    @Bean
    public static IClock systemClock() {
        return new SystemClock();
    }

    /**
     * @return
     */
    @Bean
    public static ITimeZoneHelper timeZoneHelper() {
        return new DefaultTimeZoneHelper();
    }

    /**
     * @param propertiesService
     * @param servletContext
     * @return
     */
    @Bean
    public DefaultApplicationStatusService applicationStatusService(final IApplicationProperties propertiesService,
        final IEventAdvisorService<?> eventAdvisorService) {
        final DefaultApplicationStatusService applicationStatusService = new DefaultApplicationStatusService(
                propertiesService, eventAdvisorService);

        applicationStatusService
                .setServerBusyMessageTimeout(serverProps.getProperty("ticket.rejected.within", Long.class, 5L));
        applicationStatusService.setServerBusyQueueTime(serverProps.getProperty("busy.queue.time", Long.class, 60L));
        return applicationStatusService;
    }

}
