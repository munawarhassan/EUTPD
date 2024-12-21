package com.pmi.tpd.metrics.heath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Default {@link HealthAggregator} implementation that aggregates {@link Health} instances and determines the final
 * system state based on a simple ordered list.
 * <p>
 * If a different order is required or a new {@link Status} type will be used, the order can be set by calling
 * {@link #setStatusOrder(List)}.
 */
public class OrderedHealthAggregator extends AbstractHealthAggregator {

    /** */
    private List<String> statusOrder;

    /**
     * Create a new {@link OrderedHealthAggregator} instance.
     */
    public OrderedHealthAggregator() {
        setStatusOrder(Status.DOWN, Status.OUT_OF_SERVICE, Status.UP, Status.UNKNOWN);
    }

    /**
     * Set the ordering of the status.
     *
     * @param statusOrder
     *            an ordered list of the status
     */
    public void setStatusOrder(final Status... statusOrder) {
        final String[] order = new String[statusOrder.length];
        for (int i = 0; i < statusOrder.length; i++) {
            order[i] = statusOrder[i].getCode();
        }
        setStatusOrder(Arrays.asList(order));
    }

    /**
     * Set the ordering of the status.
     *
     * @param statusOrder
     *            an ordered list of the status codes
     */
    public void setStatusOrder(final List<String> statusOrder) {
        Assert.notNull(statusOrder, "StatusOrder must not be null");
        this.statusOrder = statusOrder;
    }

    @Override
    protected Status aggregateStatus(final List<Status> candidates) {
        // Only sort those status instances that we know about
        final List<Status> filteredCandidates = new ArrayList<Status>();
        for (final Status candidate : candidates) {
            if (this.statusOrder.contains(candidate.getCode())) {
                filteredCandidates.add(candidate);
            }
        }
        // If no status is given return UNKNOWN
        if (filteredCandidates.isEmpty()) {
            return Status.UNKNOWN;
        }
        // Sort given Status instances by configured order
        Collections.sort(filteredCandidates, new StatusComparator(this.statusOrder));
        return filteredCandidates.get(0);
    }

    /**
     * {@link Comparator} used to order {@link Status}.
     */
    private final class StatusComparator implements Comparator<Status> {

        /** */
        private final List<String> statusOrder;

        private StatusComparator(final List<String> statusOrder) {
            this.statusOrder = statusOrder;
        }

        @Override
        public int compare(final Status s1, final Status s2) {
            final int i1 = this.statusOrder.indexOf(s1.getCode());
            final int i2 = this.statusOrder.indexOf(s2.getCode());
            return i1 < i2 ? -1 : i1 == i2 ? s1.getCode().compareTo(s2.getCode()) : 1;
        }

    }

}
