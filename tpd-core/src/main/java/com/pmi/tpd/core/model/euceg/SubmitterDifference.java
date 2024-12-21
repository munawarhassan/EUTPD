package com.pmi.tpd.core.model.euceg;

import com.pmi.tpd.euceg.core.support.DiffChange;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitterDifference {

    private String submitterId;
    private Integer originalRevision;
    private Integer revisedRevision;
    private DiffChange submitterChangeType;
    private DiffChange submitterDetailsChangeType;
    private String submitterPatch;
    private String submitterDetailsPatch;
}
