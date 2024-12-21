package com.pmi.tpd.core.elasticsearch.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author christophe friederich
 * @since 2.5
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class AttachedAttachmentIndexed {

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String uuid;

}
