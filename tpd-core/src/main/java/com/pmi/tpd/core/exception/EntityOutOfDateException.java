package com.pmi.tpd.core.exception;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**

 */
public class EntityOutOfDateException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN_VERSION = -1;

    private final int currentVersion;

    private final int expectedVersion;

    public EntityOutOfDateException(final KeyedMessage message, final Throwable cause) {
        this(message, cause, UNKNOWN_VERSION, UNKNOWN_VERSION);
    }

    public EntityOutOfDateException(final KeyedMessage message, final int expectedVersion, final int currentVersion) {
        super(message);

        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    public EntityOutOfDateException(final KeyedMessage message, final Throwable cause, final int expectedVersion,
            final int currentVersion) {
        super(message, cause);

        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public int getExpectedVersion() {
        return expectedVersion;
    }
}
