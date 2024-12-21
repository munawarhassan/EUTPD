package com.pmi.tpd.euceg.backend.core.domibus.api.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@JsonDeserialize(builder = MessageLog.MessageLogBuilder.class)
@JsonSerialize
public class MessageLog {

    private String messageId;

    private String mshRole;

    private String conversationId;

    private String messageType;

    private String messageStatus;

    private String notificationStatus;

    private String fromPartyId;

    private String toPartyId;

    private String originalSender;

    private String finalRecipient;

    private String refToMessageId;

    private Date received;

    private Date deleted;

    private int sendAttempts;

    private int sendAttemptsMax;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageLogBuilder {

    }

}