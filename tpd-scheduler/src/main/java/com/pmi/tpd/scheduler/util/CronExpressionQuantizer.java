package com.pmi.tpd.scheduler.util;

import java.util.Random;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Utility class to prevent the use of cron expressions that run more frequently
 * than once per minute. It is assumed
 * that the cron expression has already been validated or that it is acceptable
 * for the validation to return the "wrong"
 * message when the expression is validated later on.
 * <p>
 * Without this, people could supply cron expressions that run every five
 * seconds or something equally inappropriate.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class CronExpressionQuantizer {

  /** */
  private static final Pattern REGEX = Pattern.compile("^[0-5]?[0-9] ");

  /** */
  private static final Random RANDOM = new Random();

  private CronExpressionQuantizer() {
  }

  /**
   * Quantize the seconds field using {@link Randomize#AS_NEEDED}.
   *
   * @param cronExpression
   *                       the cron expression to (possibly) quantize.
   * @return the cron expression with the seconds field forced to a random value
   *         between {@code 0} and {@code 59}
   *         (inclusive) if it is not already in that format
   */
  public static String quantizeSecondsField(final String cronExpression) {
    return quantizeSecondsField(cronExpression, Randomize.AS_NEEDED);
  }

  /**
   * Quantize the seconds field, randomizing it as requested.
   *
   * @param cronExpression
   *                       the cron expression to (possibly) quantize.
   * @param randomize
   *                       controls the randomization of the seconds fields
   *                       ({@code null} is assumed to mean
   *                       {@link Randomize#AS_NEEDED}.
   * @return the cron expression with the seconds field forced to a value between
   *         {@code 0} and {@code 59} (inclusive)
   *         if it is not already in that format or as instructed to by the
   *         {@code randomize} option.
   */
  public static String quantizeSecondsField(@Nullable final String cronExpression,
      @Nullable final Randomize randomize) {
    if (!shouldEdit(cronExpression, randomize)) {
      return cronExpression;
    }

    final int pos = cronExpression.indexOf(' ');
    if (pos <= 0) {
      return cronExpression;
    }

    final StringBuilder sb = new StringBuilder(cronExpression.length());
    sb.append(randomize != Randomize.NEVER ? RANDOM.nextInt(60) : 0);
    return sb.append(cronExpression, pos, cronExpression.length()).toString();
  }

  @SuppressWarnings("SimplifiableIfStatement")
  private static boolean shouldEdit(@Nullable final String cronExpression, @Nullable final Randomize randomize) {
    if (cronExpression == null) {
      return false;
    }
    if (randomize == Randomize.ALWAYS) {
      return true;
    }
    return !REGEX.matcher(cronExpression).find();
  }

  /**
   * Controls whether or not randomization is performed when a cron expression is
   * quantized.
   */
  public static enum Randomize {
    /**
     * Never randomize the seconds field. If the supplied value does not meet the
     * requirement (a simple integer from
     * {@code 0} to {@code 59}), then force it to {@code 0}.
     */
    NEVER,

    /**
     * Randomize the seconds field if quantization is needed; otherwise, leave it
     * alone. If the supplied value does
     * not meet the requirement (a simple integer from {@code 0} to {@code 59}),
     * then force it to a randomized
     * value; otherwise, leave it alone.
     */
    AS_NEEDED,

    /**
     * Replace the seconds fields with a randomized value in all cases. The
     * replacement is performed regardless of
     * whether or not the value was already in the permitted format.
     */
    ALWAYS
  }
}
