package com.pmi.tpd.euceg.core.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import com.google.common.io.Closeables;
import com.pmi.tpd.euceg.core.util.validation.ValidationException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ColumnDescriptorTest extends MockitoTestCase {

    @Test
    public void shouldNotCreateColumnWithWrongArgument() {
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> ColumnDescriptor.createColumn(null, String.class),
            "name parameter is required");
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> ColumnDescriptor.createColumn("", String.class),
            "name parameter is required");
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> ColumnDescriptor.createColumn("  ", (Class<?>) null),
            "targetType parameter is required");
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> ColumnDescriptor.createColumn("col", (Class<?>) null),
            "targetType parameter is required");
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testComparison() {
        final ColumnDescriptor<String> col = ColumnDescriptor.createColumn("col", String.class);
        assertEquals(true, col.equals(col));
        assertEquals(true,
            ColumnDescriptor.createColumn("col", String.class)
                    .equals(ColumnDescriptor.createColumn("col", Integer.class)));
        assertEquals(false, ColumnDescriptor.createColumn("col", String.class).equals("col"));
        assertEquals(false, ColumnDescriptor.createColumn("col", String.class).equals(null));
    }

    @Test
    public void testGetValueWithWrongParameter() {
        final ColumnDescriptor<String> col = ColumnDescriptor.createColumn("col", String.class);
        assertThrowsWithMessage(IllegalArgumentException.class, () -> col.getValue(null), "row parameter is required");
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> col.getValue(mock(Row.class)),
            "sheet parameter is required");

    }

    @Test
    public void testGetValue() throws IOException {
        final ColumnDescriptor<String> col = ColumnDescriptor.createColumn("Submitter_ID", String.class);
        final ColumnDescriptor<String> colLowercase = ColumnDescriptor.createColumn("submitter_id", String.class);
        final ColumnDescriptor<String> colSpace = ColumnDescriptor.createColumn("  submitter_id  ", String.class);
        final ColumnDescriptor<String> wrongCol = ColumnDescriptor.createColumn("wrong", String.class);
        final ColumnDescriptor<Date> colWrongType = ColumnDescriptor.createColumn("Submitter_ID", Date.class);
        final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xls");
        try (final Workbook workbook = WorkbookFactory.create(in)) {
            final Sheet sheet = workbook.getSheetAt(0);
            final Row row = sheet.getRow(1); // skip first row
            assertEquals("99962", col.getValue(row));
            assertEquals("99962", colLowercase.getValue(row));
            assertThrowsWithMessage(IllegalArgumentException.class,
                () -> colSpace.getValue(row),
                "Cell index must be >= 0");
            assertThrowsWithMessage(IllegalArgumentException.class,
                () -> wrongCol.getValue(row),
                "Cell index must be >= 0");

            assertThrowsWithMessage(ValidationException.class,
                () -> colWrongType.getValue(row),
                "The column 'Submitter_ID' has wrong value in row [1] of sheet Sheet1");
        } finally {
            Closeables.closeQuietly(in);
        }
    }

    @Test
    public void testIsPartOf() throws IOException {
        final ColumnDescriptor<String> col = ColumnDescriptor.createColumn("Submitter_SME", String.class);
        final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xls");
        try (final Workbook workbook = WorkbookFactory.create(in)) {

            assertEquals(true, col.isPartOf(workbook.getSheetAt(0)));
            assertEquals(false, col.isPartOf(workbook.getSheetAt(1)));
        } finally {
            Closeables.closeQuietly(in);
        }
    }
}
