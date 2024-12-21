package com.pmi.tpd.core.migration;

import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.restore.IRestoreState;

public interface IMigrationState extends IBackupState, IRestoreState {
}
