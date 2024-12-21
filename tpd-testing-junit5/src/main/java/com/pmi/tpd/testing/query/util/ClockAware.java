package com.pmi.tpd.testing.query.util;

import java.time.Clock;

public interface ClockAware {

    /**
     * Clock used by this instance.
     *
     * @return clock
     */
    Clock clock();
}
