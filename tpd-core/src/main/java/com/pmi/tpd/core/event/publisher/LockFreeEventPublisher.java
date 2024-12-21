package com.pmi.tpd.core.event.publisher;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.ClassUtils;
import com.pmi.tpd.core.event.config.IListenerHandlersConfiguration;

/**
 * A non-blocking implementation of the {@link com.pmi.tpd.api.event.publisher.IEventPublisher} interface.
 * <p>
 * This class is a drop-in replacement for {@link com.pmi.tpd.core.event.publisher.EventPublisherImpl} except that
 * it does not synchronise on the internal map of event type to {@link IListenerInvoker}, and should handle much higher
 * parallelism of event dispatch.
 * <p>
 * One can customise the event listening by instantiating with custom {@link IListenerHandler listener handlers} and the
 * event dispatching through {@link com.pmi.tpd.core.event.publisher.IEventDispatcher}.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @see IListenerHandler
 * @see IEventDispatcher
 */
public final class LockFreeEventPublisher implements IEventPublisher {

    /**
     * Gets the {@link IListenerInvoker invokers} for a listener.
     */
    private final InvokerBuilder invokerBuilder;

    /**
     * Publishes an event.
     */
    private final Publisher publisher;

    /**
     * <strong>Note:</strong> this field makes this implementation stateful.
     */
    private final Listeners listeners = new Listeners();

    /**
     * If you need to customise the asynchronous handling, you should use the
     * {@link com.pmi.tpd.core.event.publisher.AsynchronousAbleEventDispatcher} together with a custom executor.
     * <p>
     * You might also want to have a look at using the {@link com.pmi.tpd.core.event.publisher.EventThreadFactory}
     * to keep the naming of event threads consistent with the default naming of the Application Event.
     *
     * @param eventDispatcher
     *            the event dispatcher to be used with the publisher
     * @param listenerHandlersConfiguration
     *            the list of listener handlers to be used with this publisher
     * @throws java.lang.IllegalArgumentException
     *             if the {@code eventDispatcher} or {@code listenerHandlersConfiguration} is {@code null}
     * @see AsynchronousAbleEventDispatcher
     * @see EventThreadFactory
     */
    public LockFreeEventPublisher(@Nonnull final IEventDispatcher eventDispatcher,
            @Nonnull final IListenerHandlersConfiguration listenerHandlersConfiguration) {
        invokerBuilder = new InvokerBuilder(Assert.notNull(listenerHandlersConfiguration).getListenerHandlers());
        publisher = new Publisher(eventDispatcher, listeners);
    }

    @Override
    public void shutdown() {

    }

    /** {@inheritDoc} */
    @Override
    public void publish(@Nonnull final Object event) {
        Assert.checkNotNull(event, "event");
        publisher.dispatch(event);
    }

    /** {@inheritDoc} */
    @Override
    public void register(@Nonnull final Object listener) {
        Assert.checkNotNull(listener, "listener");
        listeners.register(listener, invokerBuilder.build(listener));
    }

