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
@JsonDeserialize(builder = ErrorLog.ErrorLogBuilder.class)
@JsonSerialize
public class ErrorLog {

    private String errorCode;

    private String errorDetail;

    private String errorSignalMessageId;

    private String messageInErrorId;

    private String mshRole;

    private Date notified;

    private Date timestamp;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorLogBuilder {

    }

}