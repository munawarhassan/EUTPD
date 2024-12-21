package com.pmi.tpd.util;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.cluster.util.PropertiesUtils;
import com.pmi.tpd.testing.junit5.TestCase;

public class PropertiesUtilsTest extends TestCase {

    @Test
    public void testComplexFormulaExpression() {
        assertEquals(6, PropertiesUtils.parseExpression("2*3", -1));
        assertEquals(2, PropertiesUtils.parseExpression("(2-1)/(4-3.5)", -1));
    }

    @Test
    public void testConstantExpression() {
        assertEquals(10, PropertiesUtils.parseExpression("10", -1));
    }

    @Test
    public void testExpressionWithCpuVariable() {
        assertEquals(Math.round(Runtime.getRuntime().availableProcessors() * 1.5),
            PropertiesUtils.parseExpression("1.5* cpu", -1));
    }

    @Test
    public void testIllegalExpression() {
        assertEquals(-1, PropertiesUtils.parseExpression("System.exit(1);return 1.0;", -1));
    }

    @Test
    public void testUndefinedExpression() {
        assertEquals(-1, PropertiesUtils.parseExpression(null, -1));
    }

}
