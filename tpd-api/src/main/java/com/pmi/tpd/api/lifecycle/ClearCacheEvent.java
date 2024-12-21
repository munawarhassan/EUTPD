package com.pmi.tpd.api.lifecycle;

/**
 * <p>
 * ClearCacheEvent class.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public final class ClearCacheEvent {

    /** */
    private static final ClearCacheEvent INSTANCE = new ClearCacheEvent();

    /**
     * <p>
     * empty.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.lifecycle.ClearCacheEvent} object.
     */
    public static ClearCacheEvent empty() {
        return INSTANCE;
    }

    private ClearCacheEvent() {
    }
}
