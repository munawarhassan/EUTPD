package com.pmi.tpd.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the operation of the {@link com.pmi.tpd.database.DbTypeBean} class.
 */
public class DbTypeBeanTest {

    @Test
    public void validKey() {
        assertNotNull(DbTypeBean.forKey("oracle"));
    }

    @Test
    public void garbageKey() {
        assertNull(DbTypeBean.forKey("garbage"));
    }
}
