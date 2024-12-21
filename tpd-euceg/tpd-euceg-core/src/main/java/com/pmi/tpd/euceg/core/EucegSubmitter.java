package com.pmi.tpd.euceg.core;

import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class EucegSubmitter {

    /** */
    private Submitter submitter;

    /** */
    private SubmitterDetails submitterDetails;

    /** */
    private String name;

    /** */
    private String submitterId;

}
