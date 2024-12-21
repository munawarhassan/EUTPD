package com.pmi.tpd.api.scheduler.status;

import static com.pmi.tpd.api.scheduler.status.IRunDetails.MAXIMUM_MESSAGE_LENGTH;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.ABORTED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.FAILED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.SUCCESS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "ConstantConditions" })
public class RunDetailsImplTest extends TestCase {

    private static final Date NOW = new Date();

    @Test
    public void testLastRunTimeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RunDetailsImpl(null, ABORTED, 0L, "Info");
        });
    }

    @Test
    public void testLastRunTimeIsDefensivelyCopied() {
        final Date original = new Date();
        final long originalTime = original.getTime();
        final RunDetailsImpl js = new RunDetailsImpl(original, ABORTED, 0L, "Info");
        original.setTime(42L);

        Date copy = Assert.checkNotNull(js.getStartTime(), "copy");
        assertThat("Modifying the original after it is submitted does not change the value",
            copy.getTime(),
            is(originalTime));

        copy.setTime(42L);
        copy = Assert.checkNotNull(js.getStartTime(), "copy");
        assertThat("Modifying the returned copy does not change the value", copy.getTime(), is(originalTime));
    }

    @Test
    public void testRunOutcomeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RunDetailsImpl(NOW, null, 0L, "Info");
        });
    }

    @Test
    public void testDurationIsNegative() {
        assertEquals(0L,
            new RunDetailsImpl(NOW, ABORTED, -42L, "Info").getDurationInMillis(),
            "Negative durations should force to 0L");
    }

    @Test
    public void testInfoIsNull() {
        assertThat(new RunDetailsImpl(NOW, ABORTED, 0L, null).getMessage(), is(""));
    }

    @Test
    public void testMessageIsMaximumLength() {
        final String message = generate(MAXIMUM_MESSAGE_LENGTH);
        final RunDetailsImpl js = new RunDetailsImpl(NOW, ABORTED, 0L, message);
        assertThat(js.getMessage(), is(message));
    }

    @Test
    public void testMessageExceedsMaximumLength() {
        final String message = generate(MAXIMUM_MESSAGE_LENGTH + 42);
        final RunDetailsImpl js = new RunDetailsImpl(NOW, ABORTED, 0L, message);
        assertNotNull(js.getMessage());
        assertThat(message, startsWith(js.getMessage()));
        assertThat(js.getMessage().length(), is(MAXIMUM_MESSAGE_LENGTH));
    }

    @Test
    public void testValues1() {
        final Date later = new Date(NOW.getTime() + 42L);
        final RunDetailsImpl js = new RunDetailsImpl(later, SUCCESS, 42L, "Info");
        assertThat(js.getStartTime(), is(later));
        assertThat(js.getRunOutcome(), is(SUCCESS));
        assertThat(js.getDurationInMillis(), is(42L));
        assertThat(js.getMessage(), is("Info"));
    }

    @Test
    public void testValues2() {
        final Date later = new Date(NOW.getTime() + 42L);
        final RunDetailsImpl js = new RunDetailsImpl(later, FAILED, 2495L, "Hello, world!");
        assertThat(js.getStartTime(), is(later));
        assertThat(js.getRunOutcome(), is(FAILED));
        assertThat(js.getDurationInMillis(), is(2495L));
        assertThat(js.getMessage(), is("Hello, world!"));
    }

    /**
     * Generates a string of the requested length and comprised of printable ASCII characters (from '!' to '~')
     *
     * @param len
     *            the desired length of the string
     * @return the generated string
     */
    private static String generate(final int len) {
        char c = '!';
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append(c);
            if (c >= '~') {
                c = '!';
            } else {
                c++; // :P
            }
        }
        return sb.toString();
    }
}
