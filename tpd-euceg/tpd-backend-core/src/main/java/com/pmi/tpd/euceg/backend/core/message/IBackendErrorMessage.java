package com.pmi.tpd.euceg.backend.core.message;

import com.pmi.tpd.euceg.api.entity.TransmitStatus;

public interface IBackendErrorMessage extends IBackendMessage {

    String getMessageId();

    TransmitStatus getStatus();

    /** */
    String getErrorCode();

    /** */
    String getErrorDetail();
}
