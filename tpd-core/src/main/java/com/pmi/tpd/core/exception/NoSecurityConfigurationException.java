package com.pmi.tpd.core.exception;

import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates the Mail server is not configured and mail cannot be sent. Regarding logging of instances of this
 * exception, see {@link MailException}.
 *
 * @since 2.0
 */
public class NoSecurityConfigurationException extends SecurityException {

    /**
     *
     */
    private static final long serialVersionUID = -5679227007960807889L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public NoSecurityConfigurationException(final KeyedMessage message) {
        super(message);
    }

}
