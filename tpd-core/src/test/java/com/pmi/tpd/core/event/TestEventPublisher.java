package com.pmi.tpd.core.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;

/**
 * Event publisher to use in tests for checking which events were published.
 */
public class TestEventPublisher implements IEventPublisher {

    private final List<Object> publishedEvents = Lists.newArrayList();

    @Override
    public void shutdown() {

    }

    @Override
    public void publish(final Object event) {
        publishedEvents.add(event);
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

    public List<Object> getPublishedEvents() {
        return publishedEvents;
    }

}
