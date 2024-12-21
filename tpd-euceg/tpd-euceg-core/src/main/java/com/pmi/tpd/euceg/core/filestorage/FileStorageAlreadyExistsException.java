package com.pmi.tpd.euceg.core.filestorage;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.euceg.api.EucegException;

/**
 * Thrown by the {@link com.pmi.tpd.euceg.core.filestorage.IFileStorage} when an attachment already exists.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class FileStorageAlreadyExistsException extends EucegException {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    public FileStorageAlreadyExistsException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }

    public FileStorageAlreadyExistsException(final KeyedMessage message) {
        super(message);
    }

}
