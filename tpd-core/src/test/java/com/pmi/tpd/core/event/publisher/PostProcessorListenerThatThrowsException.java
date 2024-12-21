package com.pmi.tpd.core.event.publisher;

import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.core.event.TestEvent;

public class PostProcessorListenerThatThrowsException {

    @EventListener
    public void onError(final TestEvent event) {
        throw new RuntimeException("error message");
    }

}
