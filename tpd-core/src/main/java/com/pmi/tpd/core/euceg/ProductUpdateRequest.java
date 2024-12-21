package com.pmi.tpd.core.euceg;

import javax.validation.constraints.NotNull;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.Product;
import org.eu.ceg.TobaccoProduct;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ProductUpdateRequest {

    /** */
    @NotNull
    private String productNumber;

    /** */
    @NotNull
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "productType")
    @JsonSubTypes({ @Type(name = "TOBACCO", value = TobaccoProduct.class),
            @Type(name = "ECIGARETTE", value = EcigProduct.class) })
    private Product product;

    /** */
    private String previousProductNumber;

    /** */
    private String generalComment;

}
