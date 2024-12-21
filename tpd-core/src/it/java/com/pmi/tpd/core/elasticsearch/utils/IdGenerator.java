package com.pmi.tpd.core.elasticsearch.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to provide sequential IDs. Uses an integer, 2^31 -1 values should be enough for the test runs.
 */
public final class IdGenerator {

    private static final AtomicInteger NEXT = new AtomicInteger();

    private IdGenerator() {
    }

    public static int nextIdAsInt() {
        return NEXT.incrementAndGet();
    }

    public static double nextIdAsDouble() {
        return NEXT.incrementAndGet();
    }

    public static String nextIdAsString() {
        return "" + nextIdAsInt();
    }
}