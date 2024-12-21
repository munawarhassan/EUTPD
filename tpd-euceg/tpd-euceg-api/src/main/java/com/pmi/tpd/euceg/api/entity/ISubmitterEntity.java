package com.pmi.tpd.euceg.api.entity;

import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;

public interface ISubmitterEntity extends IInitializable, IIdentityEntity<String>, IAuditEntity {

    /**
     * @return Returns the current version of attachment.
     * @since 2.4
     */
    int getVersion();

    String getSubmitterId();

    String getName();

    SubmitterStatus getStatus();

    String getXmlSubmitter();

    String getXmlSubmitterDetail();

    SubmitterDetails getSubmitterDetails();

    Submitter getSubmitter();

}