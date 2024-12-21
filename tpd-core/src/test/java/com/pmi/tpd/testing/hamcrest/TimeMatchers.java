package com.pmi.tpd.testing.hamcrest;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class TimeMatchers {

    public static Matcher<Date> closeTo(final Date ref, final int plusOrMinus, final TimeUnit unit) {
        return new TypeSafeMatcher<>() {

            @Override
            public boolean matchesSafely(final Date date) {
                return Math.abs(ref.getTime() - date.getTime()) < TimeUnit.MILLISECONDS.convert(plusOrMinus, unit);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a date around ")
                        .appendValue(ref)
                        .appendText(" plus or minus ")
                        .appendValue(plusOrMinus)
                        .appendText(" ")
                        .appendText(unit.name().toLowerCase());
            }
        };
    }

}
