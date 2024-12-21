package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.scheduler.exec.CompositeRunableTask;

/**
 * Decorates another {@link MaintenanceTask}, putting the system in maintenance mode before executing the delegate task.
 * Regardless of the outcome of the delegate task, the system will be unlocked.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MaintenanceModePhase extends CompositeRunableTask {

    /** */
    private final MaintenanceApplicationEvent maintenanceEvent;

    /** */
    private final IMaintenanceModeHelper maintenanceModeHelper;

    /**
     * @param steps
     * @param totalWeight
     * @param maintenanceEvent
     * @param maintenanceModeHelper
     */
    protected MaintenanceModePhase(final Step[] steps, final int totalWeight,
            final MaintenanceApplicationEvent maintenanceEvent, final IMaintenanceModeHelper maintenanceModeHelper) {
        super(steps, totalWeight);

        this.maintenanceEvent = maintenanceEvent;
        this.maintenanceModeHelper = maintenanceModeHelper;
    }

    @Override
    public void run() {
        // all of the work should happen in maintenance mode and the scheduler should be paused; decorate the task we
        // enclose
        maintenanceModeHelper.lock(maintenanceEvent);
        try {
            super.run();
        } finally {
            maintenanceModeHelper.unlock(maintenanceEvent);
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    public static class Builder extends CompositeRunableTask.AbstractBuilder<Builder> {

        /** */
        private final IMaintenanceModeHelper maintenanceModeHelper;

        /** */
        private MaintenanceApplicationEvent maintenanceEvent;

        /**
         * @param maintenanceModeHelper
         */
        public Builder(final IMaintenanceModeHelper maintenanceModeHelper) {
            this.maintenanceModeHelper = maintenanceModeHelper;
        }

        // NOT REMOVE: for mockito proxying
        @Override
        public Builder add(final IRunnableTask step, final int weight) {
            return super.add(step, weight);
        }

        /**
         * @param event
         * @return
         */
        public Builder event(final MaintenanceApplicationEvent event) {
            this.maintenanceEvent = event;
            return this;
        }

        @Override
        @Nonnull
        public MaintenanceModePhase build() {
            return new MaintenanceModePhase(steps.toArray(new Step[steps.size()]), totalWeight, maintenanceEvent,
                    maintenanceModeHelper);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
