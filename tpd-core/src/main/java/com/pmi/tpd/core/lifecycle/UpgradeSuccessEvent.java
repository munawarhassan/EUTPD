package com.pmi.tpd.core.lifecycle;

/**
 * <p>
 * UpgradeSuccessEvent class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class UpgradeSuccessEvent {

    /** */
    private static final UpgradeSuccessEvent INSTANCE = new UpgradeSuccessEvent();

    /**
     * <p>
     * empty.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.lifecycle.UpgradeSuccessEvent} object.
     */
    public static UpgradeSuccessEvent empty() {
        return INSTANCE;
    }

    private UpgradeSuccessEvent() {
    }
}
