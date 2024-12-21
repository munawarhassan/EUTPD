package com.pmi.tpd.api.audit;

import com.pmi.tpd.api.audit.annotation.Audited;

/**
 * The priority levels at which audit events can be raised. Lower levels are filtered out first by consumers.
 * {@link #NONE} can be used to cancel an inherited {@link Audited &#064;Audited} annotation
 * 
 * @author Christophe Friederich
 * @since 2.4
 */
public enum Priority {

    HIGH(1000),
    MEDIUM(700),
    LOW(300),
    NONE(0);

    private int weight;

    Priority(final int weightToAdd) {
        weight = weightToAdd;
    }

    /**
     * Retrieves the weight of this priority relative to other priorities. When filtering, higher weighted priorities
     * will be filtered last.
     *
     * @return the weight of this priority
     */
    public int getWeight() {
        return weight;
    }
}
