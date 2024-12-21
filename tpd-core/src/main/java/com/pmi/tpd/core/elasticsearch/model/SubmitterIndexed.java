package com.pmi.tpd.core.elasticsearch.model;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import org.eu.ceg.SubmitterDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@Document(indexName = "submitters_v2")
@Setting(settingPath = "elasticsearch-settings.json", shards = 1, replicas = 0)
@Getter
public class SubmitterIndexed extends AuditEntityIndexed implements IIdentityEntity<String> {

    /** */
    @Id
    private String id;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String submitterId;

    /** */
    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String name;

    @Field(type = FieldType.Keyword, store = true)
    private SubmitterStatus status;

    /** */
    @Field(type = FieldType.Boolean, store = true)
    private boolean sme;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String vat;

    /** */
    @Field(type = FieldType.Text, store = true)
    private String address;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String country;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String phone;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String email;

    public static Page<SubmitterIndexed> map(final Page<SubmitterEntity> page) {
        return page.map(SubmitterIndexed::from);
    }

    public static SubmitterIndexed from(@Nonnull final SubmitterEntity entity) {
        checkNotNull(entity, "entity");
        final SubmitterDetails details = checkNotNull(entity.getSubmitterDetails(), "submitterDetails");
        final SubmitterIndexed request = new SubmitterIndexed();
        request.id = entity.getId();
        request.submitterId = entity.getId();

        request.submitterId = entity.getSubmitterId();
        request.name = details.getName();
        request.status = entity.getStatus();
        request.vat = details.getVatNumber();
        request.address = details.getAddress();
        request.country = details.getCountry().value();
        request.phone = details.getPhoneNumber();
        request.email = details.getEmail();
        request.audit(entity);
        return request;
    }

}
