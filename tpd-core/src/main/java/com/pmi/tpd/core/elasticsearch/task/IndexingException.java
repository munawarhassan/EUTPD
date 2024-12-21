package com.pmi.tpd.core.elasticsearch.task;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * {@link ServiceException} specialisation for exceptions generated during the backup <i>or</i> restore processing
 * performed by the {@link IBackupService backup service}.
 * <p>
 * When creating new exception types to represent backup <i>or</i> restore processing failures, they should all be
 * extended from this base class to group them in the hierarchy.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class IndexingException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public IndexingException(final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public IndexingException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
