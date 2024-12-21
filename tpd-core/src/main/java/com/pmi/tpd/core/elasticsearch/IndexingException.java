package com.pmi.tpd.core.elasticsearch;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.core.migration.IMigrationService;

/**
 * {@link ServiceException} specialisation for exceptions generated during migration processing performed by the
 * {@link IMigrationService migration service}.
 * <p>
 * When creating new exception types to represent specific migration processing failures, they should all be extended
 * from this base class to group them in the hierarchy.
 *
 * @author Christophe Friederich
 * @since 1.4
 */
public class IndexingException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndexingException(final KeyedMessage message) {
        super(message);
    }

    public IndexingException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
