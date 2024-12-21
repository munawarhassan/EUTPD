package com.pmi.tpd.cluster;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.pmi.tpd.api.versioning.IBuildUtilsInfo;

/**
 * A {@link IClusterJoinRequirement} which requires that the build commit hashes
 * match between the client and server.
 * <p>
 * application currently requires that both nodes are running the exact same
 * code. This will need to change in the
 * future if we wish to perform seamless upgrades.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Component
public class BuildHashClusterJoinRequirement implements IClusterJoinRequirement<String> {

  /** */
  private final IBuildUtilsInfo buildInfo;

  /**
   * @param buildInfo
   */
  @Inject
  public BuildHashClusterJoinRequirement(final IBuildUtilsInfo buildInfo) {
    this.buildInfo = buildInfo;
  }

  @Nonnull
  @Override
  public String getName() {
    return "buildHash";
  }

  @Override
  public String getValue() {
    return buildInfo.getCommitId();
  }
}
