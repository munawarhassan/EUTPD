package com.pmi.tpd.core.upgrade;

import java.util.Date;

/**
 * Simple representation of an upgrade performed in history.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUpgradeHistoryItem {

    /**
     * For historical data, we do not have information about when upgrade tasks were performed. In this case, null may
     * be returned.
     *
     * @return the time when the upgrade was performed; may be null if it is unknown
     */
    Date getTimePerformed();

    /**
     * For historical data, this number may not represent a released build but instead the last upgrade task to run.
     * This is because often the release build number does not have an associated upgrade task.
     *
     * @return the build number that was being upgraded to which represents which version the instance was running at
     *         the time of upgrade.
     */
    String getTargetBuildNumber();

    /**
     * <p>
     * getOriginalBuildNumber.
     * </p>
     *
     * @return the previous build number before this upgrade, or the build number used to infer this history item if it
     *         was inferred.
     */
    String getOriginalBuildNumber();

    /**
     * <p>
     * the next version after that build number.
     * <p>
     * For example, if the target build number is <code>207</code>, the version returned would be <code>3.8</code>, as
     * it has a build number of <code>209</code>.
     *
     * @return the version that corresponds to the target build number.
     */
    String getTargetVersion();

    /**
     * <p>
     * getOriginalVersion.
     * </p>
     *
     * @return the previous version before this upgrade.
     */
    String getOriginalVersion();

    /**
     * <p>
     * isInferred.
     * </p>
     *
     * @return true if this was inferred from upgrade tasks; false otherwise
     */
    boolean isInferred();
}
