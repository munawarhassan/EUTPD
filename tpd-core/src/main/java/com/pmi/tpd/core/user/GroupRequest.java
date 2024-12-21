package com.pmi.tpd.core.user;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@JsonSerialize
public final class GroupRequest {

    /** */
    private final boolean deletable;

    /** */
    private final String name;

    /** */
    private final boolean active;

    @JsonCreator
    private GroupRequest(final String name, final boolean deletable, final boolean active) {
        this.name = name;
        this.deletable = deletable;
        this.active = active;
    }

    /**
     * @return Returns the name of group.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * @return Returns {@code true} whether the group is deletable, otherwise {@code false}.
     */
    public boolean isDeletable() {
        return deletable;
    }

    /**
     * @return Returns {@code true} whether the group is activate, otherwise {@code false}.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return Returns the {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder {

        /** */
        private boolean deletable;

        /** */
        private String name;

        /** */
        private boolean active;

        private Builder() {
        }

        /**
         * @param group
         *            group used to fill the builder.
         */
        public Builder(final GroupRequest group) {
            deletable = group.isDeletable();
            name = group.getName();
        }

        /**
         * @param value
         * @return
         */
        public Builder deletable(final boolean value) {
            deletable = value;

            return this;
        }

        /**
         * @param value
         * @return
         */
        public Builder name(final String value) {
            state(!checkNotNull(value, "value").trim().isEmpty(), "The value may not be blank or empty");
            name = value;

            return this;
        }

        /**
         * @param value
         * @return
         */
        public Builder active(final boolean value) {
            active = value;

            return this;
        }

        /**
         * @return Returns new populate instance of {@link GroupRequest}.
         */
        public GroupRequest build() {
            state(StringUtils.isNotBlank(name), "A group name is required");

            return new GroupRequest(name, deletable, active);
        }
    }
}
