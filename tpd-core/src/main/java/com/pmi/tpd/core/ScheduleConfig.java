package com.pmi.tpd.core;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.quartz.Scheduler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.context.ServletContextAware;

import com.pmi.tpd.api.context.ITimeZoneHelper;
import com.pmi.tpd.api.scheduler.CountingJobRunner;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.tenant.ITenantAccessor;
import com.pmi.tpd.cluster.concurrent.ConfigurableThreadFactory;
import com.pmi.tpd.cluster.concurrent.ExecutorServiceConfiguration;
import com.pmi.tpd.cluster.concurrent.StateTransferringScheduledExecutorService;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;
import com.pmi.tpd.scheduler.quartz.QuartzHazelcastJobStore;
import com.pmi.tpd.scheduler.quartz.QuartzSchedulerService;
import com.pmi.tpd.scheduler.spring.ScheduledJobLifecycle;
import com.pmi.tpd.scheduler.spring.SchedulerLifecycle;
import com.pmi.tpd.scheduler.spring.SchedulerServiceConfiguration;
import com.pmi.tpd.scheduler.support.MemoryRunDetailsDao;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;

@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig implements EnvironmentAware, ServletContextAware, SchedulingConfigurer, AsyncConfigurer {

    /** */
    private Environment environment;

    /** */
    private RelaxedPropertyResolver schedulerProps;

    /** */
    private ServletContext servletContext;

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        this.schedulerProps = new RelaxedPropertyResolver(environment, "scheduler.");
    }

    /** {@inheritDoc} */
    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ScheduledExecutorService configurableScheduledExecutorService() {
        final ExecutorServiceConfiguration configuration = new ExecutorServiceConfiguration(
                environment.getProperty("executor.max.threads", "cpu"));
        final ThreadGroup threadGroup = new ThreadGroup(HazelcastConstants.EXECUTOR_CORE);
        threadGroup.setDaemon(true);
        final ConfigurableThreadFactory configurableThreadFactory = new ConfigurableThreadFactory();
        configurableThreadFactory.setClassLoader(servletContext.getClassLoader());
        configurableThreadFactory.setDaemon(true);
        configurableThreadFactory.setThreadGroup(threadGroup);
        configurableThreadFactory.setThreadNamePrefix("threadcore");
        return new ScheduledThreadPoolExecutor(configuration.getCorePoolSize());
    }

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService scheduledExecutorService() {
        return new StateTransferringScheduledExecutorService(configurableScheduledExecutorService());
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduledExecutorService());
    }

    @Bean(name = "localScheduler", destroyMethod = "")
    public SchedulerFactoryBean localScheduler() {
        final SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setSchedulerName("localScheduler");
        final Properties quartzProperties = new Properties();

        quartzProperties.setProperty("org.quartz.jobStore.misfireThreshold",
            schedulerProps.getProperty("misfire.threshold", "3600000"));
        scheduler.setQuartzProperties(quartzProperties);
        scheduler.setAutoStartup(false);
        return scheduler;
    }

    @Bean(name = "clusteredScheduler", destroyMethod = "")
    public SchedulerFactoryBean clusteredScheduler() {
        final SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setSchedulerName("clusterScheduler");
        final Properties quartzProperties = new Properties();

        quartzProperties.setProperty("org.quartz.jobStore.misfireThreshold",
            schedulerProps.getProperty("misfire.threshold", "3600000"));
        quartzProperties.setProperty("org.quartz.jobStore.class", QuartzHazelcastJobStore.class.getName());
        scheduler.setQuartzProperties(quartzProperties);
        scheduler.setAutoStartup(false);

        return scheduler;
    }

    @Bean
    public QuartzSchedulerService schedulerService(final ITimeZoneHelper timeZone,
        @Named("localScheduler") final Scheduler localScheduler,
        @Named("clusteredScheduler") final Scheduler clusteredScheduler,
        final ITenantAccessor tenantAccessor) throws SchedulerServiceException {
        return new QuartzSchedulerService(new MemoryRunDetailsDao(), new SchedulerServiceConfiguration(timeZone),
                localScheduler, clusteredScheduler, tenantAccessor);
    }

    @Override
    public Executor getAsyncExecutor() {
        return scheduledExecutorService();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    @Bean
    public ScheduledJobLifecycle scheduledJobLifecycle(final ISchedulerService schedulerService,
        final List<IScheduledJobSource> sources) {
        return new ScheduledJobLifecycle(schedulerService, sources);
    }

    @Bean
    public SchedulerLifecycle schedulerLifecycle(final ILifecycleAwareSchedulerService schedulerService) {
        return new SchedulerLifecycle(schedulerService,
                schedulerProps.getProperty("shutdown.timeout", Integer.class, 15));
    }

    /**
     * @param schedulerService
     * @return
     * @since 2.2
     */
    @Bean
    public CountingJobRunner countingJobRunner(@Nonnull final ISchedulerService schedulerService) {
        return new CountingJobRunner(schedulerService);
    }
}
