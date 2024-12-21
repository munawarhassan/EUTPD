package com.pmi.tpd.euceg.core.exporter.submission;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@ToString
public class ExportOption {

    private boolean stripedRow;
}
