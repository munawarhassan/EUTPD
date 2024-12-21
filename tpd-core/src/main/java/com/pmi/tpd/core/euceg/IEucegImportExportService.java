package com.pmi.tpd.core.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

public interface IEucegImportExportService {

    /**
     * Import product from excel file.
     *
     * @param excelFile
     *                       a input stream representing the excel file to import (can not be {@code null}).
     * @param productType
     *                       the type of product to import.
     * @param selectedSheet
     *                       list of spreadsheet to use during import.
     * @param sourceFileName
     *                       name of the file to import.
     * @return Returns a {@link ValidationResult} representing the result of a validation of import execution(can be
     *         empty but never {@code null}).
     */
    @Nonnull
    ValidationResult importProductFromExcel(@Nonnull InputStream excelFile,
        @Nonnull String sourceFileName,
        @Nonnull ProductType productType,
        @Nullable int[] selectedSheet,
        boolean keepSaleHistory);

    /**
     * Import submitter from excel file.
     *
     * @param excelFile
     *                  a input stream representing the excel file to import (can not be {@code null}).
     * @return Returns a {@link ValidationResult} representing the result of a validation of import execution (can be
     *         empty but never {@code null}).
     */
    @Nonnull
    ValidationResult importSubmitterFromExcel(@Nonnull InputStream excelFile);

    /**
     * Generate a list of diff patches between products in excel file and current stored products if exist.
     *
     * @param excelFile
     *                        the excel file to use.
     * @param fileProductType
     *                        the type of product.
     * @param selectedSheets
     *                        the list of excel sheets
     * @return Returns a new instance of {@link ProductDiffRequest} containing a {@link ValidationResult} if process
     *         failed or a list of {@code string} representing the diff patch of each product.
     * @throws IOException
     *                     if IO error
     * @since 2.4
     */
    @Nonnull
    ProductDiffRequest generateProductDiffFromFile(@Nonnull InputStream excelFile,
        @Nonnull ProductType fileProductType,
        int[] selectedSheets,
        final boolean keepSaleHistory) throws IOException;

    /**
     * write a zip output stream containing xml of submission and all attachments associated to.
     *
     * @param submissionId
     *                     a unique identifier of existing submission
     * @param outputStream
     *                     the output stream to write the ZIP file to.
     * @since 2.5
     */
    void writeZipSubmissionPackage(@Nonnull Long id, @Nonnull OutputStream outputStream);

    /**
     * write a zip file containing all xml representing a submission.
     *
     * @param submissionId
     *                     a unique identifier of existing submission
     * @param outputStream
     *                     the output stream to write the ZIP file to.
     */
    void writeZipSubmissionReport(@Nonnull Long submissionId, @Nonnull OutputStream outputStream);

}
