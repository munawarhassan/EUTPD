package com.pmi.tpd.core;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pmi.tpd.core.upgrade.internal.IUpgradeHistoryRepository;
import com.pmi.tpd.core.upgrade.internal.IUpgradeHistoryVersionRepository;
import com.pmi.tpd.core.upgrade.internal.IUpgradeStore;
import com.pmi.tpd.core.upgrade.internal.JpaUpgradeHistoryRepository;
import com.pmi.tpd.core.upgrade.internal.JpaUpgradeHistoryVersionRepository;
import com.pmi.tpd.core.upgrade.internal.JpaUpgradeStore;

@Configuration
public class UpgradeConfig {

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IUpgradeHistoryRepository upgradeHistoryRepository(final EntityManager entityManager) {
        return new JpaUpgradeHistoryRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IUpgradeHistoryVersionRepository upgradeHistoryVersionRepository(final EntityManager entityManager) {
        return new JpaUpgradeHistoryVersionRepository(entityManager);
    }

    @Bean
    public static IUpgradeStore upgradeStore(final IUpgradeHistoryVersionRepository upgradeHistoryVersionRepository,
        final IUpgradeHistoryRepository upgradeHistoryRepository) {
        return new JpaUpgradeStore(upgradeHistoryVersionRepository, upgradeHistoryRepository);
    }
}
