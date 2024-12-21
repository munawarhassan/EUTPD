package com.pmi.tpd.cluster.hazelcast;

/**
 * Internal application constants for Hazelcast data structures.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class HazelcastConstants {

    /**
     * The name of the member attribute for the node's VM ID, a unique ID that remains stable for the lifetime of the
     * JVM.
     **/
    public static final String ATT_NODE_VM_ID = "node.vm.id";

    /** The name of the member attribute for the node's name, a user configurable name that is set at start time. **/
    public static final String ATT_NODE_NAME = "node.name";

    /** Queue capacity for the Hazelcast IExecutorService. */
    public static final String EXECUTOR_QUEUE_SIZE = "executor.queue.size";

    /** */
    public static final int EXECUTOR_QUEUE_SIZE_DEFAULT = 0;

    /** Number of threads per node for the Hazelcast IExecutorService. */
    public static final String EXECUTOR_MAX_THREADS = "executor.max.threads";

    /** */
    public static final int EXECUTOR_MAX_THREADS_DEFAULT = 16;

    /** The name of the core IExecutorService. */
    public static final String EXECUTOR_CORE = "app.core";

    /** The instance name of the HazelcastInstance. This _must_ match with string bean declaration */
    public static final String HAZELCAST_INSTANCE_NAME = "hazelcast";

    /** The name of the IMap backing the HazelcastBucketedExecutors. */
    public static final String MAP_BUCKETED_EXECUTOR = "bucketed.executor.tasks";

    /** Map name for Quartz jobstore calendars. */
    public static final String MAP_QUARTZ_JOBSTORE_CALENDARS = "quartz.jobStore.calendars";

    /** Map name for Quartz jobstore jobs. */
    public static final String MAP_QUARTZ_JOBSTORE_JOBS = "quartz.jobStore.jobs";

    /** Map name for Quartz jobstore triggers. */
    public static final String MAP_QUARTZ_JOBSTORE_TRIGGERS = "quartz.jobStore.triggers";

    /** The number of seconds to wait for a graceful shutdown. */
    public static final String GRACEFUL_SHUTDOWN_TIMEOUT = "graceful.shutdown.max.wait";

    /** Enable/Disable hazelcast phone number check on startup. */
    public static final String PHONE_HOME_ENABLED = "phone.home.enabled";

    /** The number of seconds the cluster will wait for a node to produce a heartbeat before assuming it is dead. */
    public static final String NODE_HEARTBEAT_TIMEOUT = "max.no.heartbeat";

    /** The number of milliseconds hazelcast should wait before timing out a remote operation. */
    public static final String OPERATION_CALL_TIMEOUT = "operation.call.timeout";

    /** The number of threads hazelcast should use to deserialise responses for remote operations. */
    public static final String RESPONSE_THREAD_COUNT = "response.thread.count";

    /** The interval in minutes hazelcast should use to sync backup versions. */
    public static final String BACKUP_SYNC_INTERVAL = "backup.sync.interval";

    /** The level at which Hazelcast should print out health monitoring information. */
    public static final String HEALTH_MONITORING_LEVEL = "health.monitoring.level";

    /** How often (in seconds) the Hazelcast health monitor should print out to the log file. */
    public static final String HEALTH_MONITORING_DELAY = "health.monitoring.delay";

    /**
     * Update Hazelcast capabilities after all the plugins have been initialised in {@link #LIFECYCLE_PHASE_PLUGINS}.
     */
    public static final int LIFECYCLE_PHASE_HAZELCAST = 1500;

    /**
     * Serialization ID for {@code SimpleInstalledLicense}.
     */
    public static final int TYPE_LICENSE = 1000;

    /**
     * Serialization ID for {@link com.atlassian.fugue.Option Option}.
     */
    public static final int TYPE_OPTION = 1001;

    private HazelcastConstants() {
    }
}
