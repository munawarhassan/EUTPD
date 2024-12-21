package com.pmi.tpd.core.model.euceg;

import org.joda.time.DateTime;
import org.springframework.data.history.Revision;

import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRevision {

    /** */
    private int id;

    /** */
    private int version;

    /** */
    private String productNumber;

    /** */
    private ProductStatus status;

    private ProductPirStatus pirStatus;

    /** */
    private String modifiedBy;

    /** */
    private DateTime modifiedDate;

    /** */
    private String createdBy;

    /** */
    private DateTime createdDate;

    public static ProductRevision fromRevision(final Revision<Integer, ProductEntity> from) {

        if (from == null) {
            return null;
        }

        return ProductRevision.builder()
                .id(from.getRevisionNumber().orElse(0))
                .status(from.getEntity().getStatus())
                .pirStatus(from.getEntity().getPirStatus())
                .version(from.getEntity().getVersion())
                .productNumber(from.getEntity().getProductNumber())
                .modifiedBy(from.getEntity().getLastModifiedBy())
                .modifiedDate(from.getEntity().getLastModifiedDate())
                .createdBy(from.getEntity().getCreatedBy())
                .createdDate(from.getEntity().getCreatedDate())
                .build();
    }
}
