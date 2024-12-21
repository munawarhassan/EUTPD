package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.TestEventPublisher;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.support.SimpleSenderMessageCreator;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class JmsMessageSenderTest extends MockitoTestCase {

    @Spy
    private final TestEventPublisher<?> eventPublisher = new TestEventPublisher<>();

    @Mock
    private IApplicationProperties applicationProperties;

    @Mock
    private ISenderMessageCreator<String, String> creator;
    
    @Mock
    private IPendingMessageProvider pendingMessageProvider;

    @InjectMocks
    private JmsMessageSender<String, String> sender;

    @BeforeEach
    public void beforeEach() {
        this.eventPublisher.getPublishedEvents().clear();
    }

    @Test
    public void shouldNotRunning() {
        assertEquals(false, sender.isRunning(), "should not running");

    }

}
