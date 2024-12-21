package com.pmi.tpd.api.event.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate events which need to be aware of the surrounding transaction, if any.
 * <p>
 * The primary use case for this annotation is events which are raised when a persistent entity is updated in some way.
 * Such events may be raised while the transaction to update the entity is still in progress, but should only be
 * <i>published</i> when the transaction is committed.
 * <p>
 * If a {@code TransactionAware} event is raised while no transaction is in progress, it is always published immediately
 * regardless of the configuration applied by this annotation.
 * <p>
 * This setting is <i>inherited</i>. If a derived event does not need to be transaction aware, it may be annotated with:
 * <code><pre>
 *     &#064;TransactionAware(TransactionAware.When.IMMEDIATE)
 * </pre></code> Such configuration indicates the event should be published immediately when it is raised, which is the
 * same behaviour that is applied if this annotation is not present on the event's class.
 *
 * @since 1.3
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TransactionAware {

    /**
     * Configures when the event should be dispatched, relative to the transaction lifecycle.
     * <p>
     * By default, the event will be dispatched {@link When#AFTER_COMMIT after the transaction is committed}. This means
     * if the transaction does not commit, the event <i>will not be published</i>.
     *
     * @return the point in the transaction lifecycle at which the event should be published
     * @see When
     */
    When value() default When.AFTER_COMMIT;

    /**
     * Defines the different points, relative to a transaction's lifecycle, at which an event can be published.
     *
     * @since 1.3
     */
    enum When {

        /**
         * The event should be published after the transaction has committed, but before it has {@link #AFTER_COMPLETION
         * completed}.
         * <p>
         * Note: If the transaction does not commit (for example, an optimistic locking failure triggers a rollback on
         * the transaction), the event will <i>never be published</i>.
         */
        AFTER_COMMIT,
        /**
         * The event should be published after the transaction has completed, <i>whether it is {@link #AFTER_COMMIT
         * committed} or rolled back</i>.
         */
        AFTER_COMPLETION,
        /**
         * The event should be published immediately, regardless of any transaction in progress.
         * <p>
         * Because {@link TransactionAware} is inherited, this entry allows derived classes to effectively disable
         * transaction awareness imposed on them by superclasses. It should not be used otherwise; simply leaving off
         * the {@link TransactionAware} annotation will produce the same behaviour.
         */
        IMMEDIATE
    }
}
