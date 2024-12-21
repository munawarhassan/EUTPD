package com.pmi.tpd.core.maintenance;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.hazelcast.collection.ISet;
import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastMaintenanceModeHelper
        implements IMaintenanceModeHelper, ItemListener<MaintenanceApplicationEvent> {

    /** */
    private final Object lock = new Object();

    /** */
    private final IMaintenanceModeHelper localMaintenanceModeHelper;

    /** */
    private final ISet<MaintenanceApplicationEvent> maintenanceEvents;

    /** */
    private UUID listenerRegistrationId;

    /**
     * @param maintenanceEvents
     * @param localMaintenanceModeHelper
     */
    public HazelcastMaintenanceModeHelper(final ISet<MaintenanceApplicationEvent> maintenanceEvents,
            final IMaintenanceModeHelper localMaintenanceModeHelper) {
        this.localMaintenanceModeHelper = localMaintenanceModeHelper;
        this.maintenanceEvents = maintenanceEvents;
    }

    @PreDestroy
    public void destroy() {
        if (listenerRegistrationId != null) {
            maintenanceEvents.removeItemListener(listenerRegistrationId);
        }
    }

    @PostConstruct
    public void init() {
        synchronized (lock) {
            listenerRegistrationId = maintenanceEvents.addItemListener(this, true);

            for (final MaintenanceApplicationEvent event : maintenanceEvents) {
                localMaintenanceModeHelper.lock(event);
            }
        }
    }

    @Override
    public void itemAdded(final ItemEvent<MaintenanceApplicationEvent> item) {
        synchronized (lock) {
            localMaintenanceModeHelper.lock(item.getItem());
        }
    }

    @Override
    public void itemRemoved(final ItemEvent<MaintenanceApplicationEvent> item) {
        synchronized (lock) {
            localMaintenanceModeHelper.unlock(item.getItem());
        }
    }

    @Override
    public void lock(@Nonnull final MaintenanceApplicationEvent event) {
        maintenanceEvents.add(event);
    }

    @Override
    public void unlock(@Nonnull final MaintenanceApplicationEvent event) {
        maintenanceEvents.remove(event);
    }
}
