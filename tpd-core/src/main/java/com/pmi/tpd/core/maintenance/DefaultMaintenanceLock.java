package com.pmi.tpd.core.maintenance;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.MoreExecutors;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.AddEvent;
import com.pmi.tpd.api.event.advisor.event.RemoveEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.maintenance.event.SystemMaintenanceEvent;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultMaintenanceLock implements IMaintenanceLock {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMaintenanceLock.class);

    /** */
    private final SystemMaintenanceEvent event;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final I18nService i18nService;

    /** */
    private final ExecutionList listeners;

    /** */
    private final IUser owner;

    /** */
    private final String token;

    /** */
    private volatile boolean locked;

    public DefaultMaintenanceLock(final IEventPublisher eventPublisher, final I18nService i18nService,
            final IEventAdvisorService<?> eventAdvisorService, final IUser owner, final String token) {
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.i18nService = i18nService;
        this.owner = checkNotNull(owner, "owner");
        this.token = checkNotNull(token, "token");

        event = new SystemMaintenanceEvent(eventAdvisorService.getEventType("performing-maintenance").orElseThrow(),
                "The system is unavailable while maintenance is being performed.",
                eventAdvisorService.getEventLevel("system-maintenance").orElseThrow(), token);
        listeners = new ExecutionList();
    }

    /**
     * @param listener
     */
    public void addListener(final Runnable listener) {
        listeners.add(listener, MoreExecutors.directExecutor());
    }

    @Nonnull
    @Override
    public IUser getOwner() {
        return owner;
    }

    @Nonnull
    @Override
    public String getUnlockToken() {
        return token;
    }

    public void lock() {
        Assert.state(!locked, "Already locked");
        locked = true;
        eventPublisher.publish(new AddEvent(this, event));
    }

    @Override
    public void unlock(@Nonnull final String token) {
        checkNotNull(token, "token");
        Assert.state(locked, "Not locked");

        // unlock is not secured beyond validating that the provided token is the correct unlock token. No permission
        // checks can be performed because the 'unlock' call is made from an unauthenticated context (the maintenance
        // lock screen) - no Spring Security processing is performed on the maintenance lock screen.
        if (event.isToken(token)) {
            eventPublisher.publish(new RemoveEvent(this, event));

            LOGGER.info("Maintenance has been completed. The system lock has been released");
            listeners.execute();
        } else {
            LOGGER.warn("An invalid token ({}) was supplied to attempt to unlock the system", token);
            throw new IncorrectTokenException(
                    i18nService.createKeyedMessage("app.service.maintenance.lock.incorrectunlocktoken"), token);
        }
    }
}
