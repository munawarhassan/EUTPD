package com.pmi.tpd.core.audit;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.core.event.server.ApplicationConfigurationChangedEvent;

public class ServerEventListenerTest extends AbstractAuditEventListenerTest {

    @InjectMocks
    private ServerEventListener listener;

    //
    // @Test
    // public void testOnApplicationSetup() throws Exception {
    // listener.onApplicationConfigurationChanged(new ApplicationSetupEvent(this, false, true));
    //
    // final AuditEvent event = getAuditEvent();
    // final IAuditEntry entry = event.getEntry();
    //
    // assertEquals(ApplicationSetupEvent.class.getSimpleName(), entry.getAction());
    // assertEquals(ApplicationConfigurationChangedEvent.Property.SERVER_IS_SETUP.toString(), entry.getTarget());
    //
    // assertConfigurationChangedDetails("false", "true", entry.getDetails());
    // assertEventCommonDetails(event, Priority.HIGH);
    // }

    @Test
    public void testOnConfiguratinApplication() throws Exception {
        listener.onApplicationConfigurationChanged(
            new ApplicationConfigurationChangedEvent<>(this, "app.done", "false", "true"));

        final AuditEvent event = getAuditEvent();
        final IAuditEntry entry = event.getEntry();

        assertEquals(ApplicationConfigurationChangedEvent.class.getSimpleName(), entry.getAction());
        assertEquals("app.done", entry.getTarget());

        assertConfigurationChangedDetails("false", "true", entry.getDetails());
        assertEventCommonDetails(event, Priority.HIGH, Channels.ADMIN_LOG, Channels.APPLICATION_CONFIGURATION);
    }

    private void assertConfigurationChangedDetails(final String expectedOld,
        final String expectedNew,
        final Map<String, String> details) {
        final String oldValue = details.get("oldValue");
        final String newValue = details.get("newValue");

        assertEquals(expectedOld, oldValue);
        assertEquals(expectedNew, newValue);
    }

}