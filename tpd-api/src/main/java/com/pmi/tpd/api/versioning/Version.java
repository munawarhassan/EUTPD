package com.pmi.tpd.api.versioning;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Version represents a version. This class is useful for comparing versions,
 * for instance when you need to determine
 * whether the version is at least X.
 * <p>
 * {@link #getVersion() Versions} parsed by this class are always
 * {@code 0}-padded to at least three places. For
 * example, an input of {@code "2"} will result in {@code [2, 0, 0]}. More
 * specific input, such as {@code "1.7.9.5"}
 * will result in longer lists; 3 places is a minimum, not a maximum.
 *
 * @since 1.3
 * @author Christophe Friederich
 */
public class Version implements Comparable<Version> {

  /**
   * A {@code Pattern} that can validate a potential version String to ensure it
   * contains a valid set of numbers and
   * '.''s {@code Version} does not use this pattern, but it's handy for code that
   * wants to ensure a version string is
   * valid before turning it into a {@code Version}.
   */
  public static final Pattern STRICT_NUMERIC_VALIDATOR = Pattern.compile("^\\d+(\\.\\d+)*$");

  /**
   * Parse out any leading numbers followed by any other characters (which are not
   * returned).
   */
  private static final Pattern STARTS_NUMERICAL_PATTERN = Pattern.compile("^(\\d+).*");

  /** default joiner */
  private static final Joiner joiner = Joiner.on('.');

  /** */
  private final List<Integer> version;

  /** */
  private final String versionString;

  /**
   * Constructor for accepting a pre-parsed version.
   *
   * @param elements
   *                 the version elements, starting with the most significant
   */
  public Version(final Integer... elements) {
    this.version = normalise(Lists.newArrayList(elements));

    versionString = joiner.join(version);
  }

  /**
   * Constructor that parses a version string. Version parts are determined by
   * splitting on [.-] characters. If a non-
   * numerical value is found (e.g. 'RC4'), the rest of the version String is
   * ignored. In other words '1.0.1-RC.2' is
   * considered equal to '1.0.1'
   *
   * @param version
   *                the version string
   */
  public Version(final String version) {
    final String[] components = version.split("[\\.\\-]");

    final List<Integer> list = Lists.newArrayListWithCapacity(components.length);
    for (final String component : components) {
      try {
        list.add(Integer.valueOf(component));
      } catch (final NumberFormatException e) {
        // not a number, check whether comp starts with a number e.g. in 1.0.1beta
        final Matcher matcher = STARTS_NUMERICAL_PATTERN.matcher(component);
        if (matcher.matches()) {
          list.add(Integer.valueOf(matcher.group(1)));
        }
        // ignore the rest of the version string (so ignore 'RC3' in 1.0.1-RC3)
        break;
      }
    }
    this.version = normalise(list);

    versionString = joiner.join(this.version);
  }

  /**
   * Compare two versions to determine their ordering. This class has a natural
   * ordering that is <i>consistent</i>
   * with {@link #equals(Object) equals}.
   * <p>
   * Note: Returns from this method should not be assumed to be {@code -1},
   * {@code 0} or {@code 1}. As per the
   * contract of {@code Comparable}, they should be evaluated as negative,
   * {@code 0} or positive. Implementations are
   * free to use any positive or negative number they wish to indicate greater
   * than and less than ordering.
   */
  @Override
  public int compareTo(@Nonnull final Version o) {
    final int mySize = this.version.size();
    final int theirSize = o.version.size();

    final int maxSize = Math.max(theirSize, mySize);
    for (int i = 0; i < maxSize; ++i) {
      final int mine = i < mySize ? this.version.get(i) : 0;
      final int theirs = i < theirSize ? o.version.get(i) : 0;

      final int diff = mine - theirs;
      if (diff != 0) {
        return diff;
      }
    }

    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final Version other = (Version) obj;
    return versionString.equals(other.versionString);
  }

  /**
   * Retrieves an immutable copy of the ordered list of version components.
   *
   * @return the version
   */
  public List<Integer> getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return versionString.hashCode();
  }

  /**
   * Returns a {@code String} representation of this {@link #getVersion()
   * version}, with the components separated by
   * dots.
   * <p>
   * Note: For version strings which contain non-numerical, non-separator
   * elements, the resulting representation will
   * not match the input. For example, an initial version like {@code "1.7.9-RC1"}
   * will result in {@code "1.7.9"} for
   * the return value here. Padding may also be applied, so an initial version of
   * {@code "2"} would be represented as
   * {@code "2.0.0"}.
   *
   * @return a string representation of the parsed version
   */
  @Override
  public String toString() {
    return versionString;
  }

  private List<Integer> normalise(final List<Integer> list) {
    // Ensure at least major, minor and patch version components.
    while (list.size() < 3) {
      list.add(0);
    }

    // Only hold immutable state, safe for return in getVersion()
    return ImmutableList.copyOf(list);
  }
}
