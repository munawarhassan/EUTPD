package com.pmi.tpd.core.maintenance;

/**
 * Enumerates the maintenance types supported by the system.
 * <p>
 * Ultimately this approach is fairly restrictive, but the range of supported types is currently very small. If an SPI
 * is ever constructed around maintenance, this will need to be removed and replaced with a more flexible approach.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum MaintenanceType {

    /**
     *
     */
    BACKUP,
    /**
     *
     */
    MIGRATION,
    /**
     * 
     */
    INDEXING
}
