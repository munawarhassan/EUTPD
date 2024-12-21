package com.pmi.tpd.scheduler.util;

import static com.pmi.tpd.scheduler.util.CronExpressionQuantizer.quantizeSecondsField;
import static com.pmi.tpd.scheduler.util.CronExpressionQuantizer.Randomize.ALWAYS;
import static com.pmi.tpd.scheduler.util.CronExpressionQuantizer.Randomize.AS_NEEDED;
import static com.pmi.tpd.scheduler.util.CronExpressionQuantizer.Randomize.NEVER;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings("ConstantConditions")
public class CronExpressionQuantizerTest extends TestCase {

    private static final Pattern SIMPLE_SECONDS_FIELD = Pattern.compile("^([1-5]?\\d) (.*)$");

    // How certain we want to be that we didn't get a spurious pass from a fortunate random number.
    private static final int ATTEMPTS = 100;

    @Test
    public void testNullOrIncompleteExpression() {
        verifyNullOrIncompleteExpression(null);
        verifyNullOrIncompleteExpression("");
        verifyNullOrIncompleteExpression("0");
        verifyNullOrIncompleteExpression("a");
        verifyNullOrIncompleteExpression("*");
        verifyNullOrIncompleteExpression("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @Test
    public void testRandomizedNever() {
        assertZeroed("x * * * * ?", NEVER);
        assertZeroed("0 * * * * ?", NEVER);
        assertNoChange("42 * * * * ?", NEVER);
        assertZeroed("60 * * * * ?", NEVER);
        assertZeroed("*/5 * * * * ?", NEVER);
        assertZeroed("7,12 * * * * ?", NEVER);
        assertZeroed("x * * * * ?", NEVER);
        assertZeroed("a b c", NEVER);
    }

    @Test
    public void testRandomizedAsNeededByNullArgument() {
        assertRandomized("x * * * * ?", null);
        assertNoChange("0 * * * * ?", null);
        assertNoChange("42 * * * * ?", null);
        assertRandomized("60 * * * * ?", null);
        assertRandomized("*/5 * * * * ?", null);
        assertRandomized("7,12 * * * * ?", null);
        assertRandomized("x * * * * ?", null);
        assertRandomized("a b c", null);
    }

    @Test
    public void testRandomizedAsNeededByDefault() {
        assertRandomized("x * * * * ?");
        assertNoChange("0 * * * * ?");
        assertNoChange("42 * * * * ?");
        assertRandomized("60 * * * * ?");
        assertRandomized("*/5 * * * * ?");
        assertRandomized("7,12 * * * * ?");
        assertRandomized("x * * * * ?");
        assertRandomized("a b c");
    }

    @Test
    public void testRandomizedAsNeeded() {
        assertRandomized("x * * * * ?", AS_NEEDED);
        assertZeroed("0 * * * * ?", AS_NEEDED);
        assertNoChange("42 * * * * ?", AS_NEEDED);
        assertRandomized("60 * * * * ?", AS_NEEDED);
        assertRandomized("*/5 * * * * ?", AS_NEEDED);
        assertRandomized("7,12 * * * * ?", AS_NEEDED);
        assertRandomized("x * * * * ?", AS_NEEDED);
        assertRandomized("a b c", AS_NEEDED);
    }

    @Test
    public void testRandomizedAlways() {
        assertRandomized("x * * * * ?", ALWAYS);
        assertRandomized("0 * * * * ?", ALWAYS);
        assertRandomized("42 * * * * ?", ALWAYS);
        assertRandomized("60 * * * * ?", ALWAYS);
        assertRandomized("*/5 * * * * ?", ALWAYS);
        assertRandomized("7,12 * * * * ?", ALWAYS);
        assertRandomized("x * * * * ?", ALWAYS);
        assertRandomized("a b c", ALWAYS);
    }

    private static void verifyNullOrIncompleteExpression(final String cronExpression) {
        assertNoChange(cronExpression, null);
        assertNoChange(cronExpression, NEVER);
        assertNoChange(cronExpression, AS_NEEDED);
        assertNoChange(cronExpression, ALWAYS);
    }

    private static void assertRandomized(final String cronExpression) {
        String s = null;
        final String expectedRest = cronExpression.substring(cronExpression.indexOf(' ') + 1);
        for (int i = 0; i < ATTEMPTS; ++i) {
            s = quantizeSecondsField(cronExpression);
            if (looksRandomized(expectedRest, s)) {
                return;
            }
        }
        fail("Did not get a randomized seconds field using cron expression '" + cronExpression
                + "' with randomize=unspecified (should default to AS_NEEDED); last result was: '" + s + '\'');
    }

    private static void assertRandomized(final String cronExpression,
        final CronExpressionQuantizer.Randomize randomize) {
        String s = null;
        final String expectedRest = cronExpression.substring(cronExpression.indexOf(' ') + 1);
        for (int i = 0; i < ATTEMPTS; ++i) {
            s = quantizeSecondsField(cronExpression, randomize);
            if (looksRandomized(expectedRest, s)) {
                return;
            }
        }
        fail("Did not get a randomized seconds field using cron expression '" + cronExpression + "' with randomize="
                + randomize + "; last result was: '" + s + '\'');
    }

    private static boolean looksRandomized(final String expectedRest, final String cronExpression) {
        final Matcher matcher = SIMPLE_SECONDS_FIELD.matcher(cronExpression);
        if (!matcher.find() || !expectedRest.equals(matcher.group(2))) {
            return false;
        }
        final int seconds = Integer.parseInt(matcher.group(1));
        return seconds > 0 && seconds <= 59 && seconds != 42;
    }

    private static void assertZeroed(final String cronExpression, final CronExpressionQuantizer.Randomize randomize) {
        final String expectedRest = cronExpression.substring(cronExpression.indexOf(' ') + 1);
        for (int i = 0; i < ATTEMPTS; ++i) {
            final String s = quantizeSecondsField(cronExpression, randomize);
            if (!looksZeroed(expectedRest, s)) {
                fail("Did not get a zeroed seconds field using cron expression '" + cronExpression + "' with randomize="
                        + randomize + "; attempt i=" + i + " result was: '" + s + '\'');
            }
        }
    }

    private static boolean looksZeroed(final String expectedRest, final String cronExpression) {
        final Matcher matcher = SIMPLE_SECONDS_FIELD.matcher(cronExpression);
        return matcher.find() && "0".equals(matcher.group(1)) && expectedRest.equals(matcher.group(2));
    }

    private static void assertNoChange(final String cronExpression) {
        for (int i = 0; i < ATTEMPTS; ++i) {
            assertThat(quantizeSecondsField(cronExpression), equalTo(cronExpression));
        }
    }

    private static void assertNoChange(final String cronExpression, final CronExpressionQuantizer.Randomize randomize) {
        for (int i = 0; i < ATTEMPTS; ++i) {
            assertThat(quantizeSecondsField(cronExpression, randomize), equalTo(cronExpression));
        }
    }
}
