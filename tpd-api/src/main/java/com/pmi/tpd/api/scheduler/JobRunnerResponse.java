package com.pmi.tpd.api.scheduler;

import static com.pmi.tpd.api.scheduler.status.IRunDetails.MAXIMUM_MESSAGE_LENGTH;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.ABORTED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.FAILED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.SUCCESS;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.pmi.tpd.api.scheduler.status.IRunDetails;
import com.pmi.tpd.api.scheduler.status.RunOutcome;
import com.pmi.tpd.api.util.Assert;

/**
 * An object that represents the result of a call to {@link IJobRunner#runJob(JobRunnerRequest)}. The job runner can use
 * this to customize the reporting of its status; otherwise it can simply return {@code null} on success and throw an
 * exception on failure.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Immutable
public final class JobRunnerResponse {

    /**
     * Creates a successful response with no additional message.
     *
     * @return the response
     */
    @Nonnull
    public static JobRunnerResponse success() {
        return success(null);
    }

    /**
     * Creates a successful response with the specified message.
     *
     * @param message
     *                the message to return, which is optional and will be truncated to
     *                {@link com.pmi.tpd.api.scheduler.status.IRunDetails#MAXIMUM_MESSAGE_LENGTH} if necessary
     * @return the response
     */
    @Nonnull
    public static JobRunnerResponse success(@Nullable final String message) {
        return new JobRunnerResponse(SUCCESS, message);
    }

    /**
     * Creates a response that indicates the request was aborted. In most cases, it will make more sense to report the
     * job as either having {@link #success() succeeded} with nothing to do or {@link #failed(String) failed}, instead.
     *
     * @param message
     *                the message to return, which will be truncated to {@link IRunDetails#MAXIMUM_MESSAGE_LENGTH} if
     *                necessary. The message is <strong>required</strong> when reporting that the job was aborted.
     * @return the response
     */
    @Nonnull
    public static JobRunnerResponse aborted(@Nonnull final String message) {
        Assert.isTrue(isNotBlank(message), "The message must be specified when reporting a job as aborted!");
        return new JobRunnerResponse(ABORTED, message);
    }

    /**
     * Creates a response that indicates the request has failed.
     *
     * @param message
     *                the message to return, which will be truncated to {@link IRunDetails#MAXIMUM_MESSAGE_LENGTH} if
     *                necessary. The message is <strong>required</strong> when reporting that the job has failed.
     * @return the response
     */
    @Nonnull
    public static JobRunnerResponse failed(@Nonnull final String message) {
        Assert.isTrue(isNotBlank(message), "The message must be specified when reporting a job as failed!");
        return new JobRunnerResponse(FAILED, message);
    }

    /**
     * Creates a response that indicates the request has failed. The {@link #getMessage() message} is set to to an
     * abbreviated representation of the exception and its causes, but the
     * {@link com.pmi.tpd.api.scheduler.status.IRunDetails#MAXIMUM_MESSAGE_LENGTH} still applies, so this information
     * may be incomplete. When possible, the {@link IJobRunner} is encouraged to trap its exceptions and report more
     * specific diagnostic messages with {@link #failed(String)}, instead.
     *
     * @param cause
     *              the exception that caused this failure
     * @return the response
     */
    @Nonnull
    public static JobRunnerResponse failed(@Nonnull final Throwable cause) {
        return new JobRunnerResponse(FAILED, toMessage(Assert.checkNotNull(cause, "cause")));
    }

    /** */
    private final RunOutcome runOutcome;

    /** */
    private final String message;

    private JobRunnerResponse(final RunOutcome runOutcome, @Nullable final String message) {
        this.runOutcome = runOutcome;
        this.message = message;
    }

    // Implementation note: This class is intended to follow the same immutable
    // object builder pattern that
    // JobConfig does; there just isn't anything else to set on it at this time.

    /**
     * @return
     */
    @Nonnull
    public RunOutcome getRunOutcome() {
        return runOutcome;
    }

    /**
     * @return
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JobRunnerResponse other = (JobRunnerResponse) o;
        return runOutcome == other.runOutcome && Objects.equal(message, other.message);
    }

    @Override
    public int hashCode() {
        return 31 * runOutcome.hashCode() + (message != null ? message.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "JobRunnerResponse[runOutcome=" + runOutcome + ",message='" + message + "']";
    }

    private static boolean isNotBlank(@Nullable final String message) {
        return message != null && !message.trim().isEmpty();
    }

    /**
     * Creates an abbreviated representation of the specified exception.
     * <p>
     * The current implementation of this is to return the {@link Class#getSimpleName() simple class name} of {@code e}.
     * If {@code e} has a {@link Throwable#getMessage() message}, then {@code ": "} is added, followed by that message.
     * This is repeated for each {@link Throwable#getCause() cause} in {@code e}'s exception chain, with newlines in
     * between, until they are all exhausted or the
     * {@link com.pmi.tpd.api.scheduler.status.IRunDetails.status.RunDetails#MAXIMUM_MESSAGE_LENGTH} has been reached.
     * This is just a rough guess at what is moderately likely to be useful information.
     * </p>
     *
     * @param e
     *          the exception to convert into an abbreviated troubleshooting message
     * @return the message
     */
    private static String toMessage(final Throwable e) {
        final StringBuilder message = new StringBuilder(MAXIMUM_MESSAGE_LENGTH);
        appendShortForm(message, e);
        Throwable cause = e.getCause();
        while (message.length() < MAXIMUM_MESSAGE_LENGTH && cause != null) {
            message.append('\n');
            appendShortForm(message, cause);
            cause = cause.getCause();
        }
        if (message.length() > MAXIMUM_MESSAGE_LENGTH) {
            message.setLength(MAXIMUM_MESSAGE_LENGTH);
        }
        return message.toString();
    }

    private static void appendShortForm(final StringBuilder sb, final Throwable e) {
        sb.append(e.getClass().getSimpleName());

        final String msg = e.getMessage();
        if (msg != null) {
            sb.append(": ").append(msg);
        }
    }
}
