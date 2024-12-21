package com.pmi.tpd.cluster;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.pmi.tpd.api.config.IApplicationConfiguration;

/**
 * A {@link IClusterJoinRequirement} which checks that the shared home locations
 * match exactly.
 * <p>
 * This is a requirement of our fork implementation for git which uses absolute
 * paths for alternates.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SharedHomeClusterJoinRequirement implements IClusterJoinRequirement<String> {

  /** */
  private final IApplicationConfiguration applicationSettings;

  /**
   * @param applicationSettings
   */
  @Inject
  public SharedHomeClusterJoinRequirement(final IApplicationConfiguration applicationSettings) {
    this.applicationSettings = applicationSettings;
  }

  @Nonnull
  @Override
  public String getName() {
    return "sharedHome";
  }

  @Override
  public String getValue() {
    return applicationSettings.getSharedHomeDirectory().toAbsolutePath().toString();
  }

}
