package com.pmi.tpd.api.paging;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class FilterTest extends TestCase {

    @Test
    public void shouldCreateBetweenFilterAcceptingNullValues() {
        final Filter<?> filter = Filter.between("prop", null, null);

        assertTrue(filter.isEmptyOrNull());
        assertTrue(filter.from().isEmpty());
        assertTrue(filter.to().isEmpty());
    }
}
