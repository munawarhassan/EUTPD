package com.pmi.tpd.core.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.AuthenticationException;

import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.core.event.auth.AuthenticationFailureEvent;
import com.pmi.tpd.core.event.auth.AuthenticationSuccessEvent;
import com.pmi.tpd.core.user.IUserService;

public class AuthenticationEventListenerTest extends AbstractAuditEventListenerTest {

    private static final String AUTH_METHOD = "magic";

    private static final String EXCEPTION_MESSAGE = "something went horribly wrong!";

    @Mock(lenient = true)
    private AuthenticationException exception;

    @Mock(lenient = true)
    private IUser user;

    @Mock(lenient = true)
    private IUserService userService;

    @InjectMocks
    private AuthenticationEventListener listener;

    @Override
    @BeforeEach
    public void setup() {
        when(userService.getUserByName(USERNAME)).thenReturn(user);
        when(user.getName()).thenReturn(USERNAME);
        when(exception.getLocalizedMessage()).thenReturn(EXCEPTION_MESSAGE);
    }

    @Test
    public void testOnAuthenticationFailure() throws Exception {
        listener.onAuthenticationFailure(new AuthenticationFailureEvent(this, USERNAME, AUTH_METHOD, exception));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertEquals(entry.getDetails().get("error"), EXCEPTION_MESSAGE);
        assertEquals(entry.getDetails().get("authentication-method"), AUTH_METHOD);
        assertEquals(USERNAME, entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.AUTHENTICATION);
    }

    @Test
    public void testOnAuthenticationFailureNoException() throws Exception {
        listener.onAuthenticationFailure(new AuthenticationFailureEvent(this, null, AUTH_METHOD, null));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertEquals(entry.getDetails().get("authentication-method"), AUTH_METHOD);
        assertEquals("<anonymous>", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.AUTHENTICATION);
    }

    @Test
    public void testOnAuthenticationFailureNoUserForUsername() throws Exception {
        listener.onAuthenticationFailure(new AuthenticationFailureEvent(this, "no-such-user", AUTH_METHOD, exception));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertEquals(entry.getDetails().get("error"), EXCEPTION_MESSAGE);
        assertEquals(entry.getDetails().get("authentication-method"), AUTH_METHOD);
        assertEquals("no-such-user", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.AUTHENTICATION);
    }

    @Test
    public void testOnAuthenticationFailureNoUsername() throws Exception {
        listener.onAuthenticationFailure(new AuthenticationFailureEvent(this, null, AUTH_METHOD, exception));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertEquals(entry.getDetails().get("error"), EXCEPTION_MESSAGE);
        assertEquals(entry.getDetails().get("authentication-method"), AUTH_METHOD);
        assertEquals("<anonymous>", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.AUTHENTICATION);
    }

    @Test
    public void testOnAuthenticationSuccess() throws Exception {
        listener.onAuthenticationSuccess(new AuthenticationSuccessEvent(this, USERNAME, AUTH_METHOD));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertContains(entry.getDetails().get("authentication-method"), AUTH_METHOD);
        assertEquals(USERNAME, entry.getTarget());
        assertEventCommonDetails(event, Priority.LOW, Channels.AUTHENTICATION);
    }
}
