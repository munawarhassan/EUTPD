package com.pmi.tpd.euceg.core.exporter;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.GroupDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;

/**
 * @author Christophe Friederich
 * @since 2.5
 */
public abstract class BaseExcelExporter<B> {

    /** */
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /** */
    private ListDescriptor root;

    /** */
    private List<ColumnDescriptor<?>> columns;

    /** */
    private List<String> columnNames;

    /** */
    private Workbook workbook;

    /** */
    private Sheet sheet;

    /** */
    @Nonnull
    private final List<ExcelSheet> excelSheets;

    /** */
    private Row row;

    /** */
    private final Map<Sheet, Integer> rowNums = Maps.newHashMap();

    /** */
    protected CellStyle columnPrimaryCellStyle;

    /** */
    protected CellStyle columnOddCellStyle;

    /** */
    private CellStyle headerCellStyle;

    /** */
    private MutableInt counter;

    /** */
    private final ExportOption options;

    /** */
    private final IDataProvider<B> dataProvider;

    protected BaseExcelExporter(@Nonnull final ListDescriptor root, @Nonnull final List<ExcelSheet> excelSheets,
            @Nullable final ExportOption options, @Nonnull final IDataProvider<B> dataProvider) {
        this.dataProvider = Preconditions.checkNotNull(dataProvider);;
        this.options = options == null ? ExportOption.builder().build() : options;
        this.excelSheets = Assert.checkNotNull(excelSheets, "excelSheets");
        setRoot(root);
    }

    protected void export(@Nonnull final OutputStream stream,
        @Nullable final ITaskMonitorProgress monitor,
        @Nonnull final ICallbackExport<B> callback) throws Throwable {
        checkNotNull(callback, "callback");
        try {
            workbook = new SXSSFWorkbook(50);
            this.configureStyle();
            counter = new MutableInt(0);

            doExport(monitor, callback);
            workbook.write(stream);
        } catch (final Exception ex) {
            LOGGER.error("Export failed", ex);
            Throwables.throwIfUnchecked(ex);
        } finally {
            IOUtils.close(stream, null);
            IOUtils.close(workbook, null);
            workbook = null;
        }
    }

    public IDataProvider<B> getDataProvider() {
        return dataProvider;
    }

    protected void peek(final B element) {

    }

    protected final void doExport(final ITaskMonitorProgress monitor, final @Nonnull ICallbackExport<B> callback) {
        final Workbook workbook = getWorkbook();

        this.configureStyle();

        // create sheets with header
        for (final ExcelSheet excelSheet : excelSheets) {
            final Sheet sheet = workbook.createSheet(excelSheet.getName());
            final Row headerRow = sheet.createRow(0);
            final MutableInt i = new MutableInt(0);

            getRoot().getColumns(excelSheet).forEach(col -> {
                final Cell cell = headerRow.createCell(i.intValue());
                cell.setCellValue(col.getName());
                cell.setCellStyle(getHeaderCellStyle());
                // sheet.autoSizeColumn(i.getAndIncrement());// cannot use with POI streaming API
                applyDefaultColumnStyle(col, cell);
                i.increment();
            });

            final int keySize = getRoot().getGroupDescriptors(excelSheet)
                    .findFirst()
                    .map(g -> g.getPrimaryKeyColumns().size())
                    .orElse(0);
            sheet.createFreezePane(keySize, 1);

        }

        callback.summarize();

        try (Stream<B> stream = this.getDataProvider().stream(monitor).peek(this::peek)) {
            stream.forEach(obj -> {
                if (obj != null) {
                    if (monitor != null) {
                        monitor.increment();
                    }
                    callback.forEach(obj);
                }
                getCounter().increment();
            });
        }
        if (monitor != null) {
            monitor.finish();
        }

    }

    @Nonnull
    protected List<ExcelSheet> getExcelSheets() {
        return this.excelSheets;
    }

    protected ExportOption getOptions() {
        return options;
    }

    protected void configureStyle() {
        // Create a Font for styling header cells
        final Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 14);

        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        final Font columnPrimaryFont = workbook.createFont();
        columnPrimaryFont.setColor(IndexedColors.WHITE.getIndex());

