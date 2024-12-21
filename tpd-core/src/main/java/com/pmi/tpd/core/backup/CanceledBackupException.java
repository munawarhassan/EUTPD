package com.pmi.tpd.core.backup;

import com.pmi.tpd.api.exec.IRunnableTaskCanceled;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class CanceledBackupException extends BackupException implements IRunnableTaskCanceled {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CanceledBackupException(final KeyedMessage message) {
        super(message);
    }
}
