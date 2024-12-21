package com.pmi.tpd.core.euceg;

import java.io.OutputStream;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.core.BulkRequest;

public interface IBulkProductService {

    /**
     * Generate a Excel containing all information of requested product.
     *
     * @param stream
     *            output stream to use.
     * @param request
     *            list of product to export.
     * @since 2.5
     */
    void exportToExcel(@Nonnull OutputStream stream, @Nonnull ProductType productType, @Nonnull BulkRequest request);

    /**
     * Create and send submission using a {@link BulkRequest requests}.
     *
     * @param requests
     *            list of request used to create and/or send submission.
     */
    void bulkSend(@Nonnull final ProductType productType, @Nonnull final BulkRequest request);
}
