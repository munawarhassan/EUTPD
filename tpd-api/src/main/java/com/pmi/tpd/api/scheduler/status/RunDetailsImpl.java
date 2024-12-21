package com.pmi.tpd.api.scheduler.status;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
public final class RunDetailsImpl implements IRunDetails {

  /** */
  private final long startTime;

  /** */
  private final RunOutcome runOutcome;

  /** */
  private final long durationInMillis;

  /** */
  private final String message;

  /**
   * @param startTime
   * @param runOutcome
   * @param durationInMillis
   * @param message
   */
  public RunDetailsImpl(final Date startTime, final RunOutcome runOutcome, final long durationInMillis,
      @Nullable final String message) {
    this.startTime = checkNotNull(startTime, "startTime").getTime();
    this.runOutcome = checkNotNull(runOutcome, "runOutcome");
    this.durationInMillis = durationInMillis >= 0L ? durationInMillis : 0L;
    this.message = truncate(message);
  }

  @Override
  @Nonnull
  public Date getStartTime() {
    return new Date(startTime);
  }

  @Override
  public long getDurationInMillis() {
    return durationInMillis;
  }

  @Override
  @Nonnull
  public RunOutcome getRunOutcome() {
    return runOutcome;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "RunDetailsImpl[startTime=" + startTime + ",runOutcome=" + runOutcome + ",durationInMillis="
        + durationInMillis + ",message=" + message + ']';
  }

  private static String truncate(@Nullable final String message) {
    if (message == null) {
      return "";
    }
    if (message.length() > MAXIMUM_MESSAGE_LENGTH) {
      return message.substring(0, MAXIMUM_MESSAGE_LENGTH);
    }
    return message;
  }
}
