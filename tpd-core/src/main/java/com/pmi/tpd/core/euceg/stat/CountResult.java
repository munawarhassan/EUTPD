package com.pmi.tpd.core.euceg.stat;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CountResult {

    /** */
    private long count;

    @Singular("partition")
    private Map<String, Long> partitions;
}