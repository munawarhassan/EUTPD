package com.pmi.tpd.api.scheduler.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;

/**
 * Utility for serializing the parameters map to a {@code byte[]} and restoring
 * it to its original form.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ParameterMapSerializer {

  // CHECKSTYLE:OFF
  /**
   * Serializes the parameters to a byte array. It is highly recommended that
   * {@code SchedulerService} implementations
   * use this serialization to make it possible to
   * {@link AbstractJobDetailsFactory#buildJobDetails(com.pmi.tpd.core.backup.scheduler.config.JobId,Object,com.pmi.tpd.core.backup.scheduler.config.RunMode)}
   * reconstruct} the {@code JobDetails} even when the {@code JobRunner} for it
   * has not been registered. Otherwise, it
   * will be difficult to recover any information about the job at all.
   *
   * @param parameters
   *                   the parameters map to serialize; may be {@code null} as
   *                   shorthand for an empty map
   * @return the serialized parameters map, or {@code null} if {@code parameters}
   *         was either {@code null} or empty
   * @throws SchedulerServiceException
   *                                   if the parameters map cannot be serialized,
   *                                   presumably because it contains an object
   *                                   that cannot be
   *                                   serialized
   */
  // CHECKSTYLE:ON
  @Nullable
  public byte[] serializeParameters(@Nullable final Map<String, Serializable> parameters)
      throws SchedulerServiceException {
    if (parameters == null || parameters.isEmpty()) {
      return null;
    }
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      final ObjectOutputStream out = new ObjectOutputStream(bytes);
      try {
        out.writeObject(parameters);
      } finally {
        out.close();
      }
    } catch (final IOException ioe) {
      throw new SchedulerServiceException("Serialization failed", ioe);
    }
    return bytes.toByteArray();
  }

  /**
   * Deserializes the parameters map from a byte array using the provided
   * {@code ClassLoader}.
   *
   * @param classLoader
   *                    the class loader to use for resolving classes
   * @param parameters
   *                    the parameters to be deserialized; may be {@code null},
   *                    which results in an empty map
   * @return the deserialized parameters
   * @throws ClassNotFoundException
   *                                if {@link ClassLoaderAwareObjectInputStream}
   *                                does
   * @throws IOException
   *                                if {@link ClassLoaderAwareObjectInputStream}
   *                                does
   */
  @Nonnull
  public Map<String, Serializable> deserializeParameters(final ClassLoader classLoader,
      @Nullable final byte[] parameters) throws ClassNotFoundException, IOException {
    if (parameters == null) {
      return ImmutableMap.of();
    }
    final ObjectInputStream in = createObjectInputStream(classLoader, parameters);
    try {
      return readParameterMap(in);
    } finally {
      in.close();
    }
  }

  /**
   * @param classLoader
   * @param parameters
   * @return
   * @throws IOException
   */
  protected ObjectInputStream createObjectInputStream(final ClassLoader classLoader, final byte[] parameters)
      throws IOException {
    return new ClassLoaderAwareObjectInputStream(classLoader, parameters);
  }

  private Map<String, Serializable> readParameterMap(final ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    @SuppressWarnings("unchecked")
    final Map<String, Serializable> map = (Map<String, Serializable>) in.readObject();
    if (map != null) {
      return map;
    }
    return ImmutableMap.of();
  }

}
