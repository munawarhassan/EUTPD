package com.pmi.tpd.api.exception;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates the Mail server is not configured and mail cannot be sent. Regarding logging of instances of this
 * exception, see {@link MailException}.
 *
 * @since 2.0
 */
public class NoMailHostConfigurationException extends MailException {

    /**
     *
     */
    private static final long serialVersionUID = -5679227007960807889L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public NoMailHostConfigurationException(final KeyedMessage message) {
        super(message);
    }

}
