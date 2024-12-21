package com.pmi.tpd.core.versioning.impl;

import java.util.Properties;

/**
 * <p>
 * BuildUtilsInfoSvnImpl class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class BuildUtilsInfoSvnImpl extends DefaultBuildUtilsInfo {

    /**
     * <p>
     * Constructor for BuildUtilsInfoSvnImpl.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     */
    public BuildUtilsInfoSvnImpl(final long minimumUpgradableBuildNumber, final String minimumUpgradableVersion) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion);
    }

    /**
     * <p>
     * Constructor for BuildUtilsInfoSvnImpl.
     * </p>
     *
     * @param minimumUpgradableBuildNumber
     *            a long.
     * @param minimumUpgradableVersion
     *            a {@link java.lang.String} object.
     * @param properties
     *            a {@link java.util.Properties} object.
     */
    public BuildUtilsInfoSvnImpl(final long minimumUpgradableBuildNumber, final String minimumUpgradableVersion,
            final Properties properties) {
        super(minimumUpgradableBuildNumber, minimumUpgradableVersion, properties);

    }

}
