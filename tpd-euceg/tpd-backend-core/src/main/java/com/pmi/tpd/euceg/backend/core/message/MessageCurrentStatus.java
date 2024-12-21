package com.pmi.tpd.euceg.backend.core.message;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class MessageCurrentStatus implements IBackendMessage {

    @Nonnull
    private final String messageId;

    @Nonnull
    private final TransmitStatus status;
}
