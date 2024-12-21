package com.pmi.tpd.core.elasticsearch;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Document(indexName = "sample-entity")
@Getter
@EqualsAndHashCode
@Builder
@ToString
@Jacksonized
public class SampleEntity {

    @Nullable
    @Id
    private final String id;

    @Nullable
    @Field(type = Text, store = true, fielddata = true)
    private final String type;

    @Nullable
    @Field(type = Text, store = true, fielddata = true)
    private final String message;

    private final int rate;

    @Nullable
    @Version
    private final Long version;

    @Nullable
    @Field(type = FieldType.Date, store = true)
    private final DateTime createdDate;
}
