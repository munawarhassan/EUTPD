package com.pmi.tpd.euceg.core.excel;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;

import com.pmi.tpd.testing.junit5.TestCase;

public class ExcelHelperTest extends TestCase {

    public ExcelHelperTest() {

    }

    @Test
    public void testBooleanConversion() {

        assertEquals(true, ExcelHelper.convert("true", Boolean.class));
        assertEquals(true, ExcelHelper.convert("on", Boolean.class));
        assertEquals(true, ExcelHelper.convert("yes", Boolean.class));
        assertEquals(true, ExcelHelper.convert("y", Boolean.class));
        assertEquals(true, ExcelHelper.convert("1", Boolean.class));
        assertEquals(true, ExcelHelper.convert("True", Boolean.class));
        assertEquals(true, ExcelHelper.convert("On", Boolean.class));
        assertEquals(true, ExcelHelper.convert("Yes", Boolean.class));
        assertEquals(true, ExcelHelper.convert("Y", Boolean.class));

        assertEquals(false, ExcelHelper.convert("false", Boolean.class));
        assertEquals(false, ExcelHelper.convert("off", Boolean.class));
        assertEquals(false, ExcelHelper.convert("no", Boolean.class));
        assertEquals(false, ExcelHelper.convert("n", Boolean.class));
        assertEquals(false, ExcelHelper.convert("0", Boolean.class));
        assertEquals(false, ExcelHelper.convert("False", Boolean.class));
        assertEquals(false, ExcelHelper.convert("Off", Boolean.class));
        assertEquals(false, ExcelHelper.convert("No", Boolean.class));
        assertEquals(false, ExcelHelper.convert("N", Boolean.class));

        assertEquals(false, ExcelHelper.convert("", Boolean.class));
        assertNull(ExcelHelper.convert(null, Boolean.class));

        try {
            assertNull(ExcelHelper.convert("O", Boolean.class));
            fail();
        } catch (final ConversionFailedException e) {

        }
    }

}
