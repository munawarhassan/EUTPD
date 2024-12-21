package com.pmi.tpd.core.event.publisher;

import static org.apache.commons.lang3.ObjectUtils.identityToString;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.ClassUtils;
import com.pmi.tpd.core.event.config.IListenerHandlersConfiguration;

/**
 * <p>
 * The default implementation of the {@link com.pmi.tpd.api.event.publisher.IEventPublisher} interface.
 * </p>
 * <p>
 * <p>
 * One can customise the event listening by instantiating with custom {@link IListenerHandler listener handlers} and the
 * event dispatching through {@link com.pmi.tpd.core.event.publisher.IEventDispatcher}. See the
 * {@link com.pmi.tpd.core.event.spi} package for more information.
 * </p>
 *
 * @see IListenerHandler
 * @see IEventDispatcher
 * @author Christophe Friederich
 * @since 1.0
 */
public final class EventPublisherImpl implements IEventPublisher {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherImpl.class);

    /** */
    private final IEventDispatcher eventDispatcher;

    /** */
    private final List<IListenerHandler> listenerHandlers;

    /**
     * <strong>Note:</strong> this field makes this implementation stateful.
     */
    private final Multimap<Class<?>, KeyedListenerInvoker> listenerInvokers;

    /**
     * <p>
     * If you need to customise the asynchronous handling, you should use the
     * {@link com.pmi.tpd.core.event.publisher.AsynchronousAbleEventDispatcher} together with a custom executor.
     * You might also want to have a look at using the {@link com.pmi.tpd.core.event.publisher.EventThreadFactory}
     * to keep the naming of event threads consistent with the default naming of the Application Event.
     * <p>
     *
     * @param eventDispatcher
     *            the event dispatcher to be used with the publisher
     * @param listenerHandlersConfiguration
     *            the list of listener handlers to be used with this publisher
     * @see AsynchronousAbleEventDispatcher
     * @see EventThreadFactory
     */
    public EventPublisherImpl(@Nonnull final IEventDispatcher eventDispatcher,
            @Nonnull final IListenerHandlersConfiguration listenerHandlersConfiguration) {
        this.eventDispatcher = Assert.notNull(eventDispatcher);
        this.listenerHandlers = Assert.notNull(Assert.notNull(listenerHandlersConfiguration).getListenerHandlers());
        this.listenerInvokers = newMultimap();
    }

    @Override
    public void shutdown() {
        this.unregisterAll();
        this.eventDispatcher.shutdown();
    }

    /** {@inheritDoc} */
    @Override
    public void publish(@Nonnull final Object event) {
        invokeListeners(findListenerInvokersForEvent(Assert.checkNotNull(event, "event")), event);
    }

    /** {@inheritDoc} */
    @Override
    public void register(@Nonnull final Object listener) {
        registerListener(identityToString(Assert.checkNotNull(listener, "listener")), listener);
    }

    /** {@inheritDoc} */
    @Override
    public void unregister(@Nonnull final Object listener) {
        unregisterListener(identityToString(Assert.notNull(listener)));
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterAll() {
        synchronized (listenerInvokers) {
            listenerInvokers.clear();
        }
    }

    private void unregisterListener(@Nonnull final String listenerKey) {
        Assert.checkHasText(listenerKey, "Key for the listener must not be empty");

        /**
         * see {@link Multimaps#synchronizedMultimap(Multimap)} for why this synchronise block is there
         */
        synchronized (listenerInvokers) {
            for (final Iterator<Map.Entry<Class<?>, KeyedListenerInvoker>> invokerIterator = listenerInvokers.entries()
                    .iterator(); invokerIterator.hasNext();) {
                if (invokerIterator.next().getValue().getKey().equals(listenerKey)) {
                    invokerIterator.remove();
                }
            }
        }
    }

    private void registerListener(@Nonnull final String listenerKey, @Nonnull final Object listener) {
        synchronized (listenerInvokers) {
            /*
             * Because we need to un-register an re-register in one 'atomic' operation
             */
            unregisterListener(listenerKey);

            final List<IListenerInvoker> invokers = Lists.newArrayList();
            for (final IListenerHandler listenerHandler : listenerHandlers) {
                invokers.addAll(listenerHandler.getInvokers(listener));
            }
            if (!invokers.isEmpty()) {
                registerListenerInvokers(listenerKey, invokers);
            } else {
                throw new IllegalArgumentException("No listener invokers were found for listener <" + listener + ">");
            }
        }
    }

    @Nonnull
    private Set<KeyedListenerInvoker> findListenerInvokersForEvent(@Nonnull final Object event) {
        final Set<KeyedListenerInvoker> invokersForEvent = Sets.newHashSet();
        /**
         * see {@link Multimaps#synchronizedMultimap(Multimap)} for why this synchronise block is there
         */
        synchronized (listenerInvokers) {
            for (final Class<?> eventClass : ClassUtils.findAllTypes(Assert.notNull(event).getClass())) {
                invokersForEvent.addAll(listenerInvokers.get(eventClass));
            }
        }
        return invokersForEvent;
    }

    private void invokeListeners(@Nonnull final Collection<KeyedListenerInvoker> listenerInvokers,
        @Nonnull final Object event) {
        for (final KeyedListenerInvoker keyedInvoker : listenerInvokers) {
            try {
                eventDispatcher.dispatch(keyedInvoker.getInvoker(), event);
            } catch (final Throwable t) {
                LOGGER.error("There was an exception thrown trying to dispatch event '" + event + "' from the invoker '"
                        + keyedInvoker.getInvoker() + "'.",
                    t);
            }
        }
    }

    private void registerListenerInvokers(@Nonnull final String listenerKey,
        @Nonnull final List<? extends IListenerInvoker> invokers) {
        for (final IListenerInvoker invoker : invokers) {
            registerListenerInvoker(listenerKey, invoker);
        }
    }

    private void registerListenerInvoker(@Nonnull final String listenerKey, @Nonnull final IListenerInvoker invoker) {
        // if supported classes is empty, then all events are supported.
        if (invoker.getSupportedEventTypes().isEmpty()) {
            listenerInvokers.put(Object.class, new KeyedListenerInvoker(listenerKey, invoker));
        }

        // if it it empty, we won't loop, otherwise register the invoker against
        // all its classes
        for (final Class<?> eventClass : invoker.getSupportedEventTypes()) {
            listenerInvokers.put(eventClass, new KeyedListenerInvoker(listenerKey, invoker));
        }
    }

    @Nonnull
    private Multimap<Class<?>, KeyedListenerInvoker> newMultimap() {
        return Multimaps.synchronizedMultimap(Multimaps.newMultimap(Maps.<Class<?>, //
        Collection<KeyedListenerInvoker>> newHashMap(), new Supplier<Collection<KeyedListenerInvoker>>() {

            @Override
            public Collection<KeyedListenerInvoker> get() {
                return Sets.newHashSet();
            }
        }));
    }

    /**
     * Map a Listener Invoker with key.
     *
     * @author Christophe Friederich
     */
    private static final class KeyedListenerInvoker {

        /** */
        @Nonnull
        private final String key;

        /** */
        @Nonnull
        private final IListenerInvoker invoker;

        KeyedListenerInvoker(@Nonnull final String key, @Nonnull final IListenerInvoker invoker) {
            this.invoker = invoker;
            this.key = key;
        }

        @Nonnull
        public String getKey() {
            return key;
        }

        @Nonnull
        public IListenerInvoker getInvoker() {
            return invoker;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder(5, 23).append(key).append(invoker).toHashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            final KeyedListenerInvoker kli = (KeyedListenerInvoker) obj;
            return new EqualsBuilder().append(key, kli.key).append(invoker, kli.invoker).isEquals();
        }
    }
}
