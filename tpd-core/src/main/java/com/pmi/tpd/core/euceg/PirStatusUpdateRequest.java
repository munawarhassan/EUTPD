package com.pmi.tpd.core.euceg;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@Builder
@JsonSerialize
@Jacksonized
public class PirStatusUpdateRequest {

    @NotNull
    private String productNumber;

    /** */
    @NotNull
    private ProductPirStatus newStatus;

}