package com.pmi.tpd.testing.query.util;

public interface Backoff {

    void yield() throws InterruptedException;

    void yield(long bound) throws InterruptedException;

    void resetBackoff();
}
