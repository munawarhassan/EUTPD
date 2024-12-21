package com.pmi.tpd.core.event.publisher;

import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.core.event.TestEvent;

public class PostProcessorListener {

    private int counter = 0;

    @EventListener
    public void onEvent(final TestEvent event) {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public void reset() {
        counter = 0;

    }
}
