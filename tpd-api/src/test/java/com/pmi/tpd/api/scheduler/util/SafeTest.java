package com.pmi.tpd.api.scheduler.util;

import static com.pmi.tpd.api.scheduler.util.Safe.copy;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.testing.junit5.TestCase;

public class SafeTest extends TestCase {

    @Test
    public void testCopyDateNull() {
        assertNull(copy((Date) null));
    }

    @Test
    public void testCopyDate() {
        final Date now = new Date();
        final Date copy = copy(now);
        assertEquals(now, copy);
        assertNotSame(now, copy);
    }

    @Test
    public void testCopyMapNull() {
        final Map<String, Serializable> map = copy((Map<String, Serializable>) null);
        assertTrue(map instanceof ImmutableMap, "Should be an ImmutableMap");
    }

    @Test
    public void testCopyMapKnownImmutable() {
        final ImmutableMap<String, Serializable> map = ImmutableMap.<String, Serializable>builder().put("Hello", 42L)
                .put("World", true).build();
        assertSame(map, copy(map));
    }
}
