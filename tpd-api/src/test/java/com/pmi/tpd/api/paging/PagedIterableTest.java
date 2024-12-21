package com.pmi.tpd.api.paging;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class PagedIterableTest {

    @Test
    public void shouldReturnLimitedListWithMaximumElement() {

        final ListPageProvider<String> provider = new ListPageProvider<>(
                ImmutableList.of("1", "2", "3", "4", "5", "6"));
        final Pageable request = PageUtils.newRequest(0, 2);
        long maxElement = 4;
        List<String> actual = Lists.newArrayList(new PagedIterable<>(provider, request).setLimit(maxElement));
        Assertions.assertIterableEquals(ImmutableList.of("1", "2", "3", "4"), actual);

        maxElement = 3;
        actual = Lists.newArrayList(new PagedIterable<>(provider, request).setLimit(maxElement));
        Assertions.assertIterableEquals(ImmutableList.of("1", "2", "3"), actual);

    }

    @Test
    public void shouldReturnActualListLesserThanMaximumElement() {

        final ListPageProvider<String> provider = new ListPageProvider<>(
                ImmutableList.of("1", "2", "3", "4", "5", "6"));
        final Pageable request = PageUtils.newRequest(0, 2);
        final long maxElement = 10;
        final List<String> actual = Lists.newArrayList(new PagedIterable<>(provider, request).setLimit(maxElement));
        Assertions.assertIterableEquals(ImmutableList.of("1", "2", "3", "4", "5", "6"), actual);

    }
}
