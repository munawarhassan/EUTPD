package com.pmi.tpd.metrics.heath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base {@link HealthAggregator} implementation to allow subclasses to focus on aggregating the {@link Status} instances
 * and not deal with contextual details etc.
 */
public abstract class AbstractHealthAggregator implements HealthAggregator {

    @Override
    public final Health aggregate(final Map<String, Health> healths) {
        final List<Status> statusCandidates = new ArrayList<Status>();
        final Map<String, Object> details = new LinkedHashMap<String, Object>();
        for (final Map.Entry<String, Health> entry : healths.entrySet()) {
            details.put(entry.getKey(), entry.getValue());
            statusCandidates.add(entry.getValue().getStatus());
        }
        return new Health.Builder(aggregateStatus(statusCandidates), details).build();
    }

    /**
     * Return the single 'aggregate' status that should be used from the specified candidates.
     *
     * @param candidates
     *            the candidates
     * @return a single status
     */
    protected abstract Status aggregateStatus(List<Status> candidates);

}
