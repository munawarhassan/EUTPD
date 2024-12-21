package com.pmi.tpd.core.euceg;

import org.eu.ceg.AppResponse;

import com.pmi.tpd.euceg.backend.core.spi.ISenderMessageHandler;

public interface ISubmissionSenderManager
        extends ISenderMessageHandler<AppResponse>, ISendAwaitingPayloadJob, ISendDeferredSubmissionJob {

}
