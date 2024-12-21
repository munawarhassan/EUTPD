package com.pmi.tpd.euceg.backend.core.message;

import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageReceiveFailure implements IBackendErrorMessage {

    /** */
    private String messageId;

    private TransmitStatus status;

    /** */
    private String errorCode;

    /** */
    private String errorDetail;

    /** */
    private String endPoint;

    /** */

}
