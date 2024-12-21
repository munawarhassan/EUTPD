package com.pmi.tpd.euceg.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pmi.tpd.api.paging.Filter.Operator;
import com.pmi.tpd.api.paging.Filters;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 */
@Getter
@Builder
@Jacksonized
@ToString
public class BulkRequest {

    public enum BulkAction {
        exportExcel,
        sendSubmission,
        createSubmission;
    }

    @Nonnull
    private final BulkAction action;

    /** */
    @Nonnull
    private final Map<String, List<Filter>> filters;

    @Nullable
    private final Map<String, String> data;

    public Filters getPagingFilters() {
        return new Filters(filters.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(f -> new com.pmi.tpd.api.paging.Filter<String>(entry.getKey(), f.getValues(),
                                f.getOperator())))
                .collect(Collectors.toList()));
    }

    @Getter
    @Builder
    @Jacksonized
    @ToString
    public static class Filter {

        @JsonProperty("operator")
        @JsonAlias("op")
        private final Operator operator;

        private final List<String> values;
    }

}
