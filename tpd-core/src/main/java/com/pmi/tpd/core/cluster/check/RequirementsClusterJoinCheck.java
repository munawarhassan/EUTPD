package com.pmi.tpd.core.cluster.check;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_ANY_NODE;
import static com.pmi.tpd.cluster.HazelcastDataUtils.readMap;
import static com.pmi.tpd.cluster.HazelcastDataUtils.writeMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.cluster.IClusterJoinRequest;
import com.pmi.tpd.cluster.IClusterJoinRequirement;

/**
 * {@link IClusterJoinCheck join check} that verifies that the other node meets
 * the {@link IClusterJoinRequirement
 * requirements} for forming a cluster. If any requirement doesn't match or a
 * requirement is missing on either of the
 * nodes, the check will fail and signal that one of the nodes needs to be
 * passivated.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class RequirementsClusterJoinCheck implements IClusterJoinCheck {

  /** */
  private final Map<String, IClusterJoinRequirement<?>> requirements;

  @Inject
  public RequirementsClusterJoinCheck(final IClusterJoinRequirement<?>... requirements) {
    this.requirements = new HashMap<>(requirements.length);
    for (final IClusterJoinRequirement<?> requirement : requirements) {
      this.requirements.put(requirement.getName(), requirement);
    }
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult accept(@Nonnull final IClusterJoinRequest request) throws IOException {
    final ClusterJoinCheckResult.Builder resultBuilder = new ClusterJoinCheckResult.Builder();

    final Map<String, IClusterJoinRequirement<?>> requirements = Maps.newHashMap(this.requirements);

    try {
      // read the advertised requirements
      final Map<String, Serializable> remoteRequirements = readMap(request.in());
      for (final Map.Entry<String, Serializable> entry : remoteRequirements.entrySet()) {
        final String name = entry.getKey();
        final Serializable expected = entry.getValue();

        final IClusterJoinRequirement<?> requirement = requirements.remove(name);
        if (requirement == null) {
          resultBuilder.passivate(PASSIVATE_ANY_NODE,
              "Connecting node requires property '" + name + "' to be '" + expected
                  + "' but the property is not defined");

        } else {
          final Serializable actual = requirement.getValue();
          if (!Objects.equals(expected, actual)) {
            resultBuilder.passivate(PASSIVATE_ANY_NODE,
                "Required property '" + name + "' should be '" + expected + "' but is '" + actual + "'");
          }
        }
      }
      for (final String missing : requirements.keySet()) {
        resultBuilder.passivate(PASSIVATE_ANY_NODE,
            "Connecting node did not provide required property '" + missing + "'");
      }
    } catch (final NoClassDefFoundError e) {
      // can happen if a requirement from the other node has a value of an unknown
      // type
      resultBuilder.passivate(PASSIVATE_ANY_NODE,
          "Could not validate whether the node meets the requirements for connecting to the " + "cluster: '"
              + e.getMessage() + "'");
    }

    return resultBuilder.build();
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult connect(@Nonnull final IClusterJoinRequest request) throws IOException {
    final Map<String, Serializable> expectedValues = new HashMap<>(requirements.size());
    for (final IClusterJoinRequirement<?> requirement : requirements.values()) {
      expectedValues.put(requirement.getName(), requirement.getValue());
    }

    writeMap(request.out(), expectedValues);

    // the remote node validates the requirements
    return ClusterJoinCheckResult.OK;
  }

  @Nonnull
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public int getOrder() {
    return 10;
  }

  @Nonnull
  @Override
  public ClusterJoinCheckResult onUnknown(@Nonnull final IClusterJoinRequest request) {
    // other node does not have the RequirementsClusterJoinCheck
    final ClusterJoinCheckResult.Builder resultBuilder = new ClusterJoinCheckResult.Builder();
    for (final String missing : requirements.keySet()) {
      resultBuilder.passivate(PASSIVATE_ANY_NODE, "Node did not provide required property '" + missing + "'");
    }

    return resultBuilder.build();
  }
}
