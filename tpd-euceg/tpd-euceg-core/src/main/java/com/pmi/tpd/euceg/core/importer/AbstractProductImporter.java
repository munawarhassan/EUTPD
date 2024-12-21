package com.pmi.tpd.euceg.core.importer;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Workbook;
import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.exception.InvalidArgumentException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.excel.ExcelHelper;
import com.pmi.tpd.euceg.core.excel.ExcelMapper;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.excel.UnsupportedExcelFormatException;
import com.pmi.tpd.euceg.core.util.validation.SimpleValidationFailure;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

/**
 * @author christophe friederich
 * @param <T>
 *            type of product
 */
public abstract class AbstractProductImporter<T extends Product> extends AbstractImporter<EucegProduct> {

    public AbstractProductImporter(final @Nonnull I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public @Nonnull IImporterResult<EucegProduct> importFromExcel(final @Nonnull InputStream excelFile,
        @Nullable final int[] selectedSheets) {
        checkNotNull(excelFile, "excelFile");

        @SuppressWarnings("null")
        final @Nonnull List<EucegProduct> products = Lists.newArrayList();
        final ValidationResult validationResult = new ValidationResult();

        try (Workbook workbook = ExcelHelper.createWorkbook(excelFile)) {

            final Collection<ObjectMapper> objectMappers = ExcelMapper
                    .build(workbook, getListDescriptor(), selectedSheets);

            for (final ObjectMapper objectMapper : objectMappers) {
                // check submitter exists
                final String submitterId = formatSubmitterId(objectMapper.getValue("Submitter_ID", String.class))
                        .orElse(null);
                checkSubmitterExists(submitterId);
                final EucegProduct.EucegProductBuilder builder = EucegProduct.builder()
                        .productNumber(getProductNumber(objectMapper))
                        .internalProductNumber(objectMapper.getValue("Internal_Product_Number", String.class))
                        .previousProductNumber(getPreviousProductNumber(objectMapper))
                        .submitterId(submitterId)
                        .preferredSubmissionType(objectMapper.getValue("Submission_Type", SubmissionTypeEnum.class))
                        .generalComment(objectMapper.getValue("Submission_General_Comment", String.class));

                products.add(
                    builder.product(createProduct(getCurrentProduct(getProductNumber(objectMapper)), objectMapper))
                            .build());
            }
        } catch (final UnsupportedExcelFormatException e) {
            // excel file format not supported
            throw new InvalidArgumentException(getI18nService().createKeyedMessage("app.euceg.import.file.notsupported",
                com.pmi.tpd.api.Product.getFullName()));
        } catch (final Exception e) {
            logger.error("Import product has failed", e);
            validationResult.addFailure(new SimpleValidationFailure(this, e.getMessage()));
        }
        return new ImportResultImpl<>(products, validationResult);
    }

    @Nonnull
    protected abstract ListDescriptor getListDescriptor();

    @Nonnull
    protected abstract T createProduct(@Nullable T product, @Nonnull final ObjectMapper objectMapper)
            throws EucegImportException;

    /**
     * @param productNumber
     *                      the product to get.
     * @return Returns the current product to update.
     */
    @Nullable
    public abstract T getCurrentProduct(@Nonnull String productNumber);

}
