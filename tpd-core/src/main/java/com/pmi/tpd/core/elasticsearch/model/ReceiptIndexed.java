package com.pmi.tpd.core.elasticsearch.model;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.google.common.collect.Lists;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@Getter
public class ReceiptIndexed {

    /** */
    private Long id;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String messageId;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String responseMessageId;

    /** */
    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String name;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private PayloadType payloadType;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private TransmitStatus transmitStatus;

    /** */
    private boolean error;

    public static List<ReceiptIndexed> map(@Nonnull final List<? extends ITransmitReceiptEntity> entities) {
        return Lists.transform(entities, ReceiptIndexed::from);
    }

    public static ReceiptIndexed from(@Nonnull final ITransmitReceiptEntity entity) {
        checkNotNull(entity, "entity");
        final ReceiptIndexed index = new ReceiptIndexed();
        index.id = entity.getId();
        index.error = entity.isError();
        index.messageId = entity.getMessageId();
        index.name = entity.getName();
        index.responseMessageId = entity.getResponseMessageId();
        index.transmitStatus = entity.getTransmitStatus();
        index.payloadType = entity.getType();
        return index;
    }

    public Long getId() {
        return id;
    }

}
