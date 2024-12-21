package com.pmi.tpd.euceg.backend.core.delivery;

import com.pmi.tpd.euceg.backend.core.message.IBackendErrorMessage;

public class RejectedMessageException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final IBackendErrorMessage failure;

    public RejectedMessageException(final IBackendErrorMessage message) {
        this.failure = message;
    }

    public IBackendErrorMessage getFailureMessage() {
        return failure;
    }
}
