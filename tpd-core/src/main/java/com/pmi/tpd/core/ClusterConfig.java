package com.pmi.tpd.core;

import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.hazelcast.core.HazelcastInstance;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.tenant.BareTenantAccessor;
import com.pmi.tpd.api.tenant.ITenantAccessor;
import com.pmi.tpd.cluster.HazelcastClusterService;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.cluster.concurrent.IClusterLockService;
import com.pmi.tpd.cluster.hazelcast.HazelcastClusterLockService;
import com.pmi.tpd.core.bootstrap.DefaultLockService;
import com.pmi.tpd.core.cluster.DefaultClusterJoinManager;
import com.pmi.tpd.core.cluster.HazelcastConfig;
import com.pmi.tpd.core.cluster.check.SharedHomeAndDatabaseJoinCheck;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

/**
 * @author Christophe Friederich
 */
@Configuration
@Import({ HazelcastConfig.class })
public class ClusterConfig {

    /**
     * @param hazelcastInstance
     * @return
     */
    @Bean
    public HazelcastClusterService clusterService(final HazelcastInstance hazelcastInstance) {
        return new HazelcastClusterService(hazelcastInstance);
    }

    /**
     * @param applicationSettings
     * @param configurationService
     * @param dataSourceConfiguration
     * @param secureTokenGenerator
     * @return
     */
    @Bean
    public SharedHomeAndDatabaseJoinCheck sharedHomeAndDatabaseJoinCheck(
        final IApplicationConfiguration applicationSettings,
        final IDatabaseConfigurationService configurationService,
        final IDataSourceConfiguration dataSourceConfiguration,
        final ISecureTokenGenerator secureTokenGenerator) {
        return new SharedHomeAndDatabaseJoinCheck(applicationSettings, configurationService, dataSourceConfiguration,
                secureTokenGenerator);
    }

    @Bean
    public DefaultClusterJoinManager clusterJoinManager(final IClock clock,
        final IEventAdvisorService<?> eventAdvisorService,
        final IClusterJoinCheck... joinChecks) {

        return new DefaultClusterJoinManager(clock, eventAdvisorService, joinChecks);
    }

    @Bean
    public HazelcastClusterLockService hazelcastClusterLockService(final HazelcastInstance hazelcast) {
        return new HazelcastClusterLockService(hazelcast);
    }

    @Bean
    public DefaultLockService defaultLockService(final IClusterLockService clusterLockService,
        @Named("dataSource") final DataSource dataSource,
        final I18nService i18nService) {
        return new DefaultLockService(clusterLockService, dataSource, i18nService);
    }

    @Bean
    public ITenantAccessor tenantAccessor() {
        return new BareTenantAccessor();
    }

}
