package com.pmi.tpd.core.euceg;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.euceg.api.EucegException;

/**
 * Thrown by the {@link ISubmissionService} when try send attachment that is sending.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class AttachmentIsSendingException extends EucegException {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    public AttachmentIsSendingException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }

    public AttachmentIsSendingException(final KeyedMessage message) {
        super(message);
    }

}
