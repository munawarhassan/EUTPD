package com.pmi.tpd.core.user.permission;

/**
 * The permitted operations allowed on an entity for the current user.
 *
 * @since 2.0
 */
public interface IPermittedOperations {

    /**
     * @return true if the entity is editable by the current user
     */
    boolean isEditable();

    /**
     * @return true if the entity is deletable by the current user
     */
    boolean isDeletable();
}
