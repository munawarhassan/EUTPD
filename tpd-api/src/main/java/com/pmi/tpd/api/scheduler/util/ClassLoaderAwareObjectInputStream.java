package com.pmi.tpd.api.scheduler.util;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import javax.annotation.Nonnull;

/**
 * An object input stream that uses the provided {@code ClassLoader} in
 * preference to any other.
 */
public class ClassLoaderAwareObjectInputStream extends ObjectInputStream {

  /** */
  private final ClassLoader classLoader;

  /**
   * @param classLoader
   *                    the class loader to use for loading classes
   * @param parameters
   *                    the byte array to be deserialized; must not be
   *                    {@code null} and is not guarded against modification
   *                    for efficiency reasons
   * @throws IOException
   *                     if
   *                     {@link ObjectInputStream#ObjectInputStream(InputStream)}
   *                     itself does
   */
  public ClassLoaderAwareObjectInputStream(@Nonnull final ClassLoader classLoader, @Nonnull final byte[] parameters)
      throws IOException {
    super(new ByteArrayInputStream(checkNotNull(parameters, "parameters")));
    this.classLoader = checkNotNull(classLoader, "classLoader");
  }

  @Override
  protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    try {
      return Class.forName(desc.getName(), false, classLoader);
    } catch (final ClassNotFoundException originalEx) {
      try {
        // Desperation. Maybe we shouldn't even be trying? If it succeeds, we may be
        // getting the
        // "wrong" class anyway because ObjectInputStream selects the ClassLoader from
        // the "nearest"
        // one it finds on the Thread's stack that is not the system ClassLoader. This
        // will *probably*
        // be the webapp's ClassLoader, so we are *probably* ok. If this becomes a
        // problem and we
        // decide to drop this delegation, then we should reproduce the logic that it
        // has for mapping
        // primitive types; e.g., "int" -> Integer.TYPE
        return super.resolveClass(desc);
      } catch (final ClassNotFoundException ignoredEx) {
        // Prefer the exception that we got from our own ClassLoader to avoid confusion,
        // since that is
        // the one we are expected to be using.
        throw originalEx;
      }
    }
  }
}
