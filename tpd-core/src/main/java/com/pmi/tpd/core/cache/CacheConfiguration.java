package com.pmi.tpd.core.cache;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

import com.google.common.base.Strings;
import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.ClusterProperty;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;
import com.pmi.tpd.core.cluster.HazelcastConfig;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;

/**
 * <p>
 * CacheCacheConfig class.
 * </p>
 *
 * @author devacfr
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties({ CacheProperties.class })
@Import({ HazelcastConfig.class })
@DependsOn({ HazelcastConstants.HAZELCAST_INSTANCE_NAME })
public class CacheConfiguration {

    private static final String PROVIDER_TYPE_CLIENT = "client";

    private static final String PROVIDER_TYPE_MEMBER = "member";

    public static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";

    public static final String HAZELCAST_CACHE_MANAGER_BEAN_NAME = "hazelcastCacheManager";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfiguration.class);

    /** */
    private final CacheProperties cacheProperties;

    public CacheConfiguration(final CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Cache is initializing...");
    }

    /**
     * ehcacheManager.
     *
     * @return a {@link javax.cache.CacheManager} instance.
     */
    @Bean(name = HAZELCAST_CACHE_MANAGER_BEAN_NAME, destroyMethod = "close")
    public javax.cache.CacheManager internalCacheManager(final HazelcastInstance hazelcastInstance) {
        LOGGER.debug("Starting cache");
        if (!Strings.isNullOrEmpty(cacheProperties.getProvider())) {
            System.setProperty(Caching.JAVAX_CACHE_CACHING_PROVIDER, cacheProperties.getProvider());
        }
        URI uri = null;
        if (!Strings.isNullOrEmpty(cacheProperties.getUri())) {
            uri = URI.create(cacheProperties.getUri());
        }
        ClusterProperty.JCACHE_PROVIDER_TYPE.setSystemProperty(PROVIDER_TYPE_MEMBER);
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        // return new HazelcastServerCacheManager(new HazelcastServerCachingProvider(hazelcastInstance),
        // hazelcastInstance,
        // null, null, HazelcastCachingProvider.propertiesByInstanceItself(hazelcastInstance));

        final javax.cache.CacheManager cacheManager = cachingProvider
                .getCacheManager(uri, null, HazelcastCachingProvider.propertiesByInstanceItself(hazelcastInstance));
        return cacheManager;
    }

    /**
     * <p>
     * cacheManager.
     * </p>
     *
     * @return a {@link org.springframework.cache.CacheManager} object.
     */
    @Bean(CACHE_MANAGER_BEAN_NAME)
    public CacheManager cacheManager(
        @Named(HAZELCAST_CACHE_MANAGER_BEAN_NAME) final javax.cache.CacheManager cacheManager) {
        final JCacheCacheManager jcacheManager = new JCacheCacheManager();
        jcacheManager.setCacheManager(cacheManager);
        return jcacheManager;
    }

    /**
     * userCache.
     *
     * @return a {@link UserCache} instance.
     */
    @Bean
    public UserCache userCache(@Named(CACHE_MANAGER_BEAN_NAME) final CacheManager cacheManager) {
        final SpringCacheBasedUserCache userCache = new SpringCacheBasedUserCache(cacheManager.getCache("userCache"));
        return userCache;
        // return new SpringCacheBasedUserCache(new ConcurrentMapCache("user-cache"));
    }
}
