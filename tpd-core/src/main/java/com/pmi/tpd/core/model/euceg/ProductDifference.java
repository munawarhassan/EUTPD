package com.pmi.tpd.core.model.euceg;

import com.pmi.tpd.euceg.core.support.DiffChange;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDifference {

    private String productNumber;

    private int originalRevision;

    private int revisedRevision;

    private DiffChange changeType;

    private String patch;
}
