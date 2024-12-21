package com.pmi.tpd.core.audit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.LoggingConstants;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.testing.junit5.TestCase;

public class DefaultAuditEntryLoggingServiceTest extends TestCase {

    public static final String SOURCE_IP = "172.0.0.0";

    private DefaultAuditEntryLoggingService service;

    @BeforeEach
    public void setup() {
        service = new DefaultAuditEntryLoggingService(new ObjectMapper(), 100);
        MDC.put(LoggingConstants.MDC_REQUEST_ID, "requestid");
        MDC.put(LoggingConstants.MDC_SESSION_ID, "sessionid");
    }

    @AfterEach
    public void tearDown() {
        MDC.remove(LoggingConstants.MDC_REQUEST_ID);
        MDC.remove(LoggingConstants.MDC_SESSION_ID);
    }

    @Test
    public void testLog() throws Exception {
        final IAuditEntry event = new AuditEntryBuilder().action("action")
                .details(Collections.emptyMap())
                .target("target")
                .timestamp(new Date())
                .user(mockUser("tbright"))
                .build();
        service.log(event);
    }

    @Test
    public void testMessageCreationContent() throws Exception {
        final Date timestamp = new Date();
        final IAuditEntry event = new AuditEntryBuilder().action("AnAction")
                .details(ImmutableMap.of("details", "message"))
                .sourceIpAddress(SOURCE_IP)
                .target("target")
                .timestamp(timestamp)
                .user(mockUser("tbright"))
                .build();

        final String message = service.getLogMessage(event);
        assertTrue(message.contains("tbright"));
        assertTrue(message.contains("AnAction"));
        assertTrue(message.contains("target"));
        assertTrue(message.contains(SOURCE_IP));
        assertTrue(message.contains("details"));
        assertTrue(message.contains("" + timestamp.getTime()));
        assertTrue(message.contains("requestid"));
        assertTrue(message.contains("sessionid"));
    }

    @Test
    public void testMessageCreationStructure() throws Exception {
        final Date timestamp = new Date();
        final IAuditEntry event = new AuditEntryBuilder().action("AnAction")
                .details(ImmutableMap.of("details", "message"))
                .sourceIpAddress(SOURCE_IP)
                .target("target")
                .timestamp(timestamp)
                .user(mockUser("tbright"))
                .build();

        final String message = service.getLogMessage(event);
        assertEquals(
            "172.0.0.0 | AnAction | tbright | " + timestamp.getTime()
                    + " | target | {\"details\":\"message\"} | requestid | sessionid",
            message);
    }

    @Test
    public void testMessageCreationMaxLength() throws Exception {
        final String message = createMessage("tbright", ImmutableMap.of("details", StringUtils.repeat("a", 2000)));
        assertTrue(message.endsWith("aaa... | requestid | sessionid"));
        assertTrue(message.length() < 200);
    }

    @Test
    public void testNewLinesEscaped() throws Exception {
        assertFalse(createMessage("\n").contains("\n"));
    }

    @Test
    public void testNewLinesEscapedNonEndOfLine() throws Exception {
        assertFalse(createMessage("\nabcde").contains("\n"));
    }

    @Test
    public void testNewLinesEscapedNonEndOfLineMultiple() throws Exception {
        assertFalse(createMessage("\nabcde\n\n\n\n\n").contains("\n"));
    }

    @Test
    public void testNewLinesReturnEscapedNonEndOfLine() throws Exception {
        assertFalse(createMessage("\rabcde").contains("\r"));
    }

    @Test
    public void testSpecialCharactersEscaped() throws Exception {
        assertTrue(createMessage(" | ").contains(" PIPE_CHAR "));
    }

    @Test
    public void testLogNull() throws Exception {
        assertThrows(NullPointerException.class, () -> service.log(null));
    }

    private static IUser mockUser(final String username) {
        final IUser user = mock(IUser.class);
        when(user.getName()).thenReturn(username);

        return user;
    }

    private String createMessage(final String username) {
        return createMessage(username, ImmutableMap.of("details", "message"));
    }

    private String createMessage(final String username, final Map<String, String> details) {
        final IAuditEntry event = new AuditEntryBuilder().action("AnAction")
                .details(details)
                .sourceIpAddress(SOURCE_IP)
                .target("target")
                .timestamp(new Date())
                .user(mockUser(username))
                .build();
        return service.getLogMessage(event);
    }
}
