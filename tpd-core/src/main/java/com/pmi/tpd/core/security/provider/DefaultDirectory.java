package com.pmi.tpd.core.security.provider;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.pmi.tpd.core.security.OperationType;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public final class DefaultDirectory implements IDirectory {

    /** Default instance for internal user directory. */
    public static final IDirectory INTERNAL = new DefaultDirectory("Internal Directory",
            Arrays.asList(OperationType.CREATE_GROUP,
                OperationType.CREATE_USER,
                OperationType.DELETE_GROUP,
                OperationType.DELETE_USER,
                OperationType.UPDATE_GROUP,
                OperationType.UPDATE_USER),
            true);

    /** */
    private final String name;

    /** */
    private final Set<OperationType> operations;

    /** */
    private final boolean active;

    /**
     * @param name
     *            the name of directory.
     * @param operations
     *            the list of operations available for a directory.
     * @param active
     *            indicate if the directory can be used.
     */
    public DefaultDirectory(@Nonnull final String name, @Nonnull final List<OperationType> operations,
            final boolean active) {
        this.name = checkNotNull(name, "name");
        this.operations = Sets.newHashSet(checkNotNull(operations, "operations"));
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserDeletable() {
        return operations.contains(OperationType.DELETE_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserUpdatable() {
        return operations.contains(OperationType.UPDATE_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserCreatable() {
        return operations.contains(OperationType.CREATE_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDeletable() {
        return operations.contains(OperationType.DELETE_GROUP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupUpdatable() {
        return operations.contains(OperationType.UPDATE_GROUP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupCreatable() {
        return operations.contains(OperationType.CREATE_GROUP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<OperationType> getAllowedOperations() {
        return operations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

}
