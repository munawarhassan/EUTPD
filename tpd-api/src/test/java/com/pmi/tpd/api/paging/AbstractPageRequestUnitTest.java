package com.pmi.tpd.api.paging;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.testing.junit5.TestCase;

public abstract class AbstractPageRequestUnitTest extends TestCase {

    public abstract Pageable newPageRequest(int page, int size);

    @Test
    void navigatesPageablesCorrectly() {

        final Pageable request = newPageRequest(1, 10);

        assertThat(request.hasPrevious(), Matchers.is(true));
        assertThat(request.next(), Matchers.is((Pageable) newPageRequest(2, 10)));

        final var first = request.previousOrFirst();

        assertThat(first.hasPrevious(), Matchers.is(false));
        assertThat(first, Matchers.is(newPageRequest(0, 10)));
        assertThat(first, Matchers.is(request.first()));
        assertThat(first.previousOrFirst(), Matchers.is(first));
    }

    @Test
    void equalsHonoursPageAndSize() {

        final var request = newPageRequest(0, 10);

        // Equals itself
        assertEqualsAndHashcode(request, request);

        // Equals same setup
        assertEqualsAndHashcode(request, newPageRequest(0, 10));

        // Does not equal on different page
        assertNotEqualsAndHashcode(request, newPageRequest(1, 10));

        // Does not equal on different size
        assertNotEqualsAndHashcode(request, newPageRequest(0, 11));
    }

}
