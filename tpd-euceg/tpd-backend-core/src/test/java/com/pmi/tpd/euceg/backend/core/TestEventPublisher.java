package com.pmi.tpd.euceg.backend.core;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;

public class TestEventPublisher<T> implements IEventPublisher {

    private final List<T> publishedEvents = Lists.newArrayList();

    @Override
    public void shutdown() {

    }

    public void clear() {
        this.publishedEvents.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publish(final Object event) {
        publishedEvents.add((T) event);
    }

    @Override
    public void register(final Object listener) {
    }

    @Override
    public void unregister(final Object listener) {
    }

    @Override
    public void unregisterAll() {
    }

    public List<T> getPublishedEvents() {
        return publishedEvents;
    }

}