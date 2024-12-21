package com.pmi.tpd.core.cluster;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;
import com.pmi.tpd.cluster.hazelcast.HazelcastLifecycle;
import com.pmi.tpd.cluster.hazelcast.OptimisticOutOfMemoryHandler;
import com.pmi.tpd.cluster.spring.HazelcastConfigFactoryBean;
import com.pmi.tpd.core.cluster.spring.HazelcastFactoryBean;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;

/**
 * <p>
 * HazelcastCacheConfig class.
 * </p>
 *
 * @author devacfr
 */
@Configuration
public class HazelcastConfig implements EnvironmentAware {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfig.class);

    /** */
    private RelaxedPropertyResolver propertyResolver;

    @PostConstruct
    public void init() {
        System.setProperty("hazelcast.logging.class", com.hazelcast.logging.Slf4jFactory.class.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment, "hazelcast.");
    }

    /**
     * <p>
     * destroy.
     * </p>
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }

    @Bean(name = HazelcastConstants.HAZELCAST_INSTANCE_NAME)
    public HazelcastConfigFactoryBean hazelcastConfig(final ApplicationContext applicationContext,
        final Environment env) {
        return new HazelcastConfigFactoryBean(applicationContext, env) {

            @Override
            public void doConfigure(final Config config) {
                // default cache configuration
                config.addCacheConfig(new CacheSimpleConfig("*").setBackupCount(0)
                        .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU)
                                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                                .setSize(100)));

                config.addMapConfig(boundedCache("*", 50, 10, 10, true));
                config.addMapConfig(boundedCache(GrantedPermission.class.getName(), 0, 0, 0, true));

                // Permission caches
                // Note: The defaultPermissions cache is unbounded, but the system will never create more than a single
                // entry. See normalizeSize's documentation for an explanation of the unbounded size.
                config.addMapConfig(boundedCache("cache.permissionGraph.defaultPermissions", 0, 0, 0, true));
                config.addMapConfig(boundedCache("cache.permissionGraph.groupPermissions",
                    getMaxOrFail("cache.permissions.groups.max"),
                    0,
                    0,
                    true));
                config.addMapConfig(boundedCache("cache.permissionGraph.userPermissions",
                    getMaxOrFail("cache.permissions.users.max"),
                    0,
                    0,
                    true));

            }

        };
    }

    @Bean
    public HazelcastFactoryBean hazelcastInstance(final Config config,
        final IEventAdvisorService<?> eventAdvisorService) {
        final HazelcastFactoryBean hazelcast = new HazelcastFactoryBean(config, eventAdvisorService);
        hazelcast.setOutOfMemoryHandler(new OptimisticOutOfMemoryHandler());
        return hazelcast;
    }

    // @Bean
    // public HazelcastRegionFactoryBean hazelcastRegion(final HazelcastInstance hazelcastInstance) {
    // final String cacheMode = propertyResolver.getProperty("hibernate.cache.mode", "LOCAL");
    // return new HazelcastRegionFactoryBean(hazelcastInstance, cacheMode);
    // }

    @Bean
    public HazelcastLifecycle hazelcastLifecycle(final HazelcastInstance hazelcast) {
        final int shutdownTimeout = propertyResolver.getProperty("partition.drain.timeout", Integer.class, 30);
        return new HazelcastLifecycle(hazelcast, shutdownTimeout);
    }

    @Bean
    public Cluster cluster(final HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getCluster();
    }

    @Bean
    public IExecutorService executorService(final HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getExecutorService(HazelcastConstants.EXECUTOR_CORE);
    }

}
