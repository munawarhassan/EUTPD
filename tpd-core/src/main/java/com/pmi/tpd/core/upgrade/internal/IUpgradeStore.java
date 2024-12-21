package com.pmi.tpd.core.upgrade.internal;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.core.upgrade.IUpgradeTask;

/**
 * <p>
 * IUpgradeStore interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUpgradeStore {

    /**
     * Selects the most recent upgrade task that doesn't have a target build, and then extracts the build number from
     * the task class name so that we can infer a version which was upgraded to.
     *
     * @return an {@link com.pmi.tpd.core.upgrade.IUpgradeHistoryItem} representing the inferred upgrade;
     *         null if all upgrade tasks have an associated target build number
     */
    List<UpgradeHistory> getUpgradeHistoryItemFromTasks();

    /**
     * <p>
     * createUpgradeVersionHistory.
     * </p>
     *
     * @param buildUtilsInfo
     *            a {@link com.pmi.tpd.api.versioning.IBuildUtilsInfo} object.
     * @return a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion} object.
     */
    UpgradeHistoryVersion createUpgradeVersionHistory(IBuildUtilsInfo buildUtilsInfo);

    /**
     * <p>
     * addToUpgradeHistory.
     * </p>
     *
     * @param upgradeTask
     *            a {@link com.pmi.tpd.core.upgrade.IUpgradeTask} object.
     * @param buildUtilsInfo
     *            a {@link com.pmi.tpd.api.versioning.IBuildUtilsInfo} object.
     * @return a {@link com.pmi.tpd.core.model.upgrade.UpgradeHistory} object.
     */
    UpgradeHistory addToUpgradeHistory(final IUpgradeTask upgradeTask, IBuildUtilsInfo buildUtilsInfo);

    /**
     * <p>
     * findUpgradeHistory.
     * </p>
     *
     * @throws org.springframework.dao.DataAccessException
     *             if any.
     * @return a {@link java.util.List} object.
     */
    List<UpgradeHistory> findUpgradeHistory() throws DataAccessException;

    /**
     * <p>
     * findUpgradeHistoryVersion.
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    List<UpgradeHistoryVersion> findUpgradeHistoryVersion();
}
