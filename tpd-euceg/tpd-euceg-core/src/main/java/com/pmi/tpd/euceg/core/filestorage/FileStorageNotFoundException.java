package com.pmi.tpd.euceg.core.filestorage;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.euceg.api.EucegException;

/**
 * Thrown by the {@link com.pmi.tpd.euceg.core.filestorage.IFileStorage} when an attachment does not exist.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class FileStorageNotFoundException extends EucegException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FileStorageNotFoundException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }

    public FileStorageNotFoundException(final KeyedMessage message) {
        super(message);
    }

}
