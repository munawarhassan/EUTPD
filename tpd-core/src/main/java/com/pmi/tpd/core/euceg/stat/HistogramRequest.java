package com.pmi.tpd.core.euceg.stat;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.LongBounds;
import org.joda.time.DateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class HistogramRequest {

    public enum HistogramInterval {

        day(DateHistogramInterval.HOUR, "H"),
        week(DateHistogramInterval.DAY, "E"),
        month(DateHistogramInterval.DAY, "d"),
        year(DateHistogramInterval.MONTH, "MMM");

        private DateHistogramInterval interval;

        private String format;

        private HistogramInterval(final DateHistogramInterval interval, final String format) {
            this.interval = interval;
            this.format = format;
        }

        public DateHistogramInterval getInterval() {
            return interval;
        }

        public String getFormat() {
            return format;
        }

    }

    /** */
    private HistogramInterval interval;

    /** */
    private int bounds;

    public HistogramRange range(final DateTime origin) {
        DateTime start = origin;
        DateTime end = origin.minusDays(getBounds());
        switch (getInterval()) {
            case day: {
                start = origin.withTime(0, 0, 0, 0);
                end = origin.withTime(23, 59, 59, 999);
                break;
            }
            case week: {
                start = origin.dayOfWeek().withMinimumValue().withTime(0, 0, 0, 0);
                end = origin.dayOfWeek().withMaximumValue().withTime(23, 59, 59, 999);
                break;
            }
            case month: {
                start = origin.withDayOfMonth(1).withTime(23, 59, 59, 999);
                end = origin.plusMonths(1).dayOfMonth().withMinimumValue().minusDays(1).withTime(23, 59, 59, 999);
                break;
            }
            case year: {
                start = origin.minusMonths(getBounds()).dayOfMonth().withMinimumValue().withTime(0, 0, 0, 0);
                end = origin.dayOfMonth().withMaximumValue().withTime(23, 59, 59, 999);
                break;
            }
            default:
                return null;
        }
        return HistogramRange.builder().start(start).end(end).build();
    }

    public RangeQueryBuilder createRangeQuery() {
        final HistogramRange range = range(DateTime.now());
        return QueryBuilders.rangeQuery("createdDate")
                .timeZone(range.getStart().getZone().toTimeZone().toZoneId().toString())
                .gt(range.getStart().getMillis())
                .lt(range.getEnd().getMillis());
    }

    public AbstractAggregationBuilder<?> createAggregation(final String name, final String field) {
        final HistogramRange range = range(DateTime.now());
        final DateTime start = range.getStart();
        final DateTime end = range.getEnd();
        return AggregationBuilders.dateHistogram(name)
                .field(field)
                .calendarInterval(getInterval().getInterval())
                .format(getInterval().getFormat())
                .minDocCount(0)
                .timeZone(start.getZone().toTimeZone().toZoneId())
                .extendedBounds(new LongBounds(start.getMillis(), end.getMillis()));
    }

    @Getter
    @Builder
    @Jacksonized
    public static class HistogramRange {

        private DateTime start;

        private DateTime end;
    }
}
