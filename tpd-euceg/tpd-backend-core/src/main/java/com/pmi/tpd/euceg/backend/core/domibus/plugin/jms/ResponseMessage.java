package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import java.util.List;

import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.Payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

/**
 * @author christophe Friederich
 */
@Builder
@Getter
@ToString
public class ResponseMessage implements IBackendMessage {

    /** */
    private String messageType;

    /** */
    private String service;

    /** */
    private String serviceType;

    /** */
    private String action;

    /** */
    private String fromPartyId;

    /** */
    private String fromPartyType;

    /** */
    private String toPartyId;

    /** */
    private String toPartyType;

    /** */
    private String fromRole;

    /** */
    private String toRole;

    /** */
    private String originalSender;

    /** */
    private String finalRecipient;

    /** */
    private String protocol;

    /** */
    private String conversationId;

    /** */
    private String refToMessageId;

    /** */
    private String messageId;

    /** */
    @Singular
    private List<Payload> payloads;

}
