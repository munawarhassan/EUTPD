package com.pmi.tpd.spring.transaction;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DelegatingTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * String Transaction Definition Utility class.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public final class SpringTransactionUtils {

    private SpringTransactionUtils() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     *
     */
    public static final TransactionDefinition REQUIRES_NEW = constantFor(
        TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    public static final TransactionDefinition REQUIRED = constantFor(TransactionDefinition.PROPAGATION_REQUIRED);

    /**
     * Create transaction definition with specific propagation behavior.
     *
     * @param propagationBehavior
     *                            propagation behavior to use.
     * @return Returns new transaction definition with specific propagation behavior.
     */
    public static TransactionDefinition definitionFor(final int propagationBehavior) {
        return definitionFor(propagationBehavior, false);
    }

    /**
     * Create transaction definition with specific propagation behavior.
     *
     * @param propagationBehavior
     *                            propagation behavior to use.
     * @param readOnly
     *                            to optimize as read-only transaction.
     * @return Returns new transaction definition with specific propagation behavior, indicating read-only transaction.
     */
    public static TransactionDefinition definitionFor(final int propagationBehavior, final boolean readOnly) {
        final DefaultTransactionDefinition definition = new DefaultTransactionDefinition(propagationBehavior);
        definition.setReadOnly(readOnly);

        return definition;
    }

    private static TransactionDefinition constantFor(final int propagationBehavior) {
        return new UnmodifiableTransactionDefinition(definitionFor(propagationBehavior));
    }

    /**
     * Invoke the provided callback after the transaction commits or immediately if no transaction is active. In the
     * case of a transaction rollback, the callback will not be invoked.
     *
     * @param callback
     *                 the callback to invoke
     */
    public static void invokeAfterCommit(final Runnable callback) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    callback.run();
                }
            });
        } else {
            callback.run();
        }
    }

    /**
     * Wraps a {@code TransactionDefinition} (typically a {@code DefaultTransactionDefinition}) to ensure it cannot be
     * updated. For definition constants, this ensures the definition isn't cast and modified in place.
     * <p>
     * Note: This class exists because {@code DelegatingTransactionDefinition} is {@code abstract}.
     */
    private static final class UnmodifiableTransactionDefinition extends DelegatingTransactionDefinition {

        /**
         *
         */
        private static final long serialVersionUID = 3925199937629212559L;

        private UnmodifiableTransactionDefinition(final TransactionDefinition targetDefinition) {
            super(targetDefinition);
        }
    }
}
