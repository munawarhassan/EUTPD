package com.pmi.tpd.core.upgrade.internal;

import javax.persistence.EntityManager;

import com.pmi.tpd.core.model.upgrade.QUpgradeHistoryVersion;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class JpaUpgradeHistoryVersionRepository extends DefaultJpaRepository<UpgradeHistoryVersion, String>
        implements IUpgradeHistoryVersionRepository {

    /**
     *
     */
    public JpaUpgradeHistoryVersionRepository(final EntityManager entityManager) {
        super(UpgradeHistoryVersion.class, entityManager);
    }

    @Override
    public QUpgradeHistoryVersion entity() {
        return QUpgradeHistoryVersion.upgradeHistoryVersion;
    }

}
