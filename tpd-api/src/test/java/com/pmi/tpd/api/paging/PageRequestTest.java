package com.pmi.tpd.api.paging;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.pmi.tpd.api.paging.Filter.Operator;
import com.pmi.tpd.testing.junit5.TestCase;

public class PageRequestTest extends TestCase {

    @Test
    public void shouldCreatePageRequestWithEmptySortAndFilter() {
        final PageRequest request = new PageRequest(0, 5, null, null);

        assertEquals(0, request.getPage());
        assertEquals(5, request.getSize());
        assertEquals(Sort.unsorted(), request.getSort());
        assertEquals(Filters.empty(), request.getFilters());
    }

    @Test
    public void shouldCreatePageRequestForEqualFilter() {
        final PageRequest request = new PageRequest(0, 5, null, createFilter("property", Operator.eq, "val"));

        assertTrue(request.getFilters().findFirst("property").isPresent());
        final Filter<?> filter = request.getFilters().get("property");
        assertEquals(Operator.eq, filter.getOperator());
        assertEquals("val", filter.getValue().get());
    }

    @Test
    public void shouldCreatePageRequestForContainFilter() {
        final PageRequest request = new PageRequest(0, 5, null, createFilter("property", Operator.contains, "val"));

        assertTrue(request.getFilters().findFirst("property").isPresent());
        final Filter<?> filter = request.getFilters().get("property");
        assertEquals(Operator.contains, filter.getOperator());
        assertEquals("val", filter.getValue().get());
    }

    @Test
    public void shouldCreatePageRequestForBetweenFilter() {
        final PageRequest request = new PageRequest(0, 5, null,
                createFilter("property", Operator.between, "val1,val2"));

        assertTrue(request.getFilters().findFirst("property").isPresent());
        final Filter<?> filter = request.getFilters().get("property");
        assertEquals(Operator.between, filter.getOperator());
        assertThat(filter.getValues(), Matchers.contains("val1", "val2"));
        assertEquals("val1", filter.from().get());
        assertEquals("val2", filter.to().get());
    }

    private String createFilter(final String property, final Operator operator, final String value) {
        return String.format("%s::%s==%s", property, operator.getOperator(), value);
    }
}
