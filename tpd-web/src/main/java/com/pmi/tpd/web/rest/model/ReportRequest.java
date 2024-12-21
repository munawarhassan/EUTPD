package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.impl.NavBuilderImpl.NavBuilder;
import com.pmi.tpd.core.euceg.report.ITrackingReport;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonSerialize
@Builder
public class ReportRequest {

    private final String id;

    private final String name;

    private final String username;

    private final String type;

    private final String url;

    private final Long modified;

    public static ReportRequest from(final ITrackingReport report, final INavBuilder.Builder<NavBuilder> navBuilder) {

        return ReportRequest.builder()
                .id(report.getId())
                .name(report.getName())
                .type(report.getType())
                .username(report.getUsername())
                .modified(report.getModified())
                .url(navBuilder.buildConfigured())
                .build();
    }
}
