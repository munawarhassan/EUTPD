package com.pmi.tpd.web.rest.model;

import java.util.Locale;

import org.eu.ceg.Presentation;
import org.joda.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.core.elasticsearch.model.PresentationIndexed;

import lombok.Getter;

@Getter
@JsonSerialize
public class PresentationRequest {

    private String nationalMarket;

    private String nationalMarketName;

    private LocalDate withdrawalDate;

    public static PresentationRequest from(final Presentation presentation) {
        final PresentationRequest request = new PresentationRequest();
        if (presentation.getNationalMarket() != null && presentation.getNationalMarket().getValue() != null) {
            request.nationalMarket = presentation.getNationalMarket().getValue().value();
            final Locale locale = new Locale("en", request.nationalMarket);
            request.nationalMarketName = locale.getDisplayCountry(Locale.ENGLISH);
        }
        if (presentation.getWithdrawalDate() != null && presentation.getWithdrawalDate().getValue() != null) {
            request.withdrawalDate = presentation.getWithdrawalDate().getValue();
        }
        return request;
    }

    public static PresentationRequest from(final PresentationIndexed presentation) {
        final PresentationRequest request = new PresentationRequest();
        request.nationalMarket = presentation.getNationalMarket();
        request.nationalMarketName = presentation.getNationalMarketName();
        request.withdrawalDate = presentation.getWithdrawalDate();
        return request;
    }
}
