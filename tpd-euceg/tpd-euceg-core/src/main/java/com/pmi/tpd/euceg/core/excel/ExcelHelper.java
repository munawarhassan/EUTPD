package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.EmissionNameEnum;
import org.eu.ceg.IngredientCategoryEnum;
import org.eu.ceg.IngredientFunctionEnum;
import org.eu.ceg.LeafCureMethodEnum;
import org.eu.ceg.LeafTypeEnum;
import org.eu.ceg.PackageTypeEnum;
import org.eu.ceg.PartTypeEnum;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.eu.ceg.ToxicityStatusEnum;
import org.eu.ceg.ToxicologicalDataAvailableEnum;
import org.eu.ceg.VoltageWattageAdjustableEnum;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.core.excel.converter.StringToBooleanConverter;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

/**
 * Excel Utility class.
 *
 * @author pschmid pascal.schmid@contracted.pmi.com
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ExcelHelper {

    /** */
    // TODO [devacfr] refactoring using ConverterFactory
    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    static {
        converter.addConverter(String.class, Boolean.class, new StringToBooleanConverter());
        converter.addConverter(String.class,
            SubmissionTypeEnum.class,
            source -> SubmissionTypeEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            TobaccoProductTypeEnum.class,
            source -> TobaccoProductTypeEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            IngredientCategoryEnum.class,
            source -> IngredientCategoryEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            IngredientFunctionEnum.class,
            source -> IngredientFunctionEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            ToxicityStatusEnum.class,
            source -> ToxicityStatusEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            ToxicologicalDataAvailableEnum.class,
            source -> ToxicologicalDataAvailableEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            PackageTypeEnum.class,
            source -> PackageTypeEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            LeafCureMethodEnum.class,
            source -> LeafCureMethodEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            LeafTypeEnum.class,
            source -> LeafTypeEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            PartTypeEnum.class,
            source -> PartTypeEnum.fromValue(Integer.valueOf(source)));

        converter.addConverter(String.class,
            EcigProductTypeEnum.class,
            source -> EcigProductTypeEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            EmissionNameEnum.class,
            source -> EmissionNameEnum.fromValue(Integer.valueOf(source)));
        converter.addConverter(String.class,
            VoltageWattageAdjustableEnum.class,
            source -> VoltageWattageAdjustableEnum.fromValue(Integer.valueOf(source)));
    }

    private ExcelHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from the given InputStream.
     *
     * @param excelFile
     * @return Returns the appropriate HSSFWorkbook / XSSFWorkbook from the given InputStream.
     * @throws UnsupportedExcelFormatException
     *                                         Raise if excel file format is not supported
     */
    @Nonnull
    public static Workbook createWorkbook(@Nonnull final InputStream excelFile) throws UnsupportedExcelFormatException {
        checkNotNull(excelFile, "excelFile");
        try {
            return WorkbookFactory.create(excelFile);
        } catch (final IllegalArgumentException | IOException e) {
            throw new UnsupportedExcelFormatException(e.getMessage(), e);
        }
    }

    /**
     * Gets a list of available {@link SheetDescriptor sheets} contained in excel file stream and according to type of
     * product.
     *
     * @param excelFile
     *                    a stream of a excel file (can <b>not</b> be {@code null}).
     * @param productType
     *                    the product type of excel file (can <b>not</b> be {@code null}).
     * @return Returns a new ordered list of {@link SheetDescriptor} representing available sheets according to type of
     *         product.
     * @throws UnsupportedExcelFormatException
     *                                         Raise if excel file format is not supported
     * @throws IOException
     *                                         if I/O error
     * @since 1.6
     */
    @Nonnull
    public static Iterable<SheetDescriptor> getImportedSheets(@Nonnull final ProductType productType)
            throws UnsupportedExcelFormatException, IOException {
        checkNotNull(productType, "productType");
        List<ExcelSheet> sheets;
        if (ProductType.TOBACCO.equals(productType)) {
            sheets = EucegExcelSchema.TobaccoProduct.getImportedSheets();
        } else if (ProductType.ECIGARETTE.equals(productType)) {
            sheets = EucegExcelSchema.EcigProduct.getImportedSheets();

        } else {
            throw new IllegalArgumentException("productType");
        }
        return sheets.stream()
                .map(s -> new SheetDescriptor(s.getIndex(), s.getName(), s.isRequired()))
                .sorted((s1, s2) -> Integer.compare(s1.getIndex(), s2.getIndex()))
                .collect(Collectors.toList());
    }

    /**
     * Get the value contained in a {@link Cell cell} according to target type.
     *
     * @param cell
     *                   a cell to use (can <b>not</b> be {@code null}).
     * @param targetType
     *                   the target type (can <b>not</b> be {@code null}).
     * @return Returns a value representing the value contained in a cell trying to convert to the specified
     *         {@code targetType}.
     * @param <T>
     *            the target type.
     */
    @Nullable
    public static <T> T getValue(@Nonnull final Cell cell, @Nonnull final Class<T> targetType) {
        checkNotNull(cell, "cell");
        checkNotNull(targetType, "targetType");
        Object value = null;
        switch (cell.getCellType()) {
            case BOOLEAN:
            case NUMERIC:
                cell.setCellType(CellType.STRING);
            case STRING:
                value = cell.getStringCellValue();
                if (Strings.isNullOrEmpty((String) value)) {
                    value = null;
                } else {
                    final String str = value.toString();
                    final ByteBuffer buffer = Eucegs.getDefaultCharset().encode(str);
                    value = Eucegs.getDefaultCharset().decode(buffer).toString();
                }
                break;
            default:
                break;

        }
        return convert(value, targetType);
    }

    @VisibleForTesting
    static <T> T convert(final Object source, final Class<T> targetType) {
        return converter.convert(source, targetType);
    }

}
