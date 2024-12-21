package com.pmi.tpd.core.backup.impl;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public enum BackupFeatureMode {

    /** */
    BACKUP(true, false),
    /** */
    BOTH(true, true),
    /** */
    RESTORE(false, true);

    /** */
    private final boolean forBackup;

    /** */
    private final boolean forRestore;

    /**
     * @param forBackup
     * @param forRestore
     */
    private BackupFeatureMode(final boolean forBackup, final boolean forRestore) {
        this.forBackup = forBackup;
        this.forRestore = forRestore;
    }

    /**
     * @return
     */
    public boolean isForBackup() {
        return forBackup;
    }

    /**
     * @return
     */
    public boolean isForRestore() {
        return forRestore;
    }
}
