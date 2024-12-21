package com.pmi.tpd.core.elasticsearch.model;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.xml.InvalidXmlCharacterFilterReader;
import com.pmi.tpd.core.elasticsearch.parser.ProductXmlParser;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the indexation of {@link ProductEntity} in Elastic Search.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.4
 */
@Document(indexName = "products_v1")
@Setting(settingPath = "elasticsearch-settings.json", shards = 1, replicas = 0)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Slf4j
public class ProductIndexed extends AuditEntityIndexed implements IIdentityEntity<String> {

    /** */
    @Id
    private String id;

    /** */
    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String productNumber;

    /** */
    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String child;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private ProductType productType;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String submitterId;

    /** */
    @Field(type = FieldType.Integer, store = true)
    private int type;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String typeName;

    /** */
    @Field(type = FieldType.Integer, store = true)
    private int submissionType;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String submissionTypeName;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private ProductStatus status;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private ProductPirStatus pirStatus;

    /** */
    @Field(type = FieldType.Boolean, store = true)
    private boolean readOnly;

    /** */
    @Field(type = FieldType.Boolean, store = true)
    private boolean sendable;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private SubmissionStatus latestSubmissionStatus;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String previousProductId;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String sourceFilename;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<PresentationIndexed> presentations;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<AttachedAttachmentIndexed> attachments;

    /** */
    private boolean latestError;

    /**
     * Convert a list of {@link ProductEntity} to list of {@link ProductIndexed}.
     *
     * @param entities
     *                 a list of entities.
     * @return Returns a list of {@link ProductIndexed}.
     */
    public static List<ProductIndexed> map(final List<ProductEntity> entities) {
        return Lists.transform(entities, ProductIndexed::from);
    }

    /**
     * Convert a page of {@link ProductEntity} to page of {@link ProductIndexed}.
     *
     * @param page
     *             a page.
     * @return Returns a page of {@link ProductIndexed}.
     */
    public static Page<ProductIndexed> map(final Page<ProductEntity> page) {
        return page.map(ProductIndexed::from);
    }

    public static ProductIndexed from(@Nonnull final IProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        ProductIndexed.ProductIndexedBuilder indexedProduct = ProductIndexed.builder()
                .id(entity.getId())
                .productNumber(entity.getProductNumber())
                .productType(entity.getProductType())
                .child(entity.getPreviousProductNumber())
                .status(entity.getStatus())
                .pirStatus(entity.getPirStatus())
                .readOnly(entity.isReadOnly())
                .sendable(entity.isSendable())
                .previousProductId(entity.getPreviousProductNumber())
                .submitterId(entity.getSubmitterId())
                .attachments(entity.getAttachments()
                        .stream()
                        .map(uuid -> AttachedAttachmentIndexed.builder().uuid(uuid).build())
                        .collect(Collectors.toList()))
                .sourceFilename(entity.getSourceFilename());

        if (entity.getPreferredSubmissionType() != null) {
            final int submissionType = entity.getPreferredSubmissionType().value();
            indexedProduct.submissionType(submissionType);
            indexedProduct.submissionTypeName(
                SubmissionTypeEnum.fromValue(submissionType).map(SubmissionTypeEnum::getName).orElse(null));
        }

        final ISubmissionEntity latestSubmission = entity.getLastestSubmission();
        if (latestSubmission != null) {
            indexedProduct.latestError(latestSubmission.isError());
            indexedProduct.latestSubmissionStatus(latestSubmission.getSubmissionStatus());
        }

        final String xml = entity.getXmlProduct();
        final CharSource source = ByteSource.wrap(xml.getBytes()).asCharSource(Eucegs.getDefaultCharset());
        try (Reader in = new InvalidXmlCharacterFilterReader(source.openStream())) {
            indexedProduct = ProductXmlParser.parse(in, indexedProduct, entity.getProductType());

        } catch (XMLStreamException | IOException e) {
            LOGGER.error("Error parsing xml product {}", entity.getProductNumber());
            throw new RuntimeException(e.getMessage(), e);
        }

        final ProductIndexed p = indexedProduct.build();
        p.audit(entity);
        return p;
    }

}
