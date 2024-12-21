package com.pmi.tpd.euceg.backend.core.spi;

import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;

public interface IReceiverMessageDelegate {

    void handleSubmitResponse(SubmitResponse message);

    void handleResponse(Response<?> message);

    void handleMessageReceiveFailure(MessageReceiveFailure message);

    void handleMessageSendFailure(MessageSendFailure message);

}
