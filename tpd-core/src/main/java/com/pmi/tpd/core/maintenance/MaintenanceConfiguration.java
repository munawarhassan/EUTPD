package com.pmi.tpd.core.maintenance;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.topic.ITopic;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.migration.DefaultMigrationService;
import com.pmi.tpd.core.migration.IMigrationService;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.migration.impl.MigrationTaskFactory;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.web.core.request.IRequestManager;

@Configuration
public class MaintenanceConfiguration {

    @Inject
    private HazelcastInstance hazelcastInstance;

    @Inject
    private IAuthenticationContext authenticationContext;

    @Inject
    private IExecutorService clusterExecutorService;

    @Inject
    private ILifecycleAwareSchedulerService schedulerService;

    @Inject
    private IEventAdvisorService<?> eventAdvisorService;

    @Inject
    private IClusterService clusterService;

    @Inject
    private IDatabaseManager databaseManager;

    @Inject
    private IEventPublisher eventPublisher;

    @Inject
    private ScheduledExecutorService executorService;

    @Inject
    private I18nService i18nService;

    @Inject
    private IRequestManager requestManager;

    @Inject
    private ISecureTokenGenerator tokenGenerator;

    /*********************************************************************************************
     * Maintenance Section.
     *********************************************************************************************/

    @Bean(name = "localMaintenanceTaskStatusSupplier")
    public IMaintenanceTaskStatusSupplier localMaintenanceTaskStatusSupplier() {
        return new LocalMaintenanceTaskStatusSupplier();
    }

    @Bean(name = "maintenanceTaskStatusSupplier")
    public IMaintenanceTaskStatusSupplier maintenanceTaskStatusSupplier(
        @Named("localMaintenanceTaskStatusSupplier") final IMaintenanceTaskStatusSupplier delegate) {
        final ITopic<IRunnableMaintenanceTaskStatus> topic = hazelcastInstance.getTopic("app.maintenance.latestTask");
        final ClusteredMaintenanceTaskStatusSupplier taskStatusSupplier = new ClusteredMaintenanceTaskStatusSupplier(
                delegate, topic);
        return taskStatusSupplier;
    }

    @Bean(name = "localMaintenanceModeHelper")
    public DefaultMaintenanceModeHelper localMaintenanceModeHelper() {
        return new DefaultMaintenanceModeHelper(eventPublisher, schedulerService);
    }

    @Bean
    public IMaintenanceModeHelper maintenanceModeHelper(
        @Named("localMaintenanceModeHelper") final DefaultMaintenanceModeHelper localMaintenanceModeHelper) {
        return new HazelcastMaintenanceModeHelper(hazelcastInstance.getSet("app.maintenance.events"),
                localMaintenanceModeHelper);
    }

    @Bean
    public IMaintenanceService maintenanceService(
        @Named("maintenanceTaskStatusSupplier") final IMaintenanceTaskStatusSupplier latestTask) {
        return new DefaultMaintenanceService(authenticationContext, clusterExecutorService, clusterService,
                databaseManager, eventPublisher, executorService, eventAdvisorService, i18nService, requestManager,
                tokenGenerator, latestTask,
                hazelcastInstance.getCPSubsystem().getAtomicLong("app.maintenance.isActive"),
                hazelcastInstance.getCPSubsystem()
                        .<ClusterMaintenanceLock> getAtomicReference("app.maintenance.clusterLock"));
    }

    @Bean
    public IMigrationTaskFactory migrationTaskFactory(final ApplicationContext applicationContext,
        @Nonnull final IDatabaseManager databaseManager) {
        return new MigrationTaskFactory(applicationContext, databaseManager);
    }

    @Bean
    public IMigrationService migrationService(final IDatabaseManager databaseManager,
        final IMaintenanceService maintenanceService,
        final IMigrationTaskFactory maintenanceTaskFactory) {
        final DefaultMigrationService service = new DefaultMigrationService(databaseManager, i18nService,
                maintenanceService, maintenanceTaskFactory);
        return service;
    }
}
