package com.pmi.tpd.cluster.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.EventObject;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.cluster.IClusterNode;

/**
 * A base class for events related to the cluster.
 * <p>
 * This class exists primarily to simplify writing event types. Plugins should
 * not listen for this low-level base class;
 * they should listen for specific subclasses.
 * <p>
 * Cluster node events are not part of the
 * {@link com.pmi.tpd.core.exec.event.ApplicationEvent ApplicationEvent}
 * hierarchy. Most cluster node events happen in response to system-level
 * actions, like new nodes joining or existing
 * nodes departing, rather than happening in response to user actions, so they
 * have their own hierarchy.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class ClusterNodeEvent extends EventObject {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final long date;

  /** */
  private final IClusterNode node;

  ClusterNodeEvent(@Nonnull final Object source, @Nonnull final IClusterNode node) {
    super(checkNotNull(source, "source"));

    this.node = Assert.checkNotNull(node, "node");

    date = System.currentTimeMillis();
  }

  /**
   * @return the timestamp when the event was <i>raised</i>
   */
  @Nonnull
  public Date getDate() {
    return new Date(date); // Return a new date to ensure immutability for the event instance
  }

  /**
   * Retrieves the node for the event. <i>This is intentionally not public.</i>
   * Derived types should expose the node
   * using a more specific name that makes sense for the context rather than this
   * generic name.
   *
   * @return the event node
   */
  @Nonnull
  IClusterNode getNode() {
    return node;
  }
}
