package com.pmi.tpd.euceg.backend.core.message;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author christophe Friederich
 */
@Getter
@ToString
@Builder
public class SubmitResponse implements IBackendMessage {

    private final String correlationId;

    /** */
    @Nonnull
    private final String messageId;

    /** */
    private final String errorMessage;

    public boolean isErrorMessage() {
        return !Strings.isNullOrEmpty(errorMessage);
    }

}
