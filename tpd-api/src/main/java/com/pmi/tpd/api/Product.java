package com.pmi.tpd.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class Product {

  /** */
  private static String prefix;

  /** */
  private static String name;

  /** */
  private static String fullName;

  /** */
  private static String clusterName;

  /** */
  private static int firstYearOfRelease;

  static {
    try {
      final Manifest manifest = getManifest();
      final Attributes attrs = manifest.getMainAttributes();
      prefix = Assert.hasText(attrs.getValue("Product-Prefix"),
          "the attribute is Product-Prefix required in manifest");
      name = Assert.hasText(attrs.getValue("Product-Name"),
          "the attribute is Product-Prefix required in manifest");
      fullName = Assert.hasText(attrs.getValue("Product-Fullname"),
          "the attribute is Product-Prefix required in manifest");
      clusterName = Assert.hasText(attrs.getValue("Product-ClusterName"),
          "the attribute is Product-Prefix required in manifest");
      firstYearOfRelease = Integer.parseInt(
          Assert.hasText(attrs.getValue("inceptionYear"), "the attribute is inceptionYear required in manifest"));
    } catch (final Throwable e) {
      LoggerFactory.getLogger(Product.class).warn("Retrieve product information failed", e);
    }
  }

  private Product() {
  }

  @Nonnull
  public static String getClusterName() {
    return clusterName;
  }

  public static int getFirstYearOfRelease() {
    return firstYearOfRelease;
  }

  @Nonnull
  public static String getFullName() {
    return fullName;
  }

  @Nonnull
  public static String getName() {
    return name;
  }

  @Nonnull
  public static String getPrefix() {
    return prefix;
  }

  @Nullable
  private static Manifest getManifest() {
    Enumeration<URL> resEnum;
    try {
      resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
      while (resEnum.hasMoreElements()) {
        InputStream is = null;
        try {
          final URL url = resEnum.nextElement();
          is = url.openStream();
          if (is != null) {
            final Manifest manifest = new Manifest(is);
            final Attributes mainAttribs = manifest.getMainAttributes();
            final String value = mainAttribs.getValue("Product-Name");
            if (value != null) {
              return manifest;
            }
          }
        } catch (final Exception e) {
          // Silently ignore wrong manifests on classpath?
        } finally {
          Closeables.closeQuietly(is);
        }
      }
    } catch (final IOException e1) {
      // Silently ignore wrong manifests on classpath?
    }
    return null;
  }
}
