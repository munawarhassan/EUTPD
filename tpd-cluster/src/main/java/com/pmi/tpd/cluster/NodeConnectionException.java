package com.pmi.tpd.cluster;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.ConnectException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class NodeConnectionException extends ConnectException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final List<String> issues;

  /**
   * @param msg
   */
  public NodeConnectionException(final String msg) {
    issues = ImmutableList.of(msg);
  }

  /**
   * @param issues
   */
  public NodeConnectionException(@Nonnull final Collection<String> issues) {
    this.issues = ImmutableList.copyOf(checkNotNull(issues, "issues"));
  }

  @Nonnull
  public List<String> getIssues() {
    return issues;
  }

  @Override
  public String getMessage() {
    return StringUtils.join(issues, ", ");
  }
}
