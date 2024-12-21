package com.pmi.tpd.database.config;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.ApplicationConstants;

/**
 * Properties to be removed/commented from the configuration file.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class RemoveSetupConfigurationRequest {

    /** */
    private final boolean removeSysAdmin;

    /** */
    private final boolean removeBaseUrl;

    /** */
    private final boolean removeDisplayName;

    private RemoveSetupConfigurationRequest(final boolean removeSysAdmin, final boolean removeBaseUrl,
            final boolean removeDisplayName) {
        this.removeSysAdmin = removeSysAdmin;
        this.removeBaseUrl = removeBaseUrl;
        this.removeDisplayName = removeDisplayName;
    }

    /**
     * @return
     */
    public boolean isRemoveSysAdmin() {
        return removeSysAdmin;
    }

    /**
     * @return
     */
    public boolean isRemoveBaseUrl() {
        return removeBaseUrl;
    }

    /**
     * @return
     */
    public boolean isRemoveDisplayName() {
        return removeDisplayName;
    }

    /**
     * @return
     */
    public Set<String> toProperties() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        if (removeSysAdmin) {
            builder.add(ApplicationConstants.Setup.SETUP_USER_NAME);
            builder.add(ApplicationConstants.Setup.SETUP_USER_PASSWORD);
            builder.add(ApplicationConstants.Setup.SETUP_USER_DISPLAY_NAME);
            builder.add(ApplicationConstants.Setup.SETUP_USER_EMAIL_ADDRESS);
        }

        if (removeBaseUrl) {
            builder.add(ApplicationConstants.Setup.SETUP_BASE_URL);
        }
        if (removeDisplayName) {
            builder.add(ApplicationConstants.Setup.SETUP_DISPLAY_NAME);
        }

        return builder.build();
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder {

        /** */
        private boolean removeSysAdmin;

        /** */
        private boolean removeBaseUrl;

        /** */
        private boolean removeDisplayName;

        /**
         * @return
         */
        public boolean hasPropertiesToRemove() {
            return removeSysAdmin || removeBaseUrl || removeDisplayName;
        }

        /**
         * @return
         */
        public Builder removeSysAdmin() {
            removeSysAdmin = true;

            return this;
        }

        /**
         * @return
         */
        public Builder removeBaseUrl() {
            removeBaseUrl = true;

            return this;
        }

        /**
         * @return
         */
        public Builder removeDisplayName() {
            removeDisplayName = true;

            return this;
        }

        /**
         * @return
         */
        public RemoveSetupConfigurationRequest build() {
            return new RemoveSetupConfigurationRequest(removeSysAdmin, removeBaseUrl, removeDisplayName);
        }
    }
}
