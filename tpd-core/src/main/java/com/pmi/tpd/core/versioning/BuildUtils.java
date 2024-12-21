package com.pmi.tpd.core.versioning;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.versioning.impl.BuildVersionImpl;

/**
 * Versioning Class Utilities.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class BuildUtils {

    /**
     *
     */
    private BuildUtils() {
    }

    /**
     * Create BuildVersion with version passed by paremeter. the version as pattern
     * [Major].[Minor].[bugfix][-type].[BuildNumber]
     * <P>
     * exemple: 1.0.0.1,1.0.0-SNAPSHOT.4562
     * </P>
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.core.versioning.BuildVersion} object.
     */
    @Nonnull
    public static BuildVersion createBuildVersion(@Nonnull final String version) {
        final String[] ar = Assert.hasText(version).split("\\.");
        if (ar.length != 4) {
            throw new IllegalArgumentException("version doesn't contains build number");
        }
        String buildNumber = StringUtils.EMPTY;
        final int endIndex = 2;
        buildNumber = ar[3];
        // remove release type
        final String bugfix = ar[endIndex];
        final int i = bugfix.indexOf('-');
        if (i >= 0) {
            ar[endIndex] = bugfix.substring(0, i);
        }
        final String ver = StringUtils.join(ar, '.', 0, endIndex + 1);
        return new BuildVersionImpl(buildNumber, ver);
    }

    /**
     * Converts a {@link com.pmi.tpd.core.versioning.BuildVersion} to {@code String}.
     *
     * @param version
     *            a version
     * @return Returns a {@code String} representation of a {@link com.pmi.tpd.core.versioning.BuildVersion}
     */
    @Nonnull
    public static String toString(@Nonnull final BuildVersion version) {
        final String[] ar = version.getVersion().split("\\.");
        final StringBuilder ver = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < ar.length) {
                ver.append(ar[i]);
            } else {
                ver.append('0');
            }
            ver.append('.');
        }
        ver.append(version.getBuildNumber());
        return ver.toString();
    }

    /**
     * Converts a string representing a version to {@code Long}.
     *
     * @param version
     *            a version
     * @return Returns a {@code Long} representation of a string version.
     */
    @Nonnull
    public static Long toLong(@Nullable final String version) {
        long versionLong = 0;
        if (StringUtils.isNotEmpty(version)) {
            final String versionString = normalizeStringVersion(version);
            final String[] versions = versionString.split("\\.");
            for (int i = 0; i < versions.length; i++) {
                final int num = Integer.parseInt(versions[i]);
                final int exp = versions.length - i;
                versionLong += num * Math.pow(100.0, exp - 1);
            }
        }
        return versionLong;
    }

    /**
     * <p>
     * normalizeStringVersion.
     * </p>
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Nonnull
    public static String normalizeStringVersion(@Nonnull final String version) {

        final String versionString = version;
        // this will always be larger than what we need.
        final StringBuilder sb = new StringBuilder(versionString.length());
        boolean minus = false;
        final StringBuilder val = new StringBuilder(5);
        forloop: for (int i = 0; i < versionString.toCharArray().length; i++) {
            final char c = versionString.toCharArray()[i];
            if (Character.isDigit(c)) {
                val.append(c);
            } else if (c == '.') {
                sb.append(val.toString());
                val.setLength(0);
                sb.append('.');
                minus = false;
            } else if (c == '-') {
                minus = true;
            } else {
                if (!minus) {
                    break forloop;
                }
            }
        }
        sb.append(val.toString());
        return sb.toString();
    }

    /**
     * <p>
     * extractTargetVersionFromUpgradeClass.
     * </p>
     *
     * @param upgradeClassName
     *            the name of the upgrade task class e.g. <code>UpgradeHomeStructureTask</code>
     * @return the target version associated with the class name; empty string if the build number could not be found
     * @throws java.lang.IllegalArgumentException
     *             if {@code upgradeClassName} is null or empty.
     * @throws java.lang.RuntimeException
     *             if any.
     */
    @Nonnull
    public static String extractTargetVersionFromUpgradeClass(@Nonnull final String upgradeClassName) {
        Class<?> cl = null;
        try {
            cl = ClassUtils.forName(upgradeClassName, BuildUtils.class.getClassLoader());
        } catch (final Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return extractTargetVersionFromUpgradeClass(cl);
    }

    /**
     * <p>
     * extractTargetVersionFromUpgradeClass.
     * </p>
     *
     * @param upgradeClass
     *            a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    public static String extractTargetVersionFromUpgradeClass(final Class<?> upgradeClass) {
        final com.pmi.tpd.core.upgrade.UpgradeVersion version = upgradeClass
                .getAnnotation(com.pmi.tpd.core.upgrade.UpgradeVersion.class);
        if (version != null) {
            return version.targetVersion();
        }
        return StringUtils.EMPTY;
    }

    /**
     * <p>
     * extractBuildNumberFromUpgradeClass.
     * </p>
     *
     * @param upgradeClassName
     *            the name of the upgrade task class e.g. <code>UpgradeHomeStructureTask</code>
     * @return the build number associated with the class name; 0 if the build number could not be found
     * @throws java.lang.IllegalArgumentException
     *             if {@code upgradeClassName} is null or empty.
     * @throws java.lang.RuntimeException
     *             if any.
     */
    @Nonnull
    public static String extractBuildNumberFromUpgradeClass(@Nonnull final String upgradeClassName) {
        Class<?> cl = null;
        try {
            cl = ClassUtils.forName(upgradeClassName, BuildUtils.class.getClassLoader());
        } catch (final Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return extractBuildNumberFromUpgradeClass(cl);
    }

    /**
     * <p>
     * extractBuildNumberFromUpgradeClass.
     * </p>
     *
     * @param upgradeClass
     *            a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    public static String extractBuildNumberFromUpgradeClass(final Class<?> upgradeClass) {
        final com.pmi.tpd.core.upgrade.UpgradeVersion version = upgradeClass
                .getAnnotation(com.pmi.tpd.core.upgrade.UpgradeVersion.class);
        if (version != null) {
            return version.buildNumber();
        }
        return StringUtils.EMPTY;
    }

    /**
     * For a version, loop through & pull out numbers. For the first '.' leave as is, but for subsequent '.', add a '0'
     * in its place. Parse the result into a Double.
     * <p>
     * This should handle '1.10' > '1.1.1' & '1.2beta3' == '1.2'
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.Double} object.
     */
    public static Double getDoubleBuildNumber(final String version) {
        final String versionString = version;
        final StringBuilder sb = new StringBuilder(versionString.length()); // this will always be
        // larger than what we
        // need.
        boolean decSeen = false;
        boolean minus = false;
        final StringBuilder val = new StringBuilder(5);
        forloop: for (int i = 0; i < versionString.toCharArray().length; i++) {
            final char c = versionString.toCharArray()[i];
            if (Character.isDigit(c)) {
                val.append(c);
            } else if (c == '.') {
                sb.append(BuildUtils.normalisedVersion(val.toString()));
                val.setLength(0);
                if (!decSeen) {
                    decSeen = true;
                    sb.append('.');
                }
                minus = false;
            } else if (c == '-') {
                minus = true;
            } else {
                if (!minus) {
                    break forloop;
                }
            }
        }
        sb.append(BuildUtils.normalisedVersion(val.toString()));

        return Double.valueOf(sb.toString());
    }

    private static String normalisedVersion(final String version) {
        return normalisedVersion(version, 4);
    }

    private static String normalisedVersion(final String version, final int maxWidth) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%0" + maxWidth + 'd', Integer.valueOf(version)));
        return sb.toString();
    }

}
