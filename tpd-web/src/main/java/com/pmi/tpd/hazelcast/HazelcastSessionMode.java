package com.pmi.tpd.hazelcast;

import javax.annotation.Nonnull;

/**
 * Represents whether session data should be managed by Hazelcast and if so, whether it should be optimized for sticky
 * sessions.
 */
public enum HazelcastSessionMode {

    /**
     * Session data is managed locally by Tomcat
     */
    LOCAL,
    /**
     * Session data is managed by Hazelcast, but optimized for sticky sessions
     */
    STICKY,
    /**
     * Session data is managed by Hazelcast assuming non-sticky sessions
     */
    REPLICATED;

    /**
     * @return the configuration value for Hazelcast's {@code sticky-session} property.
     */
    @Nonnull
    public String getStickySessionProperty() {
        return Boolean.toString(this == STICKY);
    }

    @Nonnull
    static HazelcastSessionMode forProperty(final String property) {
        for (final HazelcastSessionMode setting : values()) {
            if (setting.name().equalsIgnoreCase(property)) {
                return setting;
            }
        }

        throw new IllegalArgumentException("Unsupported property '" + property + "'");
    }
}
