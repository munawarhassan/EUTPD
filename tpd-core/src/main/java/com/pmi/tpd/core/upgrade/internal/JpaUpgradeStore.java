package com.pmi.tpd.core.upgrade.internal;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.model.upgrade.QUpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.core.upgrade.IUpgradeTask;
import com.querydsl.jpa.JPQLQuery;

/**
 * <p>
 * JpaUpgradeStore class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class JpaUpgradeStore implements IUpgradeStore {

    /** */
    private final IUpgradeHistoryVersionRepository upgradeHistoryVersionRepository;

    /** */
    private final IUpgradeHistoryRepository upgradeHistoryRepository;

    /**
     * <p>
     * Constructor for JpaUpgradeStore.
     * </p>
     *
     * @param upgradeHistoryVersionRepository
     *                                        a
     *                                        {@link com.pmi.tpd.core.upgrade.internal.IUpgradeHistoryVersionRepository}
     *                                        object.
     * @param upgradeHistoryRepository
     *                                        a {@link com.pmi.tpd.core.upgrade.internal.IUpgradeHistoryRepository}
     *                                        object.
     */
    @Inject
    public JpaUpgradeStore(@Nonnull final IUpgradeHistoryVersionRepository upgradeHistoryVersionRepository,
            @Nonnull final IUpgradeHistoryRepository upgradeHistoryRepository) {
        this.upgradeHistoryRepository = Assert.notNull(upgradeHistoryRepository);
        this.upgradeHistoryVersionRepository = Assert.notNull(upgradeHistoryVersionRepository);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public UpgradeHistory addToUpgradeHistory(final IUpgradeTask upgradeTask, final IBuildUtilsInfo buildUtilsInfo) {
        UpgradeHistory upgradeHistory = UpgradeHistory.builder()
                .upgradeClass(upgradeTask.getClass().getName())
                .buildNumber(upgradeTask.getBuildNumber())
                .targetBuildNumber(buildUtilsInfo.getCurrentBuildNumber())
                .build();
        upgradeHistory = this.upgradeHistoryRepository.saveAndFlush(upgradeHistory);
        return upgradeHistory;
    }

    /** {@inheritDoc} */
    @Override
    public List<UpgradeHistory> getUpgradeHistoryItemFromTasks() {
        final QUpgradeHistory upgradeHistory = QUpgradeHistory.upgradeHistory;
        final JPQLQuery<UpgradeHistory> jpaQuery = upgradeHistoryRepository.from()
                .from(upgradeHistoryRepository.entity())
                .where(upgradeHistory.targetBuildNumber.isNull());

        return jpaQuery.fetch();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public UpgradeHistoryVersion createUpgradeVersionHistory(final IBuildUtilsInfo buildUtilsInfo) {
        final Timestamp timePerformed = new Timestamp(System.currentTimeMillis());
        final String currentBuildNumber = buildUtilsInfo.getCurrentBuildNumber();
        final String version = buildUtilsInfo.getCurrentLongVersion();

        // first check if the record already exists
        return this.upgradeHistoryVersionRepository.findById(currentBuildNumber).orElseGet(() -> {
            final UpgradeHistoryVersion item = UpgradeHistoryVersion.builder()
                    .targetBuildNumber(currentBuildNumber)
                    .targetVersion(version)
                    .timePerformed(timePerformed)
                    .build();
            return this.upgradeHistoryVersionRepository.saveAndFlush(item);
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<UpgradeHistory> findUpgradeHistory() throws DataAccessException {
        return Lists.newArrayList(this.upgradeHistoryRepository.findAll());
    }

    /** {@inheritDoc} */
    @Override
    public List<UpgradeHistoryVersion> findUpgradeHistoryVersion() {
        return this.upgradeHistoryVersionRepository.findAll();
    }

    /**
     * <p>
     * upgradeHistoryVersionExists.
     * </p>
     *
     * @param buildNumber
     *                    a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean upgradeHistoryVersionExists(final String buildNumber) {
        return this.upgradeHistoryVersionRepository.existsById(buildNumber);
    }

    /**
     * <p>
     * upgradeHistoryExists.
     * </p>
     *
     * @param id
     *           a {@link java.lang.Long} object.
     * @return a boolean.
     */
    public boolean upgradeHistoryExists(final Long id) {
        return this.upgradeHistoryRepository.existsById(id);
    }

    /**
     * For test.
     */
    public void clearHistory() {
        this.upgradeHistoryRepository.deleteAll();
        this.upgradeHistoryVersionRepository.deleteAll();
    }

}
