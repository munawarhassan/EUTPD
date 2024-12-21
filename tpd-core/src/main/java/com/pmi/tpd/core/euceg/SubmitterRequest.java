package com.pmi.tpd.core.euceg;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
@Schema(name = "Submitter", description = "Contains information of submitter")
public class SubmitterRequest implements IIdentityEntity<String> {

    @NotNull
    @Size(max = 10)
    private String submitterId;

    /** */
    @NotNull
    @Size(max = 250)
    private String name;

    /** */
    private SubmitterDetails details;

    /** */
    private Submitter submitter;

    /** */
    private String createdBy;

    /** */
    private DateTime createdDate;

    /** */
    private String lastModifiedBy;

    /** */
    private DateTime lastModifiedDate;

    @Override
    public String getId() {
        return this.submitterId;
    }

    @Nonnull
    public static SubmitterRequest from(final SubmitterEntity entity) {
        return builder().submitterId(entity.getSubmitterId())
                .name(entity.getName())
                .details(entity.getSubmitterDetails())
                .submitter(entity.getSubmitter())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmitterRequestBuilder {

    }

}
