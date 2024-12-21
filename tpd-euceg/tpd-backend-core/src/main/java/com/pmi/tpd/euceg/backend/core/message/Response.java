package com.pmi.tpd.euceg.backend.core.message;

import java.util.List;

import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class Response<R> implements IBackendMessage {

    private String conversationId;

    @Singular
    private List<R> responses;

}
