package com.pmi.tpd.scheduler.quartz;

import java.util.Map;
import java.util.Properties;

import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

import com.google.common.collect.ImmutableMap;

/**
 * Generates an initial {@code Properties} object that a {@code Quartz2ConfigurationSettings} can use as a starting
 * point for further configuration.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class QuartzDefaultSettingsFactory {

    private QuartzDefaultSettingsFactory() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a factory class and should not be instantiated");
    }

    /** */
    private static final ImmutableMap<String, String> DEFAULT_LOCAL_CONFIG = ImmutableMap.<String, String> builder()
            .put("org.quartz.jobStore.class", RAMJobStore.class.getName())
            .put("org.quartz.scheduler.instanceName", "scheduler-quartz2.local")
            .put("org.quartz.scheduler.skipUpdateCheck", "true")
            .put("org.quartz.threadPool.class", SimpleThreadPool.class.getName())
            .put("org.quartz.threadPool.threadCount", "4")
            .put("org.quartz.threadPool.threadPriority", "4")
            .build();

    /** */
    private static final ImmutableMap<String, String> DEFAULT_CLUSTERED_CONFIG = ImmutableMap.<String, String> builder()
            .put("org.quartz.jobStore.class", QuartzHardenedJobStore.class.getName())
            .put("org.quartz.jobStore.isClustered", "true")
            .put("org.quartz.scheduler.instanceName", "scheduler-quartz2.clustered")
            .put("org.quartz.scheduler.skipUpdateCheck", "true")
            .put("org.quartz.threadPool.class", SimpleThreadPool.class.getName())
            .put("org.quartz.threadPool.threadCount", "4")
            .put("org.quartz.threadPool.threadPriority", "4")
            .build();

    /**
     * @return
     */
    public static Properties getDefaultLocalSettings() {
        return toProperties(DEFAULT_LOCAL_CONFIG);
    }

    /**
     * @return
     */
    public static Properties getDefaultClusteredSettings() {
        return toProperties(DEFAULT_CLUSTERED_CONFIG);
    }

    private static Properties toProperties(final Map<String, String> defaultConfig) {
        final Properties config = new Properties();
        for (final Map.Entry<String, String> entry : defaultConfig.entrySet()) {
            config.setProperty(entry.getKey(), entry.getValue());
        }
        return config;
    }
}
