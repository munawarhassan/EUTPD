package com.pmi.tpd.core.audit;

import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.request.IRequestManager;

public abstract class AbstractAuditEventListenerTest extends MockitoTestCase {

    protected static final String USERNAME = "USERNAME";

    @Mock
    private IAuthenticationContext authenticationContext;

    @Captor
    private ArgumentCaptor<AuditEvent> eventCaptor;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IAuditEntryLoggingService loggingService;

    @Mock
    private IRequestManager requestManager;

    @BeforeEach
    public void setup() {
        doReturn(Optional.of(mockUser(USERNAME))).when(authenticationContext).getCurrentUser();
    }

    protected static IUser mockUser(final String name) {
        final IUser user = mock(IUser.class, withSettings().lenient());
        when(user.getName()).thenReturn(name);
        return user;
    }

    protected void assertEventCommonDetails(final AuditEvent event, final Priority priority, final String... channels) {
        assertEquals(priority, event.getPriority());
        if (channels.length == 0) {
            assertTrue(event.getChannels().isEmpty());
        } else {
            assertThat(event.getChannels(), Matchers.containsInAnyOrder(channels));
        }
    }

    protected AuditEvent getAuditEvent() {
        verify(eventPublisher).publish(eventCaptor.capture());

        return eventCaptor.getValue();
    }

    protected void assertNoAuditEvent() {
        verify(eventPublisher, never()).publish(any());
    }
}
