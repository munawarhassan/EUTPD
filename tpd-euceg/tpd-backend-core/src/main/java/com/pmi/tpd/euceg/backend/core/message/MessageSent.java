package com.pmi.tpd.euceg.backend.core.message;

import javax.annotation.Nonnull;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class MessageSent implements IBackendMessage {

    private final String correlationId;

    @Nonnull
    private final String messageId;
}
