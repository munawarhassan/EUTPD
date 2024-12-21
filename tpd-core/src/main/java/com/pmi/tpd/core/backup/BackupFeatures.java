package com.pmi.tpd.core.backup;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.core.backup.impl.BackupFeatureMode;
import com.pmi.tpd.core.backup.impl.SimpleBackupFeature;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class BackupFeatures {

    /**
     *
     */
    private static final List<IBackupFeature> BACKUP_FEATURES = ImmutableList.<IBackupFeature> builder()
            .add(new SimpleBackupFeature("core", "backup-support", BackupFeatureMode.BACKUP))
            // indicates the clients must use application/json as the mime type for certain POST requests
            // that need to be protected from XSRF (but can't use the built-in support for this)
            .add(new SimpleBackupFeature("web", "json", BackupFeatureMode.BACKUP))
            .add(new SimpleBackupFeature("forks", "alternates", BackupFeatureMode.BOTH))
            .build();

    /**
     * private constructor
     */
    private BackupFeatures() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     */
    public static List<IBackupFeature> getFeatures() {
        return BACKUP_FEATURES;
    }
}