    /** {@inheritDoc} */
    @Override
    public void unregister(@Nonnull final Object listener) {
        Assert.notNull(listener);
        listeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterAll() {
        listeners.clear();
    }

    /**
     * Maps classes to the relevant {@link Invokers}.
     */
    private static final class Listeners {

        /**
         * We always want an {@link Invokers} created for any class requested, even if it is empty.
         */
        private final ConcurrentMap<Class<?>, Invokers> invokers = CacheBuilder.newBuilder()
                .<Class<?>, Invokers> build(new CacheLoader<Class<?>, Invokers>() {

                    @Override
                    public Invokers load(final Class<?> key) throws Exception {
                        return new Invokers();
                    }

                })
                .asMap();

        public void register(final Object listener, final Iterable<IListenerInvoker> invokers) {
            for (final IListenerInvoker invoker : invokers) {
                register(listener, invoker);
            }
        }

        private void register(final Object listener, final IListenerInvoker invoker) {
            // if supported classes is empty, then all events are supported.
            if (invoker.getSupportedEventTypes().isEmpty()) {
                invokers.get(Object.class).add(listener, invoker);
            } else {
                // if it it empty, we won't loop, otherwise register the invoker against all its classes
                for (final Class<?> eventClass : invoker.getSupportedEventTypes()) {
                    invokers.get(eventClass).add(listener, invoker);
                }
            }
        }

        public void remove(final Object listener) {
            for (final Invokers entry : invokers.values()) {
                entry.remove(listener);
            }
        }

        public void clear() {
            invokers.clear();
        }

        public Iterable<IListenerInvoker> get(final Class<?> eventClass) {
            return invokers.get(eventClass).all();
        }
    }

    /**
     * map of Key to Set of ListenerInvoker.
     */
    private static final class Invokers {

        /** */
        private final ConcurrentMap<Object, IListenerInvoker> listeners = new MapMaker().weakKeys().makeMap();

        public Iterable<IListenerInvoker> all() {
            return listeners.values();
        }

        public void remove(final Object key) {
            listeners.remove(key);
        }

        public void add(final Object key, final IListenerInvoker invoker) {
            listeners.put(key, invoker);
        }
    }

    /**
     * Responsible for publishing an event.
     * <p>
     * Must first get the Set of all ListenerInvokers that are registered for that event and then use the
     * {@link IEventDispatcher} to send the event to them.
     */
    private static final class Publisher {

        /** logger. */
        private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

        /** */
        private final Listeners listeners;

        /** */
        private final IEventDispatcher dispatcher;

        /**
         * transform an event class into the relevant invokers.
         */
        private final Function<Class<?>, Iterable<IListenerInvoker>> toListnerInvoker = //
        new Function<Class<?>, Iterable<IListenerInvoker>>() {

            @Override
            public Iterable<IListenerInvoker> apply(final Class<?> eventClass) {
                return listeners.get(eventClass);
            }
        };

        Publisher(final IEventDispatcher dispatcher, final Listeners listeners) {
            this.dispatcher = Assert.notNull(dispatcher);
            this.listeners = Assert.notNull(listeners);
        }

        public void dispatch(final Object event) {
            for (final IListenerInvoker invoker : getInvokers(event)) {
                try {
                    dispatcher.dispatch(invoker, event);
                } catch (final Throwable t) {
                    LOGGER.error("There was an exception thrown trying to dispatch event '" + event
                            + "' from the invoker '" + invoker + "'.",
                        t);
                }
            }
        }

        /**
         * Get all classes and interfaces an object extends or implements and then find all ListenerInvokers that apply.
         *
         * @param event
         *            to find its classes/interfaces
         * @return an iterable of the invokers for those classes.
         */
        private Iterable<IListenerInvoker> getInvokers(final Object event) {
            final Set<Class<?>> allEventTypes = ClassUtils.findAllTypes(event.getClass());
            return ImmutableSet.copyOf(concat(transform(allEventTypes, toListnerInvoker)));
        }
    };

    /**
     * Holds all configured {@link ListenerHandler handlers}.
     */
    static final class InvokerBuilder {

        /** */
        private final Iterable<IListenerHandler> listenerHandlers;

        InvokerBuilder(final Iterable<IListenerHandler> listenerHandlers) {
            this.listenerHandlers = Assert.notNull(listenerHandlers);
        }

        public Iterable<IListenerInvoker> build(final Object listener) throws IllegalArgumentException {
            final ImmutableList.Builder<IListenerInvoker> builder = ImmutableList.builder();
            for (final IListenerHandler listenerHandler : listenerHandlers) {
                builder.addAll(listenerHandler.getInvokers(listener));
            }
            final List<IListenerInvoker> invokers = builder.build();
            if (invokers.isEmpty()) {
                throw new IllegalArgumentException("No listener invokers were found for listener <" + listener + ">");
            }
            return invokers;
        }
    }
}
