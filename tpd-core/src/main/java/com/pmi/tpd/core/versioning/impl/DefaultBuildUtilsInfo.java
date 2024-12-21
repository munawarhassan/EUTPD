package com.pmi.tpd.core.versioning.impl;

import java.util.Date;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * <p>
 * DefaultBuildUtilsInfo class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultBuildUtilsInfo extends AbstractBuildUtilsInfo {

    /**
     * <p>
     * Constructor for DefaultBuildUtilsInfo.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public DefaultBuildUtilsInfo(final long minimumUpgradableBuildNumber,
            @Nonnull final String minimumUpgradableVersion) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion);
    }

    /**
     * <p>
     * Constructor for DefaultBuildUtilsInfo.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     * @param properties
     *            a {@link java.util.Properties} object.
     */
    public DefaultBuildUtilsInfo(final long minimumUpgradableBuildNumber,
            @Nonnull final String minimumUpgradableVersion, @Nonnull final Properties properties) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion, properties);

    }

    /**
     * <p>
     * Constructor for DefaultBuildUtilsInfo.
     * </p>
     *
     * @param version
     *            a {@link java.lang.String} object.
     * @param buildTime
     *            a {@link java.util.Date} object.
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public DefaultBuildUtilsInfo(@Nonnull final String version, @Nonnull final Date buildTime,
            final long minimumUpgradableBuildNumber, @Nonnull final String minimumUpgradableVersion) {
        super(version, buildTime, minimumUpgradableBuildNumber, minimumUpgradableVersion);
    }

}
