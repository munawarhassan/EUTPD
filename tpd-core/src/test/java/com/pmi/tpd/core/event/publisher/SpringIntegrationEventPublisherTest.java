package com.pmi.tpd.core.event.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.TestEvent;

@ContextConfiguration(locations = { "classpath:com/pmi/tpd/core/event/publisher/beans-definitions-tests.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringIntegrationEventPublisherTest extends AbstractSpringContextTests {

    @Autowired
    private IEventPublisher eventPublisher;

    @Autowired
    private PostProcessorListener postProcessorListener;

    @Autowired
    private EventPublisherAwared eventPublisherAwared;

    @BeforeEach
    public void setUp() throws Exception {
        postProcessorListener.reset();
    }

    @Test
    public void publishEvent() throws InterruptedException {
        final TestEvent event = new TestEvent(this);
        eventPublisher.publish(event);
        assertEquals(1, postProcessorListener.getCounter());
    }

    @Test
    public void awaredListernerPublishEvent() {
        eventPublisherAwared.publishEvent();
        assertEquals(1, postProcessorListener.getCounter());
    }

}
