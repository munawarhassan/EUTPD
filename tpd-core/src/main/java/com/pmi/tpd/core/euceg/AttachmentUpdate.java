package com.pmi.tpd.core.euceg;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eu.ceg.AttachmentAction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentUpdate {

    /** */
    @NotNull
    @Size(min = 1, max = 80)
    private String attachmentId;

    /** */
    @NotNull
    @Size(min = 1, max = 250)
    private String filename;

    /** */
    private boolean confidential;

    /** */
    private AttachmentAction action;

    /** */
    private AttachmentSendStatus sendStatus;
}
