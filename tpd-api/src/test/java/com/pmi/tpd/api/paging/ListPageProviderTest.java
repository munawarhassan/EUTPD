package com.pmi.tpd.api.paging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.ImmutableList;

public class ListPageProviderTest {

    /** */
    private static final Pageable REQ_0_2 = PageUtils.newRequest(0, 2);

    @Test
    public void testEmptyList() {
        final ListPageProvider<String> provider = new ListPageProvider<>(Collections.<String> emptyList());

        final Page<String> page = provider.get(REQ_0_2);

        assertNotNull(page);
        assertTrue(page.isLast());
        assertEquals(0, page.getNumberOfElements());
    }

    @Test
    public void testPaging() {
        final ListPageProvider<String> provider = new ListPageProvider<>(ImmutableList.of("1", "2", "3"));

        Page<String> page = provider.get(REQ_0_2);

        // verify page 1
        assertNotNull(page);
        assertFalse(page.isLast());
        assertEquals(2, page.getNumberOfElements());

        List<String> values = ImmutableList.copyOf(page.getContent());
        assertEquals("1", values.get(0));
        assertEquals("2", values.get(1));

        // get page 2
        final Pageable secondPageRequest = page.nextPageable();
        assertEquals(2, secondPageRequest.getOffset());
        assertEquals(2, secondPageRequest.getPageSize());

        page = provider.get(secondPageRequest);

        // verify page 2
        assertNotNull(page);
        assertTrue(page.isLast());
        assertEquals(1, page.getNumberOfElements());

        values = ImmutableList.copyOf(page.getContent());
        assertEquals("3", values.get(0));
    }
}
