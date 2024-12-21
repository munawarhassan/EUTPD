package com.pmi.tpd.euceg.api.entity;

import java.util.Map;

import javax.annotation.Nonnull;

import org.eu.ceg.AbstractAppResponse;
import org.eu.ceg.AppResponse;
import org.eu.ceg.ErrorResponse;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum TransmitStatus {

  /**
   * Indicate the associated payload is in an awaiting send state.
   * <p>
   * all associated element Attachements and Submitter Detail are sent (only for {@link PayloadType#SUBMISSION}.
   * </p>
   * )
   */
  AWAITING("Waiting"),
  /**
   * Indicate the associated payload has been sent.
   */
  PENDING("Pending"), // READY_TO_SEND, SEND_ENQUEUED, SEND_IN_PROGRESS, WAITING_FOR_RECEIPT, ACKNOWLEDGED,
                      // ACKNOWLEDGED_WITH_WARNING
  /**
   * Indicate a response has been received.
   */
  RECEIVED("Received"), // RECEIVED, RECEIVED_WITH_WARNINGS
  /**
   * Indicate that Domibus has rejected the request.
   */
  REJECTED("Rejected"), // SEND_ATTEMPT_FAILED, SEND_FAILURE, NOT_FOUND
  /**
   * Indicate that Domibus has deleted the response.
   */
  DELETED("Deleted"), // DELETED
  /**
   * indicate the sent has been cancelled by user.
   */
  CANCELLED("Cancelled");

  /** */
  private String name;

  /**
   * @param name
   */
  private TransmitStatus(final String name) {
    this.name = name;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
  public static Map<String, String> toMap() {
    final Map<String, String> map = Maps.newLinkedHashMap();
    for (final TransmitStatus e : TransmitStatus.values()) {
      map.put(e.toString(), e.getName());
    }
    return map;
  }

  /**
   * @param status
   * @return
   */
  public static TransmitStatus from(final String status) {
    if (Strings.isNullOrEmpty(status)) {
      return null;
    }
    for (final TransmitStatus item : TransmitStatus.values()) {
      if (item.name().equalsIgnoreCase(status)) {
        return item;
      }
    }
    return null;
  }

  /**
   * @param response
   * @return
   */
  public static TransmitStatus from(@Nonnull final AppResponse response) {
    Assert.checkNotNull(response, "response");
    if (response instanceof ErrorResponse) {
      return REJECTED;
    } else if (response instanceof AbstractAppResponse) {
      final AbstractAppResponse resp = (AbstractAppResponse) response;
      switch (resp.getStatus()) {
        case SUCCESS:
          return RECEIVED;
        case ERROR:
        default:
          return REJECTED;
      }
    }

    return REJECTED;
  }

}
