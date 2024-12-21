package com.pmi.tpd.api.util;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class ClassLoaderUtilsTest extends TestCase {

    @Test
    public void loadClass() throws ClassNotFoundException {
        assertEquals(ClassLoaderUtilsTest.class,
            ClassLoaderUtils.loadClass(ClassLoaderUtilsTest.class.getName(), this.getClass()));

        try {
            ClassLoaderUtils.loadClass("some.class", (ClassLoader) null);
            fail("Should have thrown a class not found exception");
        } catch (final ClassNotFoundException ex) {
            // good, good
        }
    }

}
