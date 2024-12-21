package com.pmi.tpd.euceg.backend.core.message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.activation.DataSource;
import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.ResponseMessage;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.SubmitMessage;

public final class MessageHelper {

    @Nonnull
    public static SubmitMessage createSubmitMessage(final BackendProperties backendProperties,
        final String messageId,
        final DataSource source) {

        final List<Payload> payloads = Arrays.asList(PayloadDataSource.builder()
                .fileName("payload")
                .mimeContentId("cid:message")
                .mimeType(source.getContentType())
                .name("payload")
                .content(source)
                .build());
        return SubmitMessage.builder()
                .action(backendProperties.getAction())
                .conversationId(messageId)
                .finalRecipient(backendProperties.getFinalRecipient())
                .fromPartyId(backendProperties.getFromPartyId())
                .fromPartyType(backendProperties.getPartyIdType())
                .fromRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator")
                .jmsCorrelationId(messageId)
                .messageId(messageId)
                .messageType("submitMessage")
                .originalSender(backendProperties.getOriginalSender())
                .payloads(payloads)
                .protocol("AS4")
                .refToMessageId(messageId)
                .service(backendProperties.getService())
                .serviceType(backendProperties.getServiceType())
                .toPartyId(backendProperties.getToPartyId())
                .toPartyType(backendProperties.getPartyIdType())
                .toRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder")
                .build();
    }

    @Nonnull
    public static ResponseMessage createResponseMessage(final BackendProperties backendProperties,
        final String conversationId,
        final String messageId,
        final List<DataSource> sources) {

        final List<Payload> payloads = sources.stream()
                .map(ds -> PayloadDataSource.builder()
                        .fileName("payload")
                        .mimeContentId("cid:message")
                        .mimeType(ds.getContentType())
                        .name("payload")
                        .content(ds)
                        .build())
                .collect(Collectors.toList());

        return ResponseMessage.builder()
                .action(backendProperties.getAction())
                .conversationId(conversationId)
                .finalRecipient(backendProperties.getFinalRecipient())
                .fromPartyId(backendProperties.getFromPartyId())
                .fromPartyType(backendProperties.getPartyIdType())
                .fromRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator")
                .messageId(messageId)
                .messageType("submitMessage")
                .originalSender(backendProperties.getOriginalSender())
                .payloads(payloads)
                .protocol("AS4")
                .refToMessageId(conversationId)
                .service(backendProperties.getService())
                .serviceType(backendProperties.getServiceType())
                .toPartyId(backendProperties.getToPartyId())
                .toPartyType(backendProperties.getPartyIdType())
                .toRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder")
                .build();
    }

}
