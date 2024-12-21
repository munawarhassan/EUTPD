package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.audit.IAuditEntry;

/**
 * Service that logs {@link IAuditEntry audit entries} to the audit log
 * 
 * @author Christophe Friederich
 * @since 2.4
 */
public interface IAuditEntryLoggingService {

    /**
     * Appends {@link IAuditEntry audit entries} to the audit log
     *
     * @param entry
     *            the entry to append to the log
     */
    void log(@Nonnull IAuditEntry entry);
}
