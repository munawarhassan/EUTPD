package com.pmi.tpd;

import java.io.Closeable;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.PickledGraphite;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jcache.JCacheGaugeSet;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.pmi.tpd.api.util.ByteConverter;
import com.pmi.tpd.core.mail.IMailService;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.metrics.gauge.BoneCpGaugeSet;
import com.pmi.tpd.metrics.gauge.OperatingSystemGaugeSet;
import com.pmi.tpd.metrics.heath.DatabaseHealthIndicator;
import com.pmi.tpd.metrics.heath.DiskSpaceHealthIndicator;
import com.pmi.tpd.metrics.heath.DomibusBackendHealthIndicator;
import com.pmi.tpd.metrics.heath.HealthAggregator;
import com.pmi.tpd.metrics.heath.HealthIndicator;
import com.pmi.tpd.metrics.heath.JavaMailHealthIndicator;
import com.pmi.tpd.metrics.heath.OrderedHealthAggregator;
import com.pmi.tpd.metrics.heath.ThreadDeadlockHealthCheck;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.web.core.rs.endpoint.RestEndpoints;
import com.pmi.tpd.web.rest.endpoint.metrics.HealthEndpoint;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

import fr.ippon.spark.metrics.SparkReporter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfig extends MetricsConfigurerAdapter implements EnvironmentAware {

    /** */
    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";

    /** */
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";

    /** */
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";

    /** */
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";

    /** */
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";

    /** */
    private static final String PROP_METRIC_REG_OS = "os";

    /** */
    private static final String PROP_METRIC_REG_DATASOURCE = "datasource";

    private static final String PROP_METRIC_REG_CACHE = "cache";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConfig.class);

    /** */
    private InstrumentedAppender logMetrics;

    /** */
    private PropertyResolver propertyResolver;

    @Override
    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment, "metrics.");
    }

    @Override
    @Bean
    public HealthCheckRegistry getHealthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Registering JVM gauges");
        final MetricRegistry metricRegistry = getMetricRegistry();
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        metricRegistry.register(PROP_METRIC_REG_OS, new OperatingSystemGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_CACHE, new JCacheGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS,
            new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register(PROP_METRIC_REG_DATASOURCE, new BoneCpGaugeSet());
        final boolean jmxEnable = propertyResolver.getProperty("jmx.enabled", Boolean.class, false);
        if (jmxEnable) {
            LOGGER.info("Initializing Metrics JMX reporting");
            final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
            this.registerReporter(jmxReporter);
            jmxReporter.start();
        }
        configureReporters(getMetricRegistry());

    }

    @Override
    public void destroy() throws Exception {
        try {
            if (logMetrics != null) {
                logMetrics.stop();
            }
        } catch (final Exception e) {

        }
        super.destroy();
    }

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        initLockbackInstrument(metricRegistry);
    }

    private void initLockbackInstrument(final MetricRegistry metricRegistry) {
        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME);

        logMetrics = new InstrumentedAppender(metricRegistry);
        logMetrics.setContext(rootLogger.getLoggerContext());
        logMetrics.start();
        rootLogger.addAppender(logMetrics);
    }

    /**
     * @author Christophe Friederich
     */
    @Configuration
    public static class HealthRegistry implements EnvironmentAware {

        /** */
        @Inject
        @Named("dataSource")
        private DataSource dataSource;

        /** */
        @Autowired(required = false)
        private IMailService mailService;

        /** */
        @Autowired(required = false)
        private IBackendManager backendManager;

        /** the Path used to compute the available disk space. */
        @Value("${app.home}")
        private File homePath;

        /** */
        private final RestEndpoints restEndpoints = new RestEndpoints();

        /** */
        private PropertyResolver propertyResolver;

        /** {@inheritDoc} */
        @Override
        public void setEnvironment(final Environment environment) {
            this.propertyResolver = new RelaxedPropertyResolver(environment, "metrics.");
        }

        /**
         * @return
         */
        @Bean
        public RestEndpoints restEndpoints() {
            return restEndpoints;
        }

        /**
         *
         */
        @PostConstruct
        public void init() {
            final HealthAggregator healthAggregator = new OrderedHealthAggregator();
            final Builder<String, HealthIndicator> healthIndicators = ImmutableMap.<String, HealthIndicator> builder()
                    .put("database", new DatabaseHealthIndicator(dataSource))
                    .put("threadlock", new ThreadDeadlockHealthCheck())
                    .put("diskspace",
                        new DiskSpaceHealthIndicator(homePath,
                                ByteConverter.toByte(propertyResolver.getProperty("thresholdDiskspace", "100 MiB"))));

            if (mailService != null) {
                healthIndicators.put("smtp", new JavaMailHealthIndicator(mailService));
            }
            if (backendManager != null) {
                healthIndicators.put("domibus.ws", new DomibusBackendHealthIndicator(backendManager));
            }

            final HealthEndpoint healthEndpoint = new HealthEndpoint(healthAggregator, healthIndicators.build());

            restEndpoints.register(healthEndpoint);
        }
    }

    /**
     * @author Christophe Friederich
     */
    @Configuration
    public static class GraphiteRegistry {

        public enum GraphiteSendType {
            PlainText,
            Pickle,
            // not implemented
            Amqp
        }

        /** */
        private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteRegistry.class);

        /** */
        @Inject
        private MetricRegistry metricRegistry;

        /** */
        @Value("${metrics.graphite.enabled}")
        private boolean graphiteEnabled;

        /** */
        @Value("${metrics.graphite.host}")
        private String graphiteHost;

        /** */
        @Value("${metrics.graphite.port}")
        private Integer graphitePort;

        /** */
        @Value("${metrics.graphite.prefix}")
        private String graphitePrefix;

        /** */
        @Value("${metrics.graphite.polling:60}")
        private int pollingInterval;

        @Value("${metrics.graphite.type:PlainText}")
        private GraphiteSendType graphiteSendType;

        /** */
        private Closeable graphiteReporter;

        @PostConstruct
        private void init() {
            if (graphiteEnabled) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Initializing Metrics Graphite reporting with host: {}, port: {}, sender type: {}",
                        graphiteHost,
                        graphitePort,
                        graphiteSendType);
                }
                GraphiteSender sender;
                switch (graphiteSendType) {
                    case PlainText:
                        sender = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
                        break;
                    case Pickle:
                    default:
                        sender = new PickledGraphite(new InetSocketAddress(graphiteHost, graphitePort));
                        break;

                }
                final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .prefixedWith(graphitePrefix)
                        .build(sender);
                reporter.start(pollingInterval, TimeUnit.SECONDS);
                graphiteReporter = reporter;
            }
        }

        /**
         * @throws Exception
         */
        @PreDestroy
        public void destroy() throws Exception {
            if (graphiteReporter != null) {
                graphiteReporter.close();
            }
        }
    }

    /**
     * @author Christophe Friederich
     */
    @Configuration
    public static class SparkRegistry {

        /** */
        private final Logger log = LoggerFactory.getLogger(SparkRegistry.class);

        /** */
        @Inject
        private MetricRegistry metricRegistry;

        /** */
        @Value("${metrics.spark.enabled}")
        private boolean sparkEnabled;

        /** */
        @Value("${metrics.spark.host}")
        private String sparkHost;

        /** */
        @Value("${metrics.spark.port}")
        private int sparkPort;

        /** */
        private Closeable sparkReporter;

        @PostConstruct
        private void init() {
            if (sparkEnabled) {
                log.info("Initializing Metrics Spark reporting");

                final SparkReporter reporter = SparkReporter.forRegistry(metricRegistry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build(sparkHost, sparkPort);
                reporter.start(1, TimeUnit.MINUTES);
                sparkReporter = reporter;
            }
        }

        @PreDestroy
        public void destroy() throws Exception {
            if (sparkReporter != null) {
                sparkReporter.close();
            }
        }
    }
}
