package com.pmi.tpd.core.audit;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionGrantedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionModifiedEvent;

public class PermissionEventListenerTest extends AbstractAuditEventListenerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private PermissionEventListener listener;

    @Test
    public void testOnGroupGlobalPermission() throws Exception {
        listener.onGlobalPermission(new GlobalPermissionGrantedEvent(this, Permission.ADMIN, "grouprr", null));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertContains(toJson(entry.getDetails()), "ADMIN", "grouprr");
        assertEquals("Global", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.ADMIN_LOG, Channels.PERMISSION);
    }

    @Test
    public void testOnGroupGlobalPermissionModified() throws Exception {
        listener.onGlobalPermission(
            new GlobalPermissionModifiedEvent(this, Permission.USER, Permission.ADMIN, "grouprr", null));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertContains(toJson(entry.getDetails()), "USER", "ADMIN", "rr");
        assertEquals("Global", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.ADMIN_LOG, Channels.PERMISSION);
    }

    @Test
    public void testOnUserGlobalPermission() throws Exception {
        listener.onGlobalPermission(new GlobalPermissionGrantedEvent(this, Permission.ADMIN, null, mockUser("rr")));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertContains(toJson(entry.getDetails()), "ADMIN", "rr");
        assertEquals("Global", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.ADMIN_LOG, Channels.PERMISSION);
    }

    @Test
    public void testOnUserGlobalPermissionModified() throws Exception {
        listener.onGlobalPermission(
            new GlobalPermissionModifiedEvent(this, Permission.USER, Permission.ADMIN, null, mockUser("rr")));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();
        assertContains(toJson(entry.getDetails()), "USER", "ADMIN", "rr");
        assertEquals("Global", entry.getTarget());
        assertEventCommonDetails(event, Priority.HIGH, Channels.ADMIN_LOG, Channels.PERMISSION);
    }

    private String toJson(final Map<String, String> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (final JsonProcessingException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}