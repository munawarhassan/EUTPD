package com.pmi.tpd.cluster.spring;

import static com.pmi.tpd.cluster.hazelcast.HazelcastConstants.HAZELCAST_INSTANCE_NAME;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableList;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.spi.merge.LatestUpdateMergePolicy;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.spring.context.SpringManagedContext;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.cluster.ClusterNodeNameResolver;
import com.pmi.tpd.cluster.hazelcast.ClusterJoinSocketInterceptor;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;
import com.pmi.tpd.cluster.util.PropertiesUtils;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;

/**
 * Programmatically assembles a Hazelcast {@code Config} object, allowing Hazelcast to be configured using
 * {@code app-config.properties} rather than Hazelcast's XML. The primary motivation here to is keep all configuration
 * in {@code app-config.properties}, but a secondary benefit is that we avoid Hazelcast's XML configuration parsing
 * which has proven buggy for Confluence.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastConfigFactoryBean implements FactoryBean<Config> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfigFactoryBean.class);

    /** */
    private final ApplicationContext applicationContext;

    /** */
    private final RelaxedPropertyResolver propertyResolver;

    private final Environment environment;

    /** */
    private boolean jmxEnabled;

    /** */
    private String clusterName;

    /** */
    private int networkPort;

    /** */
    private boolean networkAwsEnabled;

    /** */
    private boolean networkMulticastEnabled;

    /** */
    private boolean networkTcpIpEnabled;

    private boolean jetEnabled;

    @Inject
    public HazelcastConfigFactoryBean(@Nonnull final ApplicationContext applicationContext,
            @Nonnull final Environment environment) {
        this.applicationContext = Assert.checkNotNull(applicationContext, "applicationContext");
        this.environment = Assert.checkNotNull(environment, "environment");
        this.propertyResolver = new RelaxedPropertyResolver(environment, "hazelcast.");
        init();
    }

    protected void init() {
        jmxEnabled = this.environment.getProperty("jmx.enabled", Boolean.class, false);
        clusterName = this.propertyResolver.getProperty("cluster.name");
        networkPort = this.propertyResolver.getProperty("network.port", Integer.class, 5701);
        networkAwsEnabled = this.propertyResolver.getProperty("network.aws.enabled", Boolean.class, false);
        networkMulticastEnabled = this.propertyResolver.getProperty("network.multicast", Boolean.class, false);
        networkTcpIpEnabled = this.propertyResolver.getProperty("network.tcpip", Boolean.class, false);
        this.jetEnabled = this.propertyResolver.getProperty("jet.enabled", Boolean.class, true);
    }

    @Override
    public final Config getObject() throws Exception {
        final SpringManagedContext managedContext = new SpringManagedContext();
        managedContext.setApplicationContext(applicationContext);

        final Config config = new Config(HAZELCAST_INSTANCE_NAME);

        config.setManagedContext(managedContext);
        config.setClassLoader(getClass().getClassLoader());

        configureAttributes(config);
        configureCluster(config);
        configureJmx(config);
        configureNetwork(config.getNetworkConfig());
        configureSerialization(config.getSerializationConfig());
        configureCaches(config);
        configureExecutors(config);
        configureMaps(config);
        configureScheduler(config);

        doConfigure(config);

        return config;
    }

    public void doConfigure(final Config config) {
        // noop
    }

    private void configureJmx(final Config config) {
        config.setProperty("hazelcast.jmx", Boolean.toString(jmxEnabled));
        config.setProperty("hazelcast.jmx.detailed", Boolean.toString(jmxEnabled));
    }

    @Override
    public Class<?> getObjectType() {
        return Config.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    protected MapConfig boundedCache(final Class<?> entityClass) {
        return boundedCache(entityClass.getName());
    }

    protected MapConfig boundedCache(final Class<?> entityClass, final String field) {
        String name = entityClass.getName();
        if (StringUtils.isNotBlank(field)) {
            // Validate that the field exists
            name += "." + field;
            if (ReflectionUtils.findField(entityClass, field) == null) {
                throw new IllegalStateException(name + " field does not exist. Please update the cache configuration");
            }
        }
        return boundedCache(name);
    }

    private MapConfig boundedCache(final String name) {
        return boundedCache(name, true);
    }

    private MapConfig boundedCache(final String name, final boolean nearCache) {
        final int timeToIdleSeconds = propertyResolver.getProperty("cache." + name + ".tti", Integer.class, 0);
        final int timeToLiveSeconds = propertyResolver.getProperty("cache." + name + ".ttl", Integer.class, 0);

        return boundedCache(name,
            getMaxOrFail("cache." + name + ".max"),
            timeToLiveSeconds,
            timeToIdleSeconds,
            nearCache);
    }

    protected MapConfig boundedCache(final String name,
        int size,
        final int timeToLive,
        final int timeToIdle,
        final boolean nearCache) {
        // Normalize all sizes to ensure direct calls to this method still apply
        // workable maximums
        size = normalizeSize(size);

        final EvictionConfig evictionConfig = new EvictionConfig().setEvictionPolicy(EvictionPolicy.LFU)
                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                .setSize(size);

        final MapConfig config = new MapConfig(name).setBackupCount(0)
                .setMaxIdleSeconds(Math.max(0, timeToIdle))
                .setEvictionConfig(evictionConfig)
                .setTimeToLiveSeconds(Math.max(0, timeToLive));

        if (nearCache) {
            final NearCacheConfig cacheConfig = defaultNearCacheConfig();
            cacheConfig.getEvictionConfig().setEvictionPolicy(EvictionPolicy.LFU).setSize(size);
            config.setNearCacheConfig(cacheConfig.setMaxIdleSeconds(Math.max(0, timeToIdle)))
                    .setTimeToLiveSeconds(Math.max(0, timeToLive));
        }
        return config;
    }

    private void configureAttributes(final Config config) {
        final MemberAttributeConfig attributeConfig = new MemberAttributeConfig();
        // Set the node's ID attribute that remains stable while the node is running.
        // This is useful because
        // Member.uuid is reset when a node rejoins a cluster.
        attributeConfig.setAttribute(HazelcastConstants.ATT_NODE_VM_ID, UUID.randomUUID().toString());
        attributeConfig.setAttribute(HazelcastConstants.ATT_NODE_NAME, ClusterNodeNameResolver.getNodeName());
        config.setMemberAttributeConfig(attributeConfig);
    }

    private void configureCaches(final Config config) {

        // Hibernate caches
        config.addMapConfig(new MapConfig("org.hibernate.cache.spi.UpdateTimestampsCache").setBackupCount(0)
                .setMergePolicyConfig(new MergePolicyConfig().setPolicy(LatestUpdateMergePolicy.class.getName()))
                .setNearCacheConfig(defaultNearCacheConfig())
                .setPerEntryStatsEnabled(true));

    }

    private void configureExecutors(final Config config) {
        final int poolSize = PropertiesUtils.parseExpression(
            propertyResolver.getProperty(HazelcastConstants.EXECUTOR_MAX_THREADS),
            HazelcastConstants.EXECUTOR_MAX_THREADS_DEFAULT);
        final int queueCapacity = PropertiesUtils.parseExpression(
            propertyResolver.getProperty(HazelcastConstants.EXECUTOR_QUEUE_SIZE),
            HazelcastConstants.EXECUTOR_QUEUE_SIZE_DEFAULT);
        final ExecutorConfig executorConfig = new ExecutorConfig(HazelcastConstants.EXECUTOR_CORE, poolSize);
        executorConfig.setQueueCapacity(queueCapacity);
        config.addExecutorConfig(executorConfig);
    }

    private void configureCluster(final Config config) {
        config.setClusterName(clusterName);
        config.getJetConfig().setEnabled(this.jetEnabled);
        config.setProperty(ClusterProperty.GRACEFUL_SHUTDOWN_MAX_WAIT.getName(),
            propertyResolver.getProperty(HazelcastConstants.GRACEFUL_SHUTDOWN_TIMEOUT));
        config.setProperty(ClusterProperty.HEALTH_MONITORING_LEVEL.getName(),
            propertyResolver.getProperty(HazelcastConstants.HEALTH_MONITORING_LEVEL));
        config.setProperty(ClusterProperty.HEALTH_MONITORING_DELAY_SECONDS.getName(),
            propertyResolver.getProperty(HazelcastConstants.HEALTH_MONITORING_DELAY));
        config.setProperty(ClusterProperty.MAX_NO_HEARTBEAT_SECONDS.getName(),
            propertyResolver.getProperty(HazelcastConstants.NODE_HEARTBEAT_TIMEOUT));
        config.setProperty(ClusterProperty.OPERATION_CALL_TIMEOUT_MILLIS.getName(),
            propertyResolver.getProperty(HazelcastConstants.OPERATION_CALL_TIMEOUT));
        config.setProperty(ClusterProperty.PARTITION_BACKUP_SYNC_INTERVAL.getName(),
            Long.toString(TimeUnit.MINUTES.toSeconds(getIntOrFail(HazelcastConstants.BACKUP_SYNC_INTERVAL))));
        config.setProperty(ClusterProperty.PHONE_HOME_ENABLED.getName(),
            propertyResolver.getProperty(HazelcastConstants.PHONE_HOME_ENABLED));

    }

    private void configureMaps(final Config config) {
        config.addMapConfig(new MapConfig("default").setBackupCount(1));

        config.addMapConfig(new MapConfig(HttpSession.class.getName()).setAsyncBackupCount(1) // Async for speed, still
                                                                                              // using a backup to try
                                                                                              // to avoid losing
                                                                                              // sessions
                .setBackupCount(0)
                .setNearCacheConfig(defaultNearCacheConfig()));
    }

    private void configureNetwork(final NetworkConfig config) {
        if (networkMulticastEnabled && networkTcpIpEnabled) {
            // Enabling both Multicast and TCP/IP causes Hazelcast to go into an infinite
            // loop when trying to initialize
            // This was tested on Hazelcast v3.3-RC2
            LOGGER.warn(
                "Both TCP/IP and Multicast have been enabled for Hazelcast node discovery. Setting to default TCP/IP");
            networkMulticastEnabled = false;
        }

        final boolean clusteringEnabled = true;

        final JoinConfig joinConfig = config.getJoin();

        final AwsConfig awsConfig = joinConfig.getAwsConfig().setEnabled(clusteringEnabled && networkAwsEnabled);
        if (networkAwsEnabled) {
            final String awsAccessKey = propertyResolver.getProperty("network.aws.access.key");
            final String awsSecretKey = propertyResolver.getProperty("network.aws.secret.key");
            final String awsRegion = propertyResolver.getProperty("network.aws.region");
            final String awsHostHeader = propertyResolver.getProperty("network.aws.host.header");
            final String awsSecurityGroupName = propertyResolver.getProperty("network.aws.security.group.name");
            final String awsTagKey = propertyResolver.getProperty("network.aws.tag.key");
            final String awsTagValue = propertyResolver.getProperty("network.aws.tag.value");
            final String awsConnectionTimeoutSecondsStr = propertyResolver
                    .getProperty("network.aws.connection.timeout.seconds", "-1");
            int awsConnectionTimeoutSeconds = -1;
            try {
                awsConnectionTimeoutSeconds = Integer.parseInt(awsConnectionTimeoutSecondsStr);
            } catch (final NumberFormatException e) {
                LOGGER.warn(
                    "Can't parse property \"hazelcast.network.aws.connection.timeout.seconds\" value \"{}\", ignoring",
                    awsConnectionTimeoutSecondsStr);
            }
            awsConfig.getProperties().put("access-key", awsAccessKey);
            awsConfig.getProperties().put("secret-key", awsSecretKey);
            if (StringUtils.isNotEmpty(awsRegion)) {
                awsConfig.getProperties().put("region", awsRegion);
            }
            if (StringUtils.isNotEmpty(awsHostHeader)) {
                awsConfig.getProperties().put("host-header", awsHostHeader);
            }
            if (StringUtils.isNotEmpty(awsSecurityGroupName)) {
                awsConfig.getProperties().put("security-group-name", awsSecurityGroupName);
            }
            if (StringUtils.isNotEmpty(awsTagKey)) {
                awsConfig.getProperties().put("tag-key", awsTagKey);
                awsConfig.getProperties().put("tag-value", awsTagValue);
            }
            if (awsConnectionTimeoutSeconds >= 0) {
                awsConfig.getProperties().put("connection-timeout-seconds", String.valueOf(clusteringEnabled));
            }
        }

        final MulticastConfig multicastConfig = joinConfig.getMulticastConfig();
        multicastConfig.setEnabled(clusteringEnabled && networkMulticastEnabled);
        multicastConfig.setMulticastPort(networkPort);

        final TcpIpConfig tcpIp = joinConfig.getTcpIpConfig().setEnabled(clusteringEnabled && networkTcpIpEnabled);
        // Check whether the optional tcpip.members has been configured
        final String tcpIpMembers = propertyResolver.getProperty("network.tcpip.members");
        if (StringUtils.isNotBlank(tcpIpMembers)) {
            tcpIp.addMember(tcpIpMembers); // addMember takes a comma separated list
        }

        config.setPort(networkPort);

        if (clusteringEnabled) {
            final SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
            interceptorConfig.setClassName(ClusterJoinSocketInterceptor.class.getName());
            interceptorConfig.setEnabled(true);
            config.setSocketInterceptorConfig(interceptorConfig);
        }
    }

    private void configureScheduler(final Config config) {
        config.addMapConfig(new MapConfig(HazelcastConstants.MAP_QUARTZ_JOBSTORE_CALENDARS));
        config.addMapConfig(new MapConfig(HazelcastConstants.MAP_QUARTZ_JOBSTORE_JOBS));
        config.addMapConfig(new MapConfig(HazelcastConstants.MAP_QUARTZ_JOBSTORE_TRIGGERS)
                .setIndexConfigs(ImmutableList.of(new IndexConfig().setName("jobGroup").addAttribute("job"),
                    new IndexConfig().setName("jobName").addAttribute("job"),
                    new IndexConfig().setName("nextFireTime").addAttribute("calendarName"),
                    new IndexConfig().setName("state").addAttribute("state"))));
    }

    private void configureSerialization(final SerializationConfig config) {

        // ßializerConfig().setImplementation(new OptionalStreamSerializer()).setTypeClass(Optional.class));
    }

    protected NearCacheConfig defaultNearCacheConfig() {
        return new NearCacheConfig().setInMemoryFormat(InMemoryFormat.OBJECT) // Always near-cache in Object mode to
                                                                              // avoid deserialization costs
                .setCacheLocalEntries(true); // Near-cache on the local node as well to avoid serialization costs
    }

    private int getIntOrFail(final String propertyName) {
        final String maxSize = propertyResolver.getProperty(propertyName);
        Assert.isTrue(!StringUtils.isBlank(maxSize), "Property '" + propertyName + "' is undefined");

        return Integer.parseInt(maxSize);
    }

    /**
     * Retrieves the configured maximum value and {@link #normalizeSize(int) normalizes values}.
     *
     * @param propertyName
     *                     the {@code .max} property to retrieve a value for
     * @return the configured maximum, safely normalized to at least 500
     */
    protected int getMaxOrFail(final String propertyName) {
        final int configured = getIntOrFail(propertyName);
        final int normalized = normalizeSize(configured);
        if (configured != 0 && configured != normalized) {
            // If the normalized value doesn't match the configured value log a warning
            // about the invalid max size
            LOGGER.warn("Ignoring \"{}={}\"; values less than 500, except 0, are not valid", propertyName, configured);
        }
        return normalized;
    }

    /**
     * Normalizes size values to ensure they will work as expected when applied to a Hazelcast {@code IMap}. The rules
     * applied are:
     * <ul>
     * <li>Values less than 0 are normalized to Integer.MAX_VALUE (No limit)</li>
     * <li>0 values are returned unchanged</li>
     * <li>Values from 1 to 499 are normalized to 500</li>
     * <li>Values of 500 or higher are returned unchanged</li>
     * </ul>
     * <p>
     * By default Hazelcast uses 271 partitions on each node to store data, so max size values are divided by 271 to
     * determine the per-partition limit. That means if the max size is less than 271, the per-partition limit is 0 and
     * all {@code IMap} data is <i>continuously evicted</i>.
     *
     * @param size
     *             the size to normalize
     * @return the normalized size
     */
    protected int normalizeSize(final int size) {
        if (size <= 0) {
            // 0 or smaller means unlimited
            return Integer.MAX_VALUE;
        }

        return Math.max(size, 500);
    }
}
