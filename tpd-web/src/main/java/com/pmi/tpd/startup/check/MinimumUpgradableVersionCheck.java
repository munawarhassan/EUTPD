package com.pmi.tpd.startup.check;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.startup.IStartupCheck;
import com.pmi.tpd.core.upgrade.IUpgradeManager;

/**
 * This is a database check that verifies that the data is not too old to be upgraded by this version of Application.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class MinimumUpgradableVersionCheck implements IStartupCheck {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(MinimumUpgradableVersionCheck.class);

    /** */
    private final IBuildUtilsInfo buildUtilsInfo;

    /** */
    private final IUpgradeManager upgradeManager;

    /**
     * @param buildUtilsInfo
     * @param upgradeManager
     */
    @Inject
    public MinimumUpgradableVersionCheck(@Nonnull final IBuildUtilsInfo buildUtilsInfo,
            @Nonnull final IUpgradeManager upgradeManager) {
        super();
        this.buildUtilsInfo = Assert.notNull(buildUtilsInfo);
        this.upgradeManager = Assert.notNull(upgradeManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Check Initial Version";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return 30;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOk() {
        if (databaseSetup()) {
            LOGGER.debug("Performing version check");

            final int databaseBuildVersionNumber = getDbBuildNumber();

            if (databaseBuildVersionNumber > 0 && databaseBuildVersionNumber < getMinimumUpgradableBuildNumber()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(
                        "Your data is too old to be upgraded." + " Minimum version required: %d, your version: %d",
                        getMinimumUpgradableBuildNumber(),
                        databaseBuildVersionNumber));
                }
                // We have an error so Application should not be started!
                return false;
            }
        }
        return true;
    }

    private boolean databaseSetup() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHtmlFaultDescription() {
        final StringBuilder message = new StringBuilder(512);
        message.append("<p>Failed to start due to your data being too old to"
                + " be able to be upgraded by this version of Portal.</p>");
        message.append("<p>Database version is: ").append(getDbBuildNumber()).append("</p>");
        message.append("<p>Minimum version required is: ").append(getMinimumUpgradableVersionString()).append("</p>");
        message.append("<p>You are running: ").append(buildUtilsInfo.getBuildInformation()).append("</p>");
        return message.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFaultDescription() {
        final StringBuffer message = new StringBuffer(512);
        message.append("Failed to start due to your data being too old to"
                + " be able to be upgraded by this version of Portal.\n");
        message.append("Database version is: ").append(getDbBuildNumber()).append("\n");
        message.append("Minimum version required is: ").append(getMinimumUpgradableVersionString()).append("\n");
        message.append("You are running: ").append(buildUtilsInfo.getBuildInformation()).append("\n");
        return message.toString();
    }

    private int getDbBuildNumber() {
        try {
            return Integer.parseInt(upgradeManager.getBuildNumber());
        } catch (final NumberFormatException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        return 0;
    }

    private long getMinimumUpgradableBuildNumber() {
        return buildUtilsInfo.getMinimumUpgradableBuildNumber();
    }

    private String getMinimumUpgradableVersionString() {
        return String.format("%s-#%d", buildUtilsInfo.getMinimumUpgradableVersion(), getMinimumUpgradableBuildNumber());
    }
}
