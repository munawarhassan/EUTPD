package com.pmi.tpd.core.upgrade;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * IUpgradeManager interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUpgradeManager {

    /**
     * <p>
     * needSetup.
     * </p>
     *
     * @return a boolean.
     */
    boolean needSetup();

    /**
     * <p>
     * doSetupUpgrade.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> doSetupUpgrade();

    /**
     * <p>
     * doUpgradeIfNeededAndAllowed.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> doUpgradeIfNeededAndAllowed();

    /**
     * <p>
     * getUpgradeHistory.
     * </p>
     *
     * @return the history of upgrades performed on this instance of Application in reverse chronological order
     */
    List<IUpgradeHistoryItem> getUpgradeHistory();

    /**
     * Get the current build number from the database. This represents the level that this application is patched to.
     * This may be different to the current version if there are patches waiting to be applied.
     *
     * @return The version information from the database
     */
    String getBuildNumber();
}
