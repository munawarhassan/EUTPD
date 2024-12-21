package com.pmi.tpd.core.euceg.stat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.euceg.api.ProductType;

public interface IEucegStatisticService {

    CountResult countSubmissionByStatus();

    CountResult countProductByPirStatus(@Nonnull ProductType productType);

    CountResult countProductBySubmissionType(@Nullable final Filters filters, String query);

    CountResult countAttachmentByStatus();

    HistogramResult getHistogramCreatedSubmission(@Nonnull final HistogramRequest request);

    HistogramResult getHistogramCreatedTobaccoProduct(@Nonnull final HistogramRequest request);

    HistogramResult getHistogramCreatedEcigProduct(@Nonnull final HistogramRequest request);

}
