package com.pmi.tpd.core.upgrade.internal;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.UpgradeConfig;
import com.pmi.tpd.core.upgrade.IUpgradeTask;

@Configuration
@ContextConfiguration(classes = { UpgradeConfig.class, UpgradeStoreIT.class })
public class UpgradeStoreIT extends BaseDaoTestIT {

    @Inject
    private IUpgradeStore upgradeStore;

    @Bean
    public static IUpgradeStore jpaUpgradeStore(final IUpgradeHistoryVersionRepository upgradeHistoryVersionRepository,
        final IUpgradeHistoryRepository upgradeHistoryRepository) {
        return new JpaUpgradeStore(upgradeHistoryVersionRepository, upgradeHistoryRepository);
    }

    @Test
    public void addToUpgradeHistory() {
        final IUpgradeTask task = mock(IUpgradeTask.class);
        when(task.getBuildNumber()).thenReturn("");
        final IBuildUtilsInfo buildUtilsInfo = mock(IBuildUtilsInfo.class);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("");

        upgradeStore.addToUpgradeHistory(task, buildUtilsInfo);
    }
}
