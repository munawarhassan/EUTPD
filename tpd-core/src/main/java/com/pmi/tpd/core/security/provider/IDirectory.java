package com.pmi.tpd.core.security.provider;

import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.security.OperationType;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IDirectory {

    /**
     * @return Returns a {@link String} representing the name of Directory.
     */
    String getName();

    /**
     * @return Returns {@code true} whether a user managed by the directory is deletable, otherwise {@code false}.
     */
    boolean isUserDeletable();

    /**
     * @return Returns {@code true} whether a user managed by the directory is updatable, otherwise {@code false}.
     */
    boolean isUserUpdatable();

    /**
     * @return Returns {@code true} whether a user could be created in the directory, otherwise {@code false}.
     */
    boolean isUserCreatable();

    /**
     * @return Returns {@code true} whether a group managed by the directory is deletable, otherwise {@code false}.
     */
    boolean isGroupDeletable();

    /**
     * @return Returns {@code true} whether a group managed by the directory is updatable, otherwise {@code false}.
     */
    boolean isGroupUpdatable();

    /**
     * @return Returns {@code true} whether a group could be created in the directory, otherwise {@code false}.
     */
    boolean isGroupCreatable();

    /**
     * @return Returns a {@link Set} representing all operations possible on the directory (can not be {@code null}).
     */
    @Nonnull
    Set<OperationType> getAllowedOperations();

    /**
     * @return Returns {@code true} whether the directory in active, otherwise {@code false}.
     */
    boolean isActive();

}