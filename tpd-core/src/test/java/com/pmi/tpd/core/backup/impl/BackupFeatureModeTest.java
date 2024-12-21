package com.pmi.tpd.core.backup.impl;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class BackupFeatureModeTest extends TestCase {

    @Test
    public void shouldBeAsExpected() {
        assertTrue(BackupFeatureMode.BACKUP.isForBackup());
        assertFalse(BackupFeatureMode.BACKUP.isForRestore());

        assertTrue(BackupFeatureMode.BOTH.isForBackup());
        assertTrue(BackupFeatureMode.BOTH.isForRestore());

        assertFalse(BackupFeatureMode.RESTORE.isForBackup());
        assertTrue(BackupFeatureMode.RESTORE.isForRestore());
    }
}
