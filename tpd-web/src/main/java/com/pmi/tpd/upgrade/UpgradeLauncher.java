package com.pmi.tpd.upgrade;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.publisher.IEventPublisherAware;
import com.pmi.tpd.core.lifecycle.UpgradeSuccessEvent;
import com.pmi.tpd.core.upgrade.IUpgradeManager;
import com.pmi.tpd.startup.StartupChecklist;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class UpgradeLauncher implements ApplicationListener<ApplicationContextEvent>, IEventPublisherAware {

    /** */
    private static final String EVENT_UPGRADING = "upgrading";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeLauncher.class);

    /** */
    private IEventPublisher eventPublisher;

    /** */
    private final IUpgradeManager upgradeManager;

    /** */
    private final StartupChecklist startupChecklist;

    private final IEventAdvisorService<?> eventAdvisorService;

    /**
     * @param upgradeManager
     * @param applicationConfiguration
     * @param eventService
     */
    @Inject
    public UpgradeLauncher(@Nonnull final IUpgradeManager upgradeManager,
            final IEventAdvisorService<?> eventAdvisorService, final StartupChecklist startupChecklist) {
        this.upgradeManager = checkNotNull(upgradeManager, "upgradeManager");
        this.eventAdvisorService = checkNotNull(eventAdvisorService, "eventAdvisorService");
        this.startupChecklist = checkNotNull(startupChecklist, "startupChecklist");
    }

    @Override
    public void setEventPublisher(@Nonnull final IEventPublisher eventPublisher) {
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
    }

    @Override
    public void onApplicationEvent(final ApplicationContextEvent event) {
        final ApplicationContext applicationContext = event.getApplicationContext().getParent();
        // accept only root context
        if (applicationContext == null
                && (event instanceof ContextStartedEvent || event instanceof ContextRefreshedEvent)) {
            this.checkIfUpgradeNeeded();
        }

    }

    /**
     * This will invoke the {@link IUpgradeManager} to see if an upgrade is needed.
     * <p/>
     * This is run at Portal startup time and can be invoked later if you need to restart Portal.
     */
    public void checkIfUpgradeNeeded() {
        if (!upgradeManager.needSetup()) {
            // no upgrade if no need setup
            eventPublisher.publish(UpgradeSuccessEvent.empty());
        } else if (upgradeManager.needSetup()) {
            // no upgrade if need setup before
            LOGGER.info("not setup yet - not upgrading");
        } else if (startupChecklist.startupOK()) {
            LOGGER.info("Application startup checks completed successfully.");
            // Add a warning that an upgrade is in progress
            final EventType eventType = eventAdvisorService.getEventType(EVENT_UPGRADING).orElseThrow();
            final Event upgradingEvent = new Event(eventType, "Application is currently being upgraded",
                    EventLevel.WARNING);

            eventAdvisorService.publishEvent(upgradingEvent);

            try {
                final Collection<String> errors = upgradeManager.doUpgradeIfNeededAndAllowed();
                addEventsForErrors(errors);
                if (errors.isEmpty()) {
                    eventPublisher.publish(UpgradeSuccessEvent.empty());
                }
            } catch (final Throwable e) {
                LOGGER.error("Exception whilst trying to upgrade: " + e.getMessage(), e);
            } finally {
                eventAdvisorService.discardEvent(upgradingEvent);
            }

        } else {
            LOGGER.error("Startup check failed. Application will be locked.");
            final EventType eventType = eventAdvisorService
                    .getEventType(StartupChecklist.getFailedStartupCheck().getName())
                    .orElseThrow();
            final Event event = new Event(eventType, StartupChecklist.getFailedStartupCheck().getFaultDescription(),
                    EventLevel.ERROR);
            eventAdvisorService.getEventContainer().publishEvent(event);
        }
    }

    private void addEventsForErrors(final Collection<String> errors) {
        for (final String exception : errors) {
            final EventType eventType = eventAdvisorService.getEventType(EVENT_UPGRADING).orElseThrow();
            final EventLevel eventLevel = eventAdvisorService.getEventLevel(EventLevel.ERROR).orElseThrow();
            final Event errorEvent = new Event(eventType, "An error occurred performing Application upgrade", exception,
                    eventLevel);
            eventAdvisorService.publishEvent(errorEvent);
        }
    }

}
