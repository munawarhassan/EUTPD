package com.pmi.tpd.core.cluster.check;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_ANY_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_OTHER_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.cluster.IClusterJoinRequest;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

/**
 * Verifies that two nodes joining share the same shared home and database.
 * There are three possible outcomes:
 * <ul>
 * <li>The nodes share have the same shared home and database - the check
 * succeeds</li>
 * <li>The nodes are unrelated: they have different shared homes _and_ databases
 * - the check fails and prevents the
 * nodes from forming a cluster. No node is passivated.</li>
 * <li>The nodes connect to the same database but have different shared homes
 * (or vice versa) - the check fails and
 * causes one of the nodes to be passivated. The most recently started node will
 * be passivated.</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SharedHomeAndDatabaseJoinCheck implements IClusterJoinCheck {

  /** */
  private final IApplicationConfiguration applicationSettings;

  /** */
  private final IDatabaseConfigurationService configurationService;

  /** */
  private final IDataSourceConfiguration dataSourceConfiguration;

  /** */
  private final ISecureTokenGenerator secureTokenGenerator;

  /**
   * @param applicationSettings
   * @param configurationService
   * @param dataSourceConfiguration
   * @param secureTokenGenerator
   */
  @Inject
  public SharedHomeAndDatabaseJoinCheck(final IApplicationConfiguration applicationSettings,
      final IDatabaseConfigurationService configurationService,
      final IDataSourceConfiguration dataSourceConfiguration, final ISecureTokenGenerator secureTokenGenerator) {
    this.applicationSettings = applicationSettings;
    this.configurationService = configurationService;
    this.dataSourceConfiguration = dataSourceConfiguration;
    this.secureTokenGenerator = secureTokenGenerator;
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult accept(@Nonnull final IClusterJoinRequest request) throws IOException {
    File tmpFile = null;
    boolean sharedHomeMatches = false;
    try {
      tmpFile = File
          .createTempFile("cluster-join", ".txt", applicationSettings.getSharedHomeDirectory().toFile());
      final String token = secureTokenGenerator.generateToken();

      Files.asCharSink(tmpFile, Charsets.UTF_8).write(token);

      request.out().writeUTF(tmpFile.getName());

      if (request.in().readBoolean()) {
        // remote node found the token file, validate the token
        sharedHomeMatches = token.equals(request.in().readUTF());
      }
    } finally {
      if (tmpFile != null && !tmpFile.delete()) {
        tmpFile.deleteOnExit();
      }
    }

    final String remoteJdbcUrl = request.in().readUTF();
    final String remoteJdbcUser = request.in().readUTF();
    final boolean databaseMatches = dataSourceConfiguration.getUrl().equals(remoteJdbcUrl)
        && dataSourceConfiguration.getUser().equals(remoteJdbcUser);

    if (sharedHomeMatches) {
      if (databaseMatches) {
        return ClusterJoinCheckResult.OK;
      }

      // databases don't match, check whether the in-memory config is up to date with
      // the on-disk config
      final IDataSourceConfiguration onDiskConfiguration = configurationService.loadDataSourceConfiguration();
      final boolean onDiskConfigMatchesCurrent = Objects
          .equals(onDiskConfiguration.getUrl(), dataSourceConfiguration.getUrl())
          && Objects.equals(onDiskConfiguration.getUser(), dataSourceConfiguration.getUser());

      return ClusterJoinCheckResult.passivate(
          onDiskConfigMatchesCurrent ? PASSIVATE_OTHER_NODE : PASSIVATE_THIS_NODE,
          "Nodes are connected to the same shared home but different databases");
    }

    // sharedHomeMatches == false
    return databaseMatches
        ? ClusterJoinCheckResult.passivate(PASSIVATE_ANY_NODE,
            "Nodes are connected to the same database but different shared homes")
        : ClusterJoinCheckResult.disconnect("Node is part of an unrelated cluster");
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult connect(@Nonnull final IClusterJoinRequest request) throws IOException {
    final File tmpFile = applicationSettings.getSharedHomeDirectory().resolve(request.in().readUTF()).toFile();
    final boolean fileExists = tmpFile.isFile();
    request.out().writeBoolean(fileExists);
    if (fileExists) {
      final String token = Files.asCharSource(tmpFile, Charsets.UTF_8).read();
      request.out().writeUTF(token);
    }

    request.out().writeUTF(dataSourceConfiguration.getUrl());
    request.out().writeUTF(dataSourceConfiguration.getUser());

    return ClusterJoinCheckResult.OK;
  }

  @Nonnull
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public int getOrder() {
    return 1;
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult onUnknown(@Nonnull final IClusterJoinRequest request) {
    return ClusterJoinCheckResult.passivate(PASSIVATE_ANY_NODE,
        "Cannot verify whether the nodes connect to the same database and shared home");
  }
}
