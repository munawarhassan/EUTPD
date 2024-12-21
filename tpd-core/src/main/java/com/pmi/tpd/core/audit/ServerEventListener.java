package com.pmi.tpd.core.audit;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.backup.event.BackupEvent;
import com.pmi.tpd.core.event.server.ApplicationConfigurationChangedEvent;
import com.pmi.tpd.core.migration.event.MigrationEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * Handles all admin activity as update configuration, maintenance activity and migration.
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class ServerEventListener extends AbstractAuditEventListener {

    static final String TARGET_SYSTEM = "System";

    @Inject
    public ServerEventListener(final IAuditEntryLoggingService auditLoggingService,
            final IRequestManager requestManager, final IAuthenticationContext authContext,
            final IEventPublisher eventPublisher) {
        super(auditLoggingService, requestManager, authContext, eventPublisher);
    }

    @EventListener
    public void onApplicationConfigurationChanged(final ApplicationConfigurationChangedEvent<String> event)
            throws Exception {
        final IAuditEntry auditEntry = createAuditEntryBuilder(event).target(event.getProperty())
                .details(toValueChangedString(event))
                .build();
        publish(event,
            auditEntry,
            Sets.newHashSet(Channels.ADMIN_LOG, Channels.APPLICATION_CONFIGURATION),
            Priority.HIGH);
    }

    @EventListener
    public void onBackup(final BackupEvent event) throws Exception {
        final IAuditEntry auditEntry = createAuditEntryBuilder(event).target(TARGET_SYSTEM).build();
        publish(event, auditEntry, Priority.HIGH);
    }

    @EventListener
    public void onMigration(final MigrationEvent event) throws Exception {
        final IAuditEntry auditEntry = createAuditEntryBuilder(event).target(TARGET_SYSTEM).build();
        publish(event, auditEntry, Priority.HIGH);
    }

    private Map<String, String> toValueChangedString(final ApplicationConfigurationChangedEvent<String> event)
            throws IOException {
        final Map<String, String> map = Maps.newHashMap();
        map.put("property", event.getProperty());
        map.put("oldValue", event.getOldValue());
        map.put("newValue", event.getNewValue());
        return map;
    }
}
