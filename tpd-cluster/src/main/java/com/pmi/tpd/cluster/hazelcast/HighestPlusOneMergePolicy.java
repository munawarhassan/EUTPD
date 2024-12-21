package com.pmi.tpd.cluster.hazelcast;

import static com.pmi.tpd.cluster.concurrent.HazelcastVersionTracker.inc;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.merge.LatestUpdateMergePolicy;
import com.hazelcast.spi.merge.MergingLastUpdateTime;
import com.hazelcast.spi.merge.MergingValue;
import com.pmi.tpd.cluster.concurrent.HazelcastVersionTracker;

/**
 * A merge policy designed to be used with the {@link HazelcastVersionTracker} where the version is used to passively
 * notify cluster-wide mutations to state.
 * <p>
 * If both values are integers the strategy will choose a value that is different to either value. This ensures that
 * both halves of the previously split brain are aware that state has changed and need to resync their local copy of
 * this state.
 * <p>
 * If either value is null it is assumed to be 0. If either entry is not an integer it will choose the most recently
 * updated value.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HighestPlusOneMergePolicy<T extends MergingValue<Object> & MergingLastUpdateTime>
        extends LatestUpdateMergePolicy<Object, T> {

    @Override
    public Object merge(final T mergingEntry, final T existingEntry) {
        final Object mergingValue = mergingEntry.getValue();
        final Object existingValue = existingEntry.getValue();

        if (mergingValue != null && !(mergingValue instanceof Integer)
                || existingValue != null && !(existingValue instanceof Integer)) {
            return super.merge(mergingEntry, existingEntry);
        }

        final Integer mergingIntValue = inc(mergingValue == null ? 0 : (Integer) mergingValue);
        final Integer existingIntValue = inc(existingValue == null ? 0 : (Integer) existingValue);

        return Math.max(mergingIntValue, existingIntValue);
    }

    @Override
    public void writeData(final ObjectDataOutput out) {
    }

    @Override
    public void readData(final ObjectDataInput in) {
    }
}
