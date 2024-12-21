package com.pmi.tpd.testing.hamcrest;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import javax.annotation.Nonnull;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.springframework.data.domain.Page;

/**
 * Hamcrest matchers for instances of {@link Page}.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class PageMatchers {

    private PageMatchers() {
        throw new UnsupportedOperationException(PageMatchers.class.getName() + " should not be instantiated");
    }

    @Nonnull
    public static <T> Matcher<Page<? extends T>> hasSize(final int expectedSize) {
        return hasSizeThat(is(expectedSize));
    }

    @Nonnull
    public static <T> Matcher<Page<? extends T>> hasSizeThat(@Nonnull final Matcher<Integer> sizeMatcher) {
        checkNotNull(sizeMatcher, "sizeMatcher");
        return new FeatureMatcher<>(sizeMatcher, "size that", "size") {

            @Override
            protected Integer featureValueOf(@Nonnull final Page<? extends T> actual) {
                return actual.getNumberOfElements();
            }
        };
    }

    /**
     * @param expectedStart
     *            expected value of the the {@code start} field
     * @param <T>
     *            page element type
     * @return matcher for the page start
     */
    @Nonnull
    public static <T> Matcher<Page<? extends T>> hasStartPage(final int expectedStart) {
        return hasStartPageThat(is(expectedStart));
    }

    /**
     * @param startMatcher
     *            matcher for the {@code start} field
     * @param <T>
     *            page element type
     * @return matcher for the page start
     */
    @Nonnull
    public static <T> Matcher<Page<? extends T>> hasStartPageThat(@Nonnull final Matcher<Integer> startMatcher) {
        checkNotNull(startMatcher, "startMatcher");
        return new FeatureMatcher<>(startMatcher, "start that", "start") {

            @Override
            protected Integer featureValueOf(@Nonnull final Page<? extends T> actual) {
                return actual.getNumber();
            }
        };
    }

    @Nonnull
    public static <T> Matcher<Page<? extends T>> isEmptyPage() {
        return hasSize(0);
    }

    @Nonnull
    public static <T> Matcher<Page<? extends T>> isLastPage() {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            protected boolean matchesSafely(final Page<? extends T> item, final Description mismatchDescription) {
                if (!item.isLast()) {
                    mismatchDescription.appendText("not last");
                }
                return item.isLast();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Last page with results");
            }
        };
    }

    /**
     * @param expectedValues
     *            matchers for expected values in the page, in the exact order as provided
     * @param <T>
     *            type of the page elements
     * @return matcher for last page containing the expected values
     * @see #isLastPage()
     * @see #isPageOf(Matcher)
     */
    @Nonnull
    @SafeVarargs
    public static <T> Matcher<Page<? extends T>> isLastPageOf(@Nonnull final Matcher<? super T>... expectedValues) {
        return allOf(isLastPage(), isPageOf(expectedValues));
    }

    /**
     * @param expectedValues
     *            expected values in the page, in the exact order as provided
     * @param <T>
     *            type of the page elements
     * @return matcher for last page containing the expected values
     * @see #isLastPage()
     * @see #isPageOf(Object[])
     */
    @Nonnull
    @SafeVarargs
    public static <T> Matcher<Page<? extends T>> isLastPageOf(final T... expectedValues) {
        return allOf(isLastPage(), isPageOf(expectedValues));
    }

    /**
     * Matcher for a page that must contain a sequence of values matching {@code expectedValues} matchers, in the exact
     * order as provided. Equivalent to using {@link Matchers#contains(Matcher[])} over the page {@link Page#getValues()
     * values}.
     *
     * @param expectedValues
     *            matchers for expected values in the page, in the exact order as provided
     * @param <T>
     *            type of the page elements
     * @return matcher for the expected page values
     * @see Matchers#contains(Matcher[])
     */
    @Nonnull
    @SafeVarargs
    public static <T> Matcher<Page<? extends T>> isPageOf(@Nonnull final Matcher<? super T>... expectedValues) {
        return allOf(PageMatchers.<T> hasSize(expectedValues.length),
            PageMatchers.<T> isPageOf(contains(expectedValues)));
    }

    /**
     * Matcher for a page that must contain a sequence of values equal to {@code expectedValues}, in the exact order as
     * provided. Equivalent to using {@link Matchers#contains(Object[])} over the page {@link Page#getValues() values}.
     *
     * @param expectedValues
     *            expected values in the page, in the exact order as provided
     * @param <T>
     *            type of the page elements
     * @return matcher for the expected page values
     * @see Matchers#contains(Object[])
     */
    @Nonnull
    @SafeVarargs
    public static <T> Matcher<Page<? extends T>> isPageOf(@Nonnull final T... expectedValues) {
        return allOf(PageMatchers.<T> hasSize(expectedValues.length),
            PageMatchers.<T> isPageOf(contains(expectedValues)));
    }

    @Nonnull
    public static <T> Matcher<Page<? extends T>> isPageOf(
        @Nonnull final Matcher<? super Iterable<? extends T>> valuesMatcher) {
        checkNotNull(valuesMatcher, "valuesMatcher");
        return new FeatureMatcher<Page<? extends T>, Iterable<? extends T>>(valuesMatcher, "values that", "values") {

            @Override
            protected Iterable<? extends T> featureValueOf(@Nonnull final Page<? extends T> actual) {
                return actual.getContent();
            }
        };
    }

    /**
     * Synonym for {@link #hasSize(int)} to avoid name clashes with other popular matcher libraries.
     *
     * @param expectedSize
     *            expected page size
     * @param <T>
     *            type of the page elements
     * @return matcher for the expected page size
     * @see #hasSize(int)
     */
    @Nonnull
    public static <T> Matcher<Page<? extends T>> isPageWithSize(final int expectedSize) {
        return hasSize(expectedSize);
    }
}
