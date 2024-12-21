package com.pmi.tpd.testing.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.data.domain.Pageable;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class PageableMatcher extends TypeSafeMatcher<Pageable> {

    public static Matcher<Pageable> pageable(final int limit) {
        return new PageableMatcher(limit);
    }

    public static Matcher<Pageable> pageable(final int start, final int limit) {
        return new PageableMatcher(start, limit);
    }

    private final int limit;

    private final int start;

    public PageableMatcher(final int limit) {
        this(0, limit);
    }

    public PageableMatcher(final int start, final int limit) {
        this.start = start;
        this.limit = limit;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("A page request with start at ")
                .appendValue(start)
                .appendText(" and limit of ")
                .appendValue(limit);
    }

    @Override
    protected boolean matchesSafely(final Pageable parameter) {
        return start == parameter.getPageNumber() && limit == parameter.getPageSize();
    }

}