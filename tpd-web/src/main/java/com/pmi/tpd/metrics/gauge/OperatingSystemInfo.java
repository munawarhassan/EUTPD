package com.pmi.tpd.metrics.gauge;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Clock;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
@ParametersAreNonnullByDefault
public class OperatingSystemInfo extends CachedGauge<String> {

    /** */
    private final OperatingSystemMXBean osMxBean;

    /**
     *
     */
    public OperatingSystemInfo() {
        this(ManagementFactory.getOperatingSystemMXBean(), 1, TimeUnit.HOURS, Clock.defaultClock());
    }

    /**
     * @param osMxBean
     */
    public OperatingSystemInfo(final OperatingSystemMXBean osMxBean) {
        this(osMxBean, 1, TimeUnit.HOURS, Clock.defaultClock());
    }

    /**
     * @param osMxBean
     * @param timeout
     * @param timeoutUnit
     * @param timeoutClock
     */
    public OperatingSystemInfo(final OperatingSystemMXBean osMxBean, final long timeout, final TimeUnit timeoutUnit,
            final Clock timeoutClock) {
        super(checkNotNull(timeoutClock, "timeoutClock"), timeout, checkNotNull(timeoutUnit, "timeoutUnit"));
        this.osMxBean = checkNotNull(osMxBean, "osMxBean");
    }

    @Override
    protected String loadValue() {
        return String.format("%s, %s (%d %s cpus)",
            osMxBean.getName(),
            osMxBean.getVersion(),
            osMxBean.getAvailableProcessors(),
            osMxBean.getArch());
    }
}