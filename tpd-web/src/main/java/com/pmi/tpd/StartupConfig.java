package com.pmi.tpd;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.startup.IStartupCheck;
import com.pmi.tpd.core.upgrade.IUpgradeManager;
import com.pmi.tpd.core.upgrade.internal.IUpgradeStore;
import com.pmi.tpd.core.upgrade.internal.UpgradeManagerImpl;
import com.pmi.tpd.startup.ServerLifecycle;
import com.pmi.tpd.startup.StartupChecklist;
import com.pmi.tpd.upgrade.UpgradeLauncher;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@ComponentScan({ "com.pmi.tpd.upgrade.task", "com.pmi.tpd.startup.check" })
public class StartupConfig {

    @Bean
    public static IEventAdvisorService<?> eventAdvisorService() {
        return EventAdvisorService.getInstance();
    }

    @Bean
    public static IUpgradeManager upgradeManager(@Nonnull final IUpgradeStore upgradeStore,
        @Nonnull final IBuildUtilsInfo buildUtilsInfo,
        @Nonnull final IApplicationProperties applicationProperties,
        @Nonnull final IApplicationConfiguration applicationConfiguration) {
        return new UpgradeManagerImpl(upgradeStore, buildUtilsInfo, applicationProperties, applicationConfiguration,
                null);
    }

    @Bean
    public static StartupChecklist startupChecklist(final List<IStartupCheck> startupChecks,
        final IApplicationConfiguration applicationConfiguration) {
        return new StartupChecklist(startupChecks, applicationConfiguration);
    }

    @Bean
    public static UpgradeLauncher upgradeLauncher(@Nonnull final IUpgradeManager upgradeManager,
        final StartupChecklist startupChecklist,
        final IEventAdvisorService<?> eventAdvisorService) {
        return new UpgradeLauncher(upgradeManager, eventAdvisorService, startupChecklist);
    }

    @Bean
    public ServerLifecycle serverLifecycle(final ApplicationEventPublisher eventPublisher) {
        return new ServerLifecycle(eventPublisher);
    }

}
