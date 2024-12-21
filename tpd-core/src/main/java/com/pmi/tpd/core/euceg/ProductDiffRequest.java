package com.pmi.tpd.core.euceg;

import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.pmi.tpd.euceg.core.support.DiffChange;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * This class contains all list of diffs of product created by method
 * {@link ISubmissionService#generateProductDiffFromFile(java.io.InputStream, com.pmi.tpd.euceg.core.ProductType, String[])}.
 *
 * @author devacfr
 * @since 2.4
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class ProductDiffRequest {

    private List<ProductDiffItem> diffs;

    private ValidationResult validationResult;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductDiffRequestBuilder {

        public ProductDiffRequestBuilder merge(@Nonnull final ProductDiffRequest request) {
            if (request != null && request.diffs != null) {
                if (this.diffs == null) {
                    this.diffs = Lists.newArrayList();
                }
                this.diffs.addAll(request.diffs);
            }
            return this;
        }
    }

    @Data
    @Builder
    public static class ProductDiffItem {

        private String productNumber;

        private DiffChange change;

        private String patch;

        private ValidationResult validationResult;

    }
}
