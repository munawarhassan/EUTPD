package com.pmi.tpd.core.audit;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.permission.GlobalPermissionEvent;
import com.pmi.tpd.core.event.permission.IPermissionModifiedEvent;
import com.pmi.tpd.core.event.permission.IPermissionRequestedEvent;
import com.pmi.tpd.core.user.permission.PermissionEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
public class PermissionEventListener extends AbstractAuditEventListener {

    /** */
    static final String TARGET_GLOBAL = "Global";

    @Autowired
    public PermissionEventListener(final IAuditEntryLoggingService auditLoggingService,
            final IRequestManager requestManager, final IAuthenticationContext authContext,
            final IEventPublisher eventPublisher) {
        super(auditLoggingService, requestManager, authContext, eventPublisher);
    }

    @EventListener
    public void onGlobalPermission(final GlobalPermissionEvent event) throws Exception {
        // exclude requested event
        if (event instanceof IPermissionRequestedEvent) {
            return;
        }
        final IAuditEntry auditEntry = createPermissionAuditEntryBuilder(event).target(TARGET_GLOBAL).build();
        publish(event, auditEntry, Sets.newHashSet(Channels.ADMIN_LOG, Channels.PERMISSION), Priority.HIGH);
    }

    private AuditEntryBuilder createPermissionAuditEntryBuilder(final PermissionEvent event) throws IOException {
        final ImmutableMap.Builder<String, String> builder = getMapBuilder();
        if (event instanceof IPermissionModifiedEvent) {
            final IPermissionModifiedEvent modifiedEvent = (IPermissionModifiedEvent) event;
            builder.put("oldPermission", modifiedEvent.getOldValue().toString());
            builder.put("newPermission", modifiedEvent.getNewValue().toString());
        } else {
            builder.put("permission", event.getPermission().name());
        }
        if (event.getAffectedUser() == null && event.getAffectedGroup() == null) {
            builder.put("global", "true");
        } else {
            if (event.getAffectedUser() != null) {
                builder.put("affectedUser", event.getAffectedUser().getName());
            }
            if (event.getAffectedGroup() != null) {
                builder.put("affectedGroup", event.getAffectedGroup());
            }
        }
        return createAuditEntryBuilder(event).details(builder.build());
    }

}
