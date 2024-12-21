package com.pmi.tpd.database;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * IContextValidation interface.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public interface IContextValidation {

    /**
     * Flushes all changes to objects in this context to the parent DataChannel, cascading flush operation all the way
     * through the stack, ultimately saving data in the database.
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    void acceptChanges();

    /**
     * Resets all uncommitted changes made to the objects in this ObjectContext, cascading rollback operation all the
     * way through the stack.
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    void rejectChanges();
}
