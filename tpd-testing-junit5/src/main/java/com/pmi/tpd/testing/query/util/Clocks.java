package com.pmi.tpd.testing.query.util;

import java.time.Clock;

public final class Clocks {

    private Clocks() {
        throw new AssertionError("Don't instantiate me");
    }

    public static Clock getClock(final Object instance) {
        if (instance instanceof ClockAware) {
            return ((ClockAware) instance).clock();
        } else {
            return Clock.systemDefaultZone();
        }
    }
}