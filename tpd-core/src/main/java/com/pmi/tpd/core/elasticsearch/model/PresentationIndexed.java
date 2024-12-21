package com.pmi.tpd.core.elasticsearch.model;

import org.joda.time.LocalDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

/**
 * @author christophe friederich
 * @since 2.5
 */
@Data
public class PresentationIndexed {

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String nationalMarket;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String nationalMarketName;

    @Field(type = FieldType.Date, store = true)
    private LocalDate withdrawalDate;

    @Field(type = FieldType.Date, store = true)
    private LocalDate launchDate;

    @Field(type = FieldType.Keyword, store = true)
    private String brandName;

    @Field(type = FieldType.Keyword, store = true)
    private String brandSubtype;

    @Field(type = FieldType.Keyword, store = true)
    private String productSubmitterNumber;
}
