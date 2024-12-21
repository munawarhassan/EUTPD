package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;

import com.hazelcast.query.Predicate;

/**
 * @since 1.1
 */
public class GroupMatcherPredicate<T extends Key<?>, V> implements Predicate<T, V> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final GroupMatcher<T> matcher;

    public GroupMatcherPredicate(final GroupMatcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean apply(final Map.Entry<T, V> entry) {
        return matcher.isMatch(entry.getKey());
    }

}
