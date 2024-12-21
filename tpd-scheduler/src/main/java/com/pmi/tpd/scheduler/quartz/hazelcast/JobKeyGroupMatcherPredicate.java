package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import com.hazelcast.query.Predicate;

/**
 * @since 1.1
 */
public class JobKeyGroupMatcherPredicate implements Predicate<TriggerKey, AbstractTriggerConfig> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final GroupMatcher<JobKey> matcher;

    public JobKeyGroupMatcherPredicate(final GroupMatcher<JobKey> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean apply(final Map.Entry<TriggerKey, AbstractTriggerConfig> entry) {
        final AbstractTriggerConfig trigger = entry.getValue();
        return trigger != null && matcher.isMatch(trigger.getJob());
    }

}
