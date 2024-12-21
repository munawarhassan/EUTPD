package com.pmi.tpd.core.model.euceg;

import org.joda.time.DateTime;
import org.springframework.data.history.Revision;

import com.pmi.tpd.euceg.api.entity.SubmitterStatus;

import lombok.Builder;
import lombok.Data;

/**
 * @author christophe friederich
 * @since 2.4
 */
@Data
@Builder
public class SubmitterRevision {

    /** */
    private int id;

    /** */
    private int version;

    /** */
    private String submitterId;

    /** */
    private String name;

    /** */
    private SubmitterStatus status;

    /** */
    private String xmlSubmitter;

    /** */
    private String xmlSubmitterDetails;

    /** */
    private String modifiedBy;

    /** */
    private DateTime modifiedDate;

    public static SubmitterRevision fromRevision(final Revision<Integer, SubmitterEntity> from) {

        return SubmitterRevision.builder()
                .id(from.getRevisionNumber().get())
                .version(from.getEntity().getVersion())
                .submitterId(from.getEntity().getSubmitterId())
                .name(from.getEntity().getName())
                .status(from.getEntity().getStatus())
                .xmlSubmitter(from.getEntity().getXmlSubmitter())
                .xmlSubmitterDetails(from.getEntity().getXmlSubmitterDetail())
                .modifiedBy(from.getEntity().getLastModifiedBy())
                .modifiedDate(from.getEntity().getLastModifiedDate())
                .build();
    }
}
