package com.pmi.tpd.cluster;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.CONNECT;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.DISCONNECT;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

/**
 * Outcome of a {@link IClusterJoinCheck}. Can represent 3 states:
 * <ul>
 * <li>Everything is fine - no error messages, {@code passivate} is
 * {@code false}</li>
 * <li>There is a problem and the nodes shouldn't connect - one or more error
 * messages have been set, {@code passivate}
 * is {@code false}</li>
 * <li>There is a serious problem and one of the nodes needs to be evented - one
 * ore more error messages have been set
 * and {@code passivate} is {@code true}</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class ClusterJoinCheckResult {

  /** */
  public static final ClusterJoinCheckResult OK = new Builder().build();

  /** */
  private final ClusterJoinCheckAction action;

  /** */
  private final ImmutableList<String> messages;

  private ClusterJoinCheckResult(final ClusterJoinCheckAction action, final ImmutableList<String> messages) {
    this.action = checkNotNull(action, "action");
    this.messages = checkNotNull(messages, "messages");
  }

  /**
   * @param message
   *                a message describing the problem
   * @return a result signalling an issue that should prevent the nodes from
   *         connecting
   */
  @Nonnull
  public static ClusterJoinCheckResult disconnect(@Nonnull final String message) {
    return new Builder().disconnect(message).build();
  }

  /**
   * @param action
   *                the passivate action to take
   * @param message
   *                a message describing the problem
   * @return a result signalling an serious issue that should cause one of the
   *         nodes to be passivated
   */
  @Nonnull
  public static ClusterJoinCheckResult passivate(@Nonnull final ClusterJoinCheckAction action,
      @Nonnull final String message) {
    return new Builder().passivate(action, message).build();
  }

  /**
   * @return the action to be performed (connect, disconnect, passivate)
   */
  @Nonnull
  public ClusterJoinCheckAction getAction() {
    return action;
  }

  /**
   * @return the error messages, if any
   */
  @Nonnull
  public ImmutableList<String> getMessages() {
    return messages;
  }

  /**
   * Builder for {@link ClusterJoinCheckResult}.
   */
  public static class Builder {

    /** */
    private final ImmutableList.Builder<String> messages = ImmutableList.builder();

    /** */
    private ClusterJoinCheckAction action = CONNECT;

    @Nonnull
    public ClusterJoinCheckResult build() {
      return new ClusterJoinCheckResult(action, messages.build());
    }

    @Nonnull
    public Builder action(@Nonnull final ClusterJoinCheckAction action, @Nonnull final String message) {
      messages.add(checkNotNull(message, "message"));

      if (this.action.getId() < checkNotNull(action, "action").getId()) {
        // action is more serious than the current action
        this.action = action;
      }
      return this;
    }

    @Nonnull
    public Builder disconnect(@Nonnull final String message) {
      return action(DISCONNECT, message);
    }

    @Nonnull
    public Builder passivate(@Nonnull final ClusterJoinCheckAction action, @Nonnull final String message) {
      isTrue(checkNotNull(action, "action").isPassivate(), action + " incompatible with passivate");
      return action(action, message);
    }
  }
}
