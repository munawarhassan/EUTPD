package com.pmi.tpd.database.liquibase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;
import com.pmi.tpd.testing.junit5.TestCase;

public class LiquibaseConstantsTest extends TestCase {

    @Test
    public void testEncodingIsUtf8() throws Exception {
        assertEquals(Charsets.UTF_8, Charset.forName(LiquibaseConstants.ENCODING));
    }

    @Test
    public void testCanNotInstantiate() throws Exception {
        assertThrows(InvocationTargetException.class, () -> {
            final Constructor<LiquibaseConstants> constructor = LiquibaseConstants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

}
