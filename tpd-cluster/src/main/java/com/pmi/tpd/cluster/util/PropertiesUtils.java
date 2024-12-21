package com.pmi.tpd.cluster.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

/**
 * Utilities for loading and saving properties files, and for parsing
 * expressions in property values.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class PropertiesUtils {

  /** */
  private static final Pattern FORMULA_PATTERN = Pattern.compile("[\\d\\+\\-\\*/\\(\\)\\s\\.]+");

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

  private PropertiesUtils() {
    throw new UnsupportedOperationException(
        getClass().getName() + " is a utility class and should not be instantiated");
  }

  /**
   * Load properties from a file. Returns empty properties if the file doesn't
   * exist, throws IOException if it could
   * not be read.
   *
   * @param file
   *             the properties file to read
   * @return the properties contained in the provided file, if any. Otherwise an
   *         empty {@link Properties} instance
   */
  @Nonnull
  public static Properties loadFromFile(final File file) throws IOException {
    final Properties properties = new Properties();
    if (file.exists()) {
      final InputStream is = new FileInputStream(file);
      try {
        properties.load(is);
      } finally {
        Closeables.closeQuietly(is);
      }
    }
    return properties;
  }

  public static void writeToFile(final Properties properties, final File file) throws IOException {
    final OutputStream os = new FileOutputStream(file, false);
    try {
      properties.store(os, null);
    } finally {
      Closeables.close(os, false);
    }
  }

  public static String getProperty(final File file, final String key) throws IOException {
    final Properties props = loadFromFile(file);
    return props.getProperty(key);
  }

  /**
   * Parses an expression and returns the interpreted value of a property. Only
   * mathematical expressions using +, -,
   * *, /, ( and ) are supported. In addition to basic arithmetic, the following
   * variables are supported:
   * <ul>
   * <li><em>cpu</em> - the number of CPUs (cores) available to the system</li>
   * </ul>
   *
   * @param expression
   *                     mathematical expression
   * @param defaultValue
   *                     the default value to use if the expression is invalid.
   * @return the result of interpreting the mathematical expression, or
   *         {@code defaultValue} if {@code expression} is
   *         undefined or invalid.
   */
  public static long parseExpression(@Nullable String expression, final long defaultValue) {
    long result = defaultValue;
    if (expression != null) {
      expression = expression.toLowerCase()
          .replace("cpu", Integer.toString(Runtime.getRuntime().availableProcessors()));
      if (FORMULA_PATTERN.matcher(expression).matches()) {
        ExpressionEvaluator evaluator;
        try {
          evaluator = new ExpressionEvaluator(expression, double.class, new String[] {}, new Class[] {});
          result = Math.round((Double) evaluator.evaluate(new Object[] {}));
        } catch (final CompileException e) {
          LOGGER.debug("Error while parsing expression " + expression, e);
        } catch (final InvocationTargetException e) {
          LOGGER.debug("Error while evaluating expression " + expression, e);
        }

      }
    }
    return result;
  }

  /**
   * Parses an expression and returns the interpreted value of a property. Only
   * mathematical expressions using +, -,
   * *, /, ( and ) are supported. In addition to basic arithmetic, the following
   * variables are supported:
   * <ul>
   * <li><em>cpu</em> - the number of CPUs (cores) available to the system</li>
   * </ul>
   *
   * @param expression
   *                     mathematical expression
   * @param defaultValue
   *                     the default value to use if the expression is invalid.
   * @return the result of interpreting the mathematical expression, or
   *         {@code defaultValue} if {@code expression} is
   *         undefined or invalid.
   */
  public static int parseExpression(@Nullable final String expression, final int defaultValue) {
    return (int) parseExpression(expression, (long) defaultValue);
  }
}
