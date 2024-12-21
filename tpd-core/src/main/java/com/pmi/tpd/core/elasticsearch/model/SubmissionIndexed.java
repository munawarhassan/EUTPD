package com.pmi.tpd.core.elasticsearch.model;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.core.elasticsearch.parser.ProductXmlParser;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@Document(indexName = "submissions_v1")
@Setting(settingPath = "elasticsearch-settings.json", shards = 1, replicas = 0)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class SubmissionIndexed extends AuditEntityIndexed implements IIdentityEntity<Long> {

    /** */
    @Id
    private Long id;

    @Field(type = FieldType.Keyword, store = true)
    private Long submissionId;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String productId;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String internalProductNumber;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private ProductType type;

    /** */

    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String productNumber;

    /** */
    @Field(type = FieldType.Integer, store = true)
    private int productType;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String productTypeName;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String previousProductId;

    /** */
    @Field(type = FieldType.Integer, store = true)
    private int submissionType;

    @Field(type = FieldType.Keyword, store = true)
    private String submissionTypeName;

    @Field(type = FieldType.Keyword, store = true)
    private SendSubmissionType sendType;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private SubmissionStatus submissionStatus;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private ProductPirStatus pirStatus;

    /** */
    @Field(type = FieldType.Boolean, store = false)
    private boolean latest;

    /** */
    @Field(type = FieldType.Boolean, store = false)
    private boolean latestSubmitted;

    /** */
    @Field(type = FieldType.Nested)
    private List<ReceiptIndexed> receipts;

    /** */
    @Field(type = FieldType.Nested)
    private List<PresentationIndexed> presentations;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String sentBy;

    public static List<SubmissionIndexed> map(@Nonnull final List<ISubmissionEntity> entities) {
        return Lists.transform(entities, SubmissionIndexed::from);
    }

    public static Page<SubmissionIndexed> map(@Nonnull final Page<ISubmissionEntity> page) {
        return page.map(SubmissionIndexed::from);
    }

    public static SubmissionIndexed from(@Nonnull final ISubmissionEntity entity) {
        checkNotNull(entity, "entity");
        final SubmissionIndexed.SubmissionIndexedBuilder indexedSubmission = SubmissionIndexed.builder();

        indexedSubmission.id = entity.getId();
        indexedSubmission.submissionId = entity.getId();
        indexedSubmission.productId = entity.getProductId();
        if (entity.getSubmissionType() != null) {
            indexedSubmission.submissionType = entity.getSubmissionType().value();
            indexedSubmission.submissionTypeName = SubmissionTypeEnum.fromValue(indexedSubmission.submissionType)
                    .map(SubmissionTypeEnum::getName)
                    .orElse(null);
        }
        indexedSubmission.submissionStatus = entity.getSubmissionStatus();
        indexedSubmission.sendType = entity.getSendType();
        indexedSubmission.type = entity.getProductType();
        indexedSubmission.receipts = ReceiptIndexed.map(entity.getReceipts());

        final IProductEntity productEntity = entity.getProduct();
        indexedSubmission.productNumber = productEntity.getProductNumber();
        indexedSubmission.pirStatus = entity.getPirStatus();

        try (Reader in = new StringReader(entity.getXmlSubmission())) {
            final ProductIndexed indexedProduct = ProductXmlParser.parse(in, entity.getProductType()).build();

            indexedSubmission.productType = indexedProduct.getType();
            indexedSubmission.productTypeName = indexedProduct.getTypeName();
            indexedSubmission.previousProductId = indexedProduct.getPreviousProductId();
            indexedSubmission.presentations(indexedProduct.getPresentations());
        } catch (final IOException | XMLStreamException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        indexedSubmission.internalProductNumber(entity.getInternalProductNumber());
        indexedSubmission.latest(entity.isLatest());
        indexedSubmission.latestSubmitted(entity.isLatestSubmitted());

        indexedSubmission.sentBy = entity.getSentBy() != null ? entity.getSentBy() : entity.getLastModifiedBy();

        final SubmissionIndexed s = indexedSubmission.build();
        s.audit(entity);
        return s;
    }

    /**
     * @return
     */
    @JsonIgnore
    public float getProgress() {
        final int size = getReceipts().size();
        final MutableInt pending = new MutableInt();
        FluentIterable.from(getReceipts()).forEach(input -> {
            if (TransmitStatus.PENDING.equals(input.getTransmitStatus())
                    || TransmitStatus.AWAITING.equals(input.getTransmitStatus())) {
                pending.increment();
            }

        });
        if (size == 0) {
            return 0.0f;
        }
        return (size - pending.floatValue()) / size;
    }

    @JsonIgnore
    public boolean isCancelable() {
        return submissionStatus.cancelable();
    }

    @JsonIgnore
    public boolean isExportable() {
        return submissionStatus.exportable();
    }

}
