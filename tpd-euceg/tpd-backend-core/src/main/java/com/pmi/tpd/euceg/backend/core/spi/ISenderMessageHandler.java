package com.pmi.tpd.euceg.backend.core.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageCurrentStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;

/**
 * Interface message handler.
 *
 * @author christophe friederich
 * @since 2.5
 * @param <R>
 *            the type of payload
 */
public interface ISenderMessageHandler<R> {

    /**
     * Trigger when the message status from access point has changed other than rejected.
     * 
     * @param message
     * @see TransmitStatus
     */
    void handleCurrentStatus(@Nonnull MessageCurrentStatus message);

    /**
     * Trigger to inform about the message status after sending a message to access point
     * 
     * @param message
     */
    void handleSubmitResponse(@Nonnull SubmitResponse message);

    /**
     * Trigger when the message response from responder is available in access point and has been retrieved.
     * 
     * @param message
     */
    void handleResponse(@Nonnull Response<R> message);

    /**
     * Trigger when the message status from access point is SEND_ATTEMPT_FAILED.
     * 
     * @param message
     */
    void handleMessageReceiveFailure(@Nonnull MessageReceiveFailure message);

    /**
     * Trigger when the message status from access point is SEND_FAILURE.
     * 
     * @param message
     */
    void handleMessageSendFailure(@Nonnull MessageSendFailure message);

    /**
     * Trigger to inform about the message status after access point has sent message to responder
     * 
     * @param message
     *                the reference to the sent message.
     */
    void handleMessageSent(@Nonnull MessageSent message);

}