        columnPrimaryCellStyle = workbook.createCellStyle();
        columnPrimaryCellStyle.setFont(columnPrimaryFont);
        columnPrimaryCellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        columnPrimaryCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        columnOddCellStyle = workbook.createCellStyle();
        columnOddCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        columnOddCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    protected Sheet entrySheet(final @Nonnull ExcelSheet excelSheet) {
        columns = root.getColumns(excelSheet).collect(Collectors.toUnmodifiableList());
        columnNames = columns.stream().map(ColumnDescriptor::getName).collect(Collectors.toUnmodifiableList());
        row = null;
        final String name = excelSheet.getName();
        this.sheet = workbook.getSheet(name);
        if (sheet == null) {
            throw new IllegalArgumentException("the sheet '" + name + "' doesn't exist");
        }
        if (!rowNums.containsKey(this.sheet)) {
            rowNums.put(this.sheet, 1);
        }
        return this.sheet;
    }

    protected Workbook getWorkbook() {
        return workbook;
    }

    protected CellStyle getHeaderCellStyle() {
        return headerCellStyle;
    }

    protected MutableInt getCounter() {
        return counter;
    }

    protected ListDescriptor getRoot() {
        return root;
    }

    protected void setRoot(final ListDescriptor root) {
        this.root = root;
    }

    protected Row createRow() {
        final int index = rowNums.get(sheet);
        row = sheet.createRow(index);
        rowNums.put(sheet, index + 1);
        return row;
    }

    protected Row getCurrentRow() {
        return row;
    }

    protected int getColumnIndex(final String column) {
        return columnNames.indexOf(column);
    }

    protected Optional<ColumnDescriptor<?>> getColumn(final String columnName) {
        return columns.stream().filter(c -> columnName.equals(c.getName())).findFirst();
    }

    protected abstract void setValue(final ColumnDescriptor<?> col);

    protected void setValues(final @Nonnull String groupName) {
        getRoot().get(groupName).orElseThrow().getColumns().forEach(col -> setValue(col));
    }

    protected void setValuesWithForeignKeys(final @Nonnull String groupName) {
        final GroupDescriptor group = getRoot().get(groupName).orElseThrow();
        GroupDescriptor parent = group.getParent();
        while (parent != null) {
            parent.getColumns().forEach(c -> {
                if (c.isPrimaryKey()) {
                    setValue(c);
                } else {
                    // just apply style but not fill value
                    final Cell cell = row.createCell(getColumnIndex(c.getName()));
                    if (this.options.isStripedRow() && counter.intValue() % 2 != 0) {
                        cell.setCellStyle(columnOddCellStyle);
                    }
                }
            });
            parent = parent.getParent();
        }
    }

    protected void setValue(final String column, final Object value) {
        final ColumnDescriptor<?> col = getColumn(column).orElseThrow();
        final Cell cell = row.createCell(getColumnIndex(column));
        cell.setCellStyle(getPreferredCellStyle(cell));
        if (value != null) {

            if (isDate(value.getClass()) && col.getMetadata() != null && col.getMetadata().getFormat() != null) {
                if (value instanceof DateTime) {
                    cell.setCellValue(((DateTime) value).toDate());
                } else {
                    cell.setCellValue((java.util.Date) value);
                }
            } else {
                cell.setCellValue(value.toString());
            }

        }

    }

    protected void applyDefaultColumnStyle(final ColumnDescriptor<?> col, final Cell cell) {
        final String format = col.getMetadata().getFormat();
        final int columnIndex = cell.getColumnIndex();
        final Sheet sheet = cell.getSheet();
        final Workbook workbook = sheet.getWorkbook();
        if (format != null) {
            if (isDate(col.getMetadata().getTargetType())) {
                final CellStyle cellStyle = workbook.createCellStyle();
                final CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
                sheet.setDefaultColumnStyle(columnIndex, cellStyle);
            }
        }
    }

    protected CellStyle getPreferredCellStyle(final Cell cell) {
        // a method to get the preferred cell style for a cell
        // this is either the already applied cell style
        // or if that not present, then the row style (default cell style for this row)
        // or if that not present, then the column style (default cell style for this column)
        CellStyle cellStyle = cell.getCellStyle();
        if (cellStyle.getIndex() == 0) {
            cellStyle = cell.getRow().getRowStyle();
        }
        if (cellStyle == null) {
            cellStyle = cell.getSheet().getColumnStyle(cell.getColumnIndex());
        }
        if (cellStyle == null) {
            cellStyle = cell.getCellStyle();
        }
        return cellStyle;
    }

    protected static boolean isDate(final Class<?> type) {
        return DateTime.class.equals(type) || java.util.Date.class.equals(type);
    }

}
