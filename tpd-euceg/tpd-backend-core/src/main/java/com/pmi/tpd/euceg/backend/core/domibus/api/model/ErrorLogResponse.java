package com.pmi.tpd.euceg.backend.core.domibus.api.model;

import java.util.List;

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
@JsonDeserialize(builder = ErrorLogResponse.ErrorLogResponseBuilder.class)
@JsonSerialize
public class ErrorLogResponse {

    private List<ErrorLog> errorLogEntries;

    private int pageSize;

    private int page;

    private long count;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorLogResponseBuilder {

    }
}