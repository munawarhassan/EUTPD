package com.pmi.tpd.euceg.core.exporter.submission;

import java.io.Reader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.Submission;
import org.joda.time.DateTime;

import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.core.exporter.BaseExcelExporter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter()
@ToString
@Accessors(fluent = true)
@Slf4j
public class BaseRequestExportSubmission {

    /** */
    private String productNumber;

    /** */
    private String internalProductNumber;

    /** */
    private ProductType productCategory;

    /** */
    private String productCategoryLabel;

    /** */
    private String productType;

    /** */
    private String productId;

    /** */
    private String previousProductId;

    /** */
    private String submissionType;

    /** */
    private String xmlSubmission;

    /** */
    @Nullable
    private Submission submission;

    /** */
    private String status;

    /** */
    private String pirStatus;

    /** */
    private String latestPirStatus;

    /** */
    private ProductStatus productStatus;

    /** */
    private DateTime lastModifiedDate;

    /** */
    private String lastModifiedBy;

    /** */
    private DateTime createdDate;

    /** */
    private String createdBy;

    /** */
    private boolean lastSubmission;

    /** */
    private boolean lastSubmittedSubmission;

    /** */
    private String sentBy;

    public void parse(final BaseExcelExporter<?> context) {

        try (Reader in = Eucegs.openReader(xmlSubmission())) {

            parseSubmission(context, in);

        } catch (final Exception e) {
            LOGGER.error("Error parsing xml submission {}", productNumber());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void parseSubmission(final BaseExcelExporter<?> context, @Nonnull final Reader xmlProduct)
            throws Exception {

    }

}
