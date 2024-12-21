package com.pmi.tpd.core.user.permission;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pmi.tpd.security.permission.Permission;

/**
 * Criteria to determine whether a group or several groups have a effective permission.
 *
 * @see EffectivePermissionDao
 */
public final class GroupPermissionCriteria extends PermissionCriteria {

    /** */
    private final Set<String> groups;

    private GroupPermissionCriteria(final Iterable<String> groups, final Permission permission) {
        super(permission);
        Preconditions.checkArgument(groups != null && !Iterables.isEmpty(groups), "at least a group must be specified");
        Preconditions.checkArgument(Iterables.all(groups, Predicates.notNull()), "null groups are not accepted");
        this.groups = ImmutableSet.copyOf(groups);
    }

    /**
     * Gets the groups associated with this criteria.
     *
     * @return the groups
     */
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractBuilder<Builder> {

        /** */
        private final Iterable<String> groups;

        /**
         * @param group
         */
        public Builder(final String group) {
            this.groups = Collections.singleton(group);
        }

        /**
         * @param groups
         */
        public Builder(final Iterable<String> groups) {
            this.groups = groups;
        }

        /**
         * @return
         */
        public GroupPermissionCriteria build() {
            return new GroupPermissionCriteria(groups, permission);
        }

        @Override
        protected Builder self() {
            return this;
        }

    }

}
