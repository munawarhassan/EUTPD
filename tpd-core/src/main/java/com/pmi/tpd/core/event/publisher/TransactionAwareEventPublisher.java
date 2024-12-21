package com.pmi.tpd.core.event.publisher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.user.GroupDeletedEvent;
import com.pmi.tpd.core.event.user.GroupMembershipCreatedEvent;
import com.pmi.tpd.core.event.user.GroupMembershipDeletedEvent;
import com.pmi.tpd.core.event.user.UserDeletedEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

/**
 * Implementation of {@link IEventPublisher} that fills in a number of standard BaseEvent properties.
 */
public class TransactionAwareEventPublisher implements IEventPublisher, ApplicationListener<ContextRefreshedEvent> {

    /** */
    private static final Map<Class<? extends BaseEvent>, TransactionAware.When> TRANSACTION_AWARE_OVERRIDES = //
            ImmutableMap.<Class<? extends BaseEvent>, TransactionAware.When> builder()
                    .put(GroupDeletedEvent.class, TransactionAware.When.AFTER_COMMIT)
                    .put(GroupMembershipCreatedEvent.class, TransactionAware.When.AFTER_COMMIT)
                    .put(GroupMembershipDeletedEvent.class, TransactionAware.When.AFTER_COMMIT)
                    .put(UserDeletedEvent.class, TransactionAware.When.AFTER_COMMIT)
                    // .put(UserUpdatedEvent.class, TransactionAware.When.AFTER_COMMIT)
                    .build();

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionAwareEventPublisher.class);

    /** */
    private final IEventPublisher delegate;

    /** */
    private final ITransactionSynchronizer synchronizer;

    /** */
    private final Field userField;

    /** */
    private IAuthenticationContext authenticationContext;

    /**
     * @param delegate
     *            the event publisher.
     * @param synchronizer
     *            the transaction synchronizer.
     */
    @Inject
    public TransactionAwareEventPublisher(@Nonnull final IEventPublisher delegate,
            @Nonnull final ITransactionSynchronizer synchronizer) {
        this.delegate = Assert.checkNotNull(delegate, "delegate");
        this.synchronizer = Assert.checkNotNull(synchronizer, "synchronizer");

        userField = getField("user");
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        authenticationContext = event.getApplicationContext().getBean(IAuthenticationContext.class);
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    /**
     * Publishes the event after first setting standard fields, such as the user triggering the event.
     * <p>
     * If a transaction is active, transaction synchronisation is available and the provided {@code event} is
     * {@link TransactionAware}, event publishing will be deferred until the requested {@link TransactionAware.When
     * publish point}.
     *
     * @param event
     *            the event to publish
     */
    @Override
    public void publish(@Nonnull final Object event) {
        setFields(checkNotNull(event, "event"));

        // If this event is transaction-aware (typically meaning it includes data that is somehow related to a
        // transaction, such as Hibernate entities) and we're in a transaction, don't actually raise the event
        // until the desired publishing point (after commit, or after completion)
        final TransactionAware.When publishWhen = getTransactionConfiguration(event.getClass());
        if (publishWhen != null && publishWhen != TransactionAware.When.IMMEDIATE) {
            if (synchronizer.register(createPublisher(publishWhen, event))) {
                LOGGER.debug("Deferring publishing for {} until {}", event.getClass().getSimpleName(), publishWhen);
                return;
            }
        }

        delegate.publish(event);
    }

    @Override
    public void register(final Object listener) {
        delegate.register(listener);
    }

    @Override
    public void unregister(final Object listener) {
        delegate.unregister(listener);
    }

    @Override
    public void unregisterAll() {
        delegate.unregisterAll();
    }

    private void setFields(final Object event) {
        if (event instanceof BaseEvent) {
            if (authenticationContext != null) {
                ReflectionUtils.setField(userField, event, authenticationContext.getCurrentUser().orElse(null));
            }
        }
    }

    private static Field getField(final String fieldName) {
        final Field field = ReflectionUtils.findField(BaseEvent.class, fieldName);
        ReflectionUtils.makeAccessible(field);

        return field;
    }

    private TransactionSynchronization createPublisher(final TransactionAware.When publishWhen, final Object event) {
        switch (publishWhen) {
            case AFTER_COMMIT:
                return new AfterCommitPublisher(event);
            case AFTER_COMPLETION:
                return new AfterCompletionPublisher(event);
            default:
                break;
        }
        throw new IllegalArgumentException(publishWhen + " is not a known publishing point");
    }

    private static TransactionAware.When getTransactionConfiguration(final Class<?> eventClass) {
        TransactionAware.When publishWhen = TRANSACTION_AWARE_OVERRIDES.get(eventClass);
        if (publishWhen == null) {
            final TransactionAware annotation = AnnotationUtils.findAnnotation(eventClass, TransactionAware.class);
            if (annotation != null) {
                publishWhen = annotation.value();
            }
        }
        return publishWhen;
    }

    /**
     * Publishes the event provided during construction after the transaction commits.
     * <p>
     * <b>Warning</b>: If the transaction does not commit, most likely because it failed and was rolled back, the event
     * will <i>never</i> be published. It is assumed that this is the desired behaviour based on the event was
     * annotated.
     */
    private final class AfterCommitPublisher implements TransactionSynchronization {

        /** */
        private final Object event;

        /** */
        private AfterCommitPublisher(final Object event) {
            this.event = event;
        }

        @Override
        public void afterCompletion(final int status) {
            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                LOGGER.debug("Publishing {} after commit", event.getClass().getSimpleName());
                delegate.publish(event);
            } else {
                LOGGER.trace("Discarding {}; the transaction was not committed (Status: {})",
                    event.getClass().getSimpleName(),
                    status);
            }
        }
    }

    /**
     * Publishes the event provided during construction after the transaction completes.
     * <p>
     * Unlike the {@link AfterCommitPublisher}, this publisher will <i>always</i> publish the event, whether the
     * transaction commits or is rolled back (or ends with some other, unknown status).
     */
    private final class AfterCompletionPublisher implements TransactionSynchronization {

        /** */
        private final Object event;

        private AfterCompletionPublisher(final Object event) {
            this.event = event;
        }

        @Override
        public void afterCompletion(final int status) {
            LOGGER.debug("Publishing {} after completion (Status: {})", event.getClass().getSimpleName(), status);
            delegate.publish(event);
        }
    }
}
