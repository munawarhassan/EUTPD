package com.pmi.tpd.scheduler.quartz.hazelcast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;

import com.google.common.collect.Maps;

/**
 * @since 1.1
 */
public class JobConfig implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Map<String, Serializable> data;

    private final boolean durable;

    private final String jobClassName;

    private final boolean recoveryRequested;

    private JobConfig(final Builder builder) {
        this.data = builder.data;
        this.durable = builder.durable;
        this.jobClassName = checkNotNull(builder.jobClassName, "jobClassName");
        this.recoveryRequested = builder.recoveryRequested;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JobDetail toJobDetail(final JobKey key) throws JobPersistenceException {
        Class<? extends Job> jobClass;
        try {
            // noinspection unchecked
            jobClass = (Class) Class.forName(jobClassName);
        } catch (final ClassNotFoundException e) {
            throw new JobPersistenceException("Unable to load job class", e);
        }

        final JobBuilder builder = JobBuilder.newJob(jobClass)
                .withIdentity(key)
                .requestRecovery(recoveryRequested)
                .storeDurably(durable);

        if (data != null) {
            builder.usingJobData(new JobDataMap(data));
        }

        return builder.build();
    }

    public static class Builder {

        private Map<String, Serializable> data;

        private boolean durable;

        private String jobClassName;

        private boolean recoveryRequested;

        public Builder() {
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Builder(final JobDetail jobDetail) {
            this();

            data((Map) jobDetail.getJobDataMap());
            durability(jobDetail.isDurable());
            jobClassName(jobDetail.getJobClass().getName());
            recoveryRequested(jobDetail.requestsRecovery());
        }

        public JobConfig build() {
            return new JobConfig(this);
        }

        public Builder data(final Map<String, Serializable> value) {
            // We are using a hash map here to preserve any null keys or values
            data = value == null || value.isEmpty() ? null : Maps.newHashMap(value);

            return this;
        }

        public Builder durability(final boolean value) {
            durable = value;

            return this;
        }

        public Builder jobClassName(final String value) {
            jobClassName = value;

            return this;
        }

        public Builder recoveryRequested(final boolean value) {
            recoveryRequested = value;

            return this;
        }

    }

}
