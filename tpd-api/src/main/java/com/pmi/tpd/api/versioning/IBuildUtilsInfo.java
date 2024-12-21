package com.pmi.tpd.api.versioning;

import java.util.Date;
import java.util.Properties;

/**
 * @since 1.0
 * @author Christophe Friederich
 */
public interface IBuildUtilsInfo {

    /**
     * The name of the properties file that contains the build properties.
     */
    String BUILD_VERSIONS_PROPERTIES = "build-versions.properties";

    // private static String KEY_BRANCH = "scm.branch";

    // private static String KEY_BUILD_USERNAME = "scm.build.user.name=";

    // private static String KEY_BUILD_USER_EMAIL = "scm.build.user.email";

    /** */
    String KEY_BUILD_TIME = "scm.build.time";

    /** */
    String KEY_COMMIT_ID = "scm.commit.id";

    /** */
    String KEY_APP_VERSION = "scm.commit.version";

    // private static String KEY_COMMIT_USERNAME = "scm.commit.user.name";

    // private static String KEY_COMMIT_USER_EMAIL = "scm.commit.user.email";

    // private static String KEY_COMMIT_MSG_FULL = "scm.commit.message.full";

    // private static String KEY_COMMIT_MSG_SHORT = "scm.commit.message.short";

    // private static String KEY_COMMIT_TIME = "scm.commit.time";

    /** */
    String REVISION_VERSION = "revision.version";

    /**
     * Gets the current version of Application.
     *
     * @return the current version of Application
     */
    String getVersion();

    /**
     * Gets the current build number of Application.
     *
     * @return the current build number of Application
     */
    String getCurrentBuildNumber();

    /**
     * Gets the current concatenation of version and build number of Application.
     *
     * @return a {@link java.lang.String} object.
     */
    String getCurrentLongVersion();

    /**
     * Gets the minimal build number that Application can upgrade from.
     *
     * @return the minimal build number that Application can upgrade from
     */
    long getMinimumUpgradableBuildNumber();

    /**
     * Gets the date this version of Application was built on.
     *
     * @return the date this version of Application was built on
     */
    Date getCurrentBuildDate();

    /**
     * Gets a build information summary as a String.
     *
     * @return a build information summary
     */
    String getBuildInformation();

    /**
     * Returns the id of the SCM commit that was used to make this build of Application. This method returns an empty
     * string if Application was built from the source distribution.
     *
     * @return the SCM commit id that was used to make this build of Application.
     */
    String getCommitId();

    /**
     * Get the minimum version of Application that can be upgraded to this instance version.
     *
     * @return the minimum version that can be upgraded.
     */
    String getMinimumUpgradableVersion();

    /**
     * <p>
     * getBuildProperties.
     * </p>
     *
     * @return a {@link java.util.Properties} object.
     */
    Properties getBuildProperties();

}
