package com.pmi.tpd.api.scheduler;

import static com.pmi.tpd.api.scheduler.JobRunnerResponse.aborted;
import static com.pmi.tpd.api.scheduler.JobRunnerResponse.failed;
import static com.pmi.tpd.api.scheduler.JobRunnerResponse.success;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.ABORTED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.FAILED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.scheduler.status.IRunDetails;
import com.pmi.tpd.api.scheduler.status.RunOutcome;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings("ConstantConditions")
public class JobRunnerResponseTest extends TestCase {

    @Test
    public void testSuccessNoArg() {
        assertResponse(success(), SUCCESS, null);
    }

    @Test
    public void testSuccessMessageNull() {
        assertResponse(success(null), SUCCESS, null);
    }

    @Test
    public void testSuccessMessage() {
        assertResponse(success("Info"), SUCCESS, "Info");
    }

    @Test
    public void testIgnoredMessageNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            aborted(null);
        });
    }

    @Test
    public void testIgnoredMessage() {
        assertResponse(aborted("Info"), ABORTED, "Info");
    }

    @Test
    public void testFailedMessageNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            failed((String) null);
        });
    }

    @Test
    public void testFailedMessage() {
        assertResponse(failed("Info"), FAILED, "Info");
    }

    @Test
    public void testFailedExceptionNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            failed((Exception) null);
        });
    }

    @Test
    public void testFailedExceptionNoChain() {
        final NullPointerException npe = new NullPointerException();
        assertResponse(failed(npe), FAILED, "NullPointerException");
    }

    @Test
    public void testFailedExceptionShortChain() {
        final NullPointerException npe = new NullPointerException();
        final ExecutionException ex = new ExecutionException(npe);
        assertResponse(failed(ex), FAILED, "ExecutionException: java.lang.NullPointerException\nNullPointerException");
    }

    @Test
    public void testFailedExceptionLongChain() {
        Throwable e = new IllegalArgumentException("The root of all evil");
        e = new ExecutionException(e.toString(), e);
        e = new ExecutionException(e.toString(), e);
        e = new ExecutionException(e.toString(), e);

        final JobRunnerResponse response = failed(e);
        assertThat(response.getRunOutcome(), equalTo(FAILED));
        assertThat(response.getMessage(), containsString("ExecutionException"));
        assertThat(response.getMessage(), containsString("IllegalArgumentException"));
        assertThat(response.getMessage(), containsString("root"));
        assertThat(response.getMessage().length(), equalTo(IRunDetails.MAXIMUM_MESSAGE_LENGTH));
    }

    private static void assertResponse(final JobRunnerResponse response,
        final RunOutcome expectedOutcome,
        final String expectedMessage) {
        assertThat(response.getRunOutcome(), equalTo(expectedOutcome));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }
}
