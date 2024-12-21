package com.pmi.tpd.core.bootstrap;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.util.IUncheckedOperation;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;

/**
 * Listens for {@link org.springframework.context.event.ContextRefreshedEvent} and creates the initial configuration for
 * the application. Checks whether the bootstrap operation must actually be performed and delegate to an operation.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class Bootstrapper extends AbstractSmartLifecycle {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    /** */

    private final IApplicationProperties propertiesService;

    /** */
    private final IUncheckedOperation<?> bootstrapOperation;

    /** */
    private final IBootstrapLockService lockService;

    @Inject
    public Bootstrapper(final IApplicationProperties propertiesService, final IUncheckedOperation<?> bootstrapOperation,
            final IBootstrapLockService lockService) {
        this.propertiesService = propertiesService;
        this.bootstrapOperation = bootstrapOperation;
        this.lockService = lockService;
    }

    /**
     * Bootstrapper should run after all other beans have finished initializing, but before anything else in the
     * lifecycle. This ensures the server is bootstrapped before plugins are started (for example).
     */
    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_BOOTSTRAP;
    }

    /**
     * Runs the injected {@link #bootstrapOperation bootstrap operation} with {@link Permission#SYS_ADMIN SYS_ADMIN}
     * permission.
     * <p>
     * Note: Any transaction created by the {@link #bootstrapOperation} must be committed before the
     * {@link IBootstrapLock BootstrapLock} is released, so it is important that no transaction exists before invoking
     * it.
     */
    @Override
    public void start() {

        if (propertiesService.isBootstrapped()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Application already setup, skipping bootstrap");
            }
        } else {
            // Critical section: only one instance at a time can perform the bootstrap operation
            lockService.getBootstrapLock().withLock(() -> {
                // Don't bootstrap if the app is already setup
                if (propertiesService.isBootstrapped()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Application already setup, skipping bootstrap");
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Performing bootstrap operation");
                    }
                    bootstrapOperation.perform();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Finished bootstrap operation");
                    }
                }

                return null;
            });
        }

        super.start();
    }

}
