package com.pmi.tpd.core.upgrade.internal;

import javax.persistence.EntityManager;

import com.pmi.tpd.core.model.upgrade.QUpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class JpaUpgradeHistoryRepository extends DefaultJpaRepository<UpgradeHistory, Long>
        implements IUpgradeHistoryRepository {

    /**
     * @param entityManager
     */
    public JpaUpgradeHistoryRepository(final EntityManager entityManager) {
        super(UpgradeHistory.class, entityManager);
    }

    @Override
    public QUpgradeHistory entity() {
        return QUpgradeHistory.upgradeHistory;
    }

}
