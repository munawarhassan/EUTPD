package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import java.util.List;

import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.Payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Getter
@ToString
@Builder
public class IncomingMessage implements IBackendMessage {

    /** */
    private String messageType;

    /** */
    private String messageId;

    /** */
    private String action;

    /** */
    private String conversationId;

    /** */
    private String fromPartyId;

    /** */
    private String fromRole;

    /** */
    private String fromPartyType;

    /** */
    private String toPartyId;

    /** */
    private String toRole;

    /** */
    private String toPartyType;

    /** */
    private String originalSender;

    /** */
    private String finalRecipient;

    /** */
    private String finalRecipienttype;

    /** */
    private String service;

    /** */
    private String serviceType;

    /** */
    private String protocol;

    /** */
    private String refToMessageId;

    /** */
    private String agreementRef;

    /** */
    private String agreementRefType;

    /** */
    @Singular
    private List<Payload> payloads;

}
