package com.pmi.tpd.scheduler.spring;

import java.util.TimeZone;

import com.pmi.tpd.api.context.ITimeZoneHelper;
import com.pmi.tpd.scheduler.spi.ISchedulerServiceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class SchedulerServiceConfiguration implements ISchedulerServiceConfiguration {

    /** */
    private final ITimeZoneHelper timeZoneHelper;

    /**
     * Default constructor.
     *
     * @param timeZoneHelper
     *            timezone helper.
     */
    public SchedulerServiceConfiguration(final ITimeZoneHelper timeZoneHelper) {
        this.timeZoneHelper = timeZoneHelper;
    }

    @Override
    public TimeZone getDefaultTimeZone() {
        return timeZoneHelper.getTimeZone();
    }

}
