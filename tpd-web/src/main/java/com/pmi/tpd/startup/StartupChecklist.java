package com.pmi.tpd.startup;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import com.google.common.collect.Ordering;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.startup.IStartupCheck;
import com.pmi.tpd.startup.check.HomeLockUtils;
import com.pmi.tpd.startup.check.HomeStartupCheck;
import com.pmi.tpd.web.context.ContextBoolean;
import com.pmi.tpd.web.context.ContextReference;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class StartupChecklist implements ApplicationListener<ApplicationContextEvent> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupChecklist.class);

    /** */
    private List<IStartupCheck> startupChecksList;

    /** */
    private static ContextBoolean checksDone = new ContextBoolean("app.startup.checksdone");

    /** */
    private static ContextBoolean validStartup = new ContextBoolean("app.startup.validstartup");

    /** */
    private static ContextReference<IStartupCheck> failedStartupCheck = new ContextReference<IStartupCheck>(

            "app.startup.failed.check");

    /** */
    private final IApplicationConfiguration applicationConfiguration;

    @Inject
    public StartupChecklist(final List<IStartupCheck> startupChecks,
            final IApplicationConfiguration applicationConfiguration) {
        startupChecksList = Ordering.from(new Comparator<IStartupCheck>() {

            @Override
            public int compare(final IStartupCheck o1, final IStartupCheck o2) {
                return o1.getOrder() - o2.getOrder();
            }
        }).<IStartupCheck> sortedCopy(Assert.notNull(startupChecks));
        this.applicationConfiguration = Assert.notNull(applicationConfiguration);
    }

    /**
     *
     */
    public static void reset() {
        checksDone.set(false);
    }

    @Override
    public void onApplicationEvent(final ApplicationContextEvent event) {

        if ((event instanceof ContextClosedEvent) || (event instanceof ContextStoppedEvent)) {

            // delete lock home file
            final HomeStartupCheck homeStartupCheck = HomeStartupCheck.getProductionCheck();

            if (((homeStartupCheck != null) && homeStartupCheck.isOk()) || applicationConfiguration.isDevMode()) {
                try {
                    HomeLockUtils.unLockHome(homeStartupCheck.getHome());

                } catch (final IOException e) {
                    LOGGER.error("Error during unlock home folder", e);
                }
            }
            for (final IStartupCheck check : startupChecksList) {
                if (check instanceof IShutdown) {
                    ((IShutdown) check).shutdown();
                }
            }

        }

    }

    public boolean shutdownOk() {
        try {
            HomeLockUtils.unLockHome(HomeStartupCheck.getProductionCheck().getHome());
        } catch (final Exception e) {
            // /CLOVER:OFF
            LOGGER.warn("error remove home file lock", e);
            // /CLOVER:ON
        }
        return true;
    }

    /**
     * Returns true if Portal started correctly. The first time this is called, it runs the checks, and then caches the
     * result.
     *
     * @return true if Portal started correctly.
     */
    public boolean startupOK() {
        if (!checksDone.get()) {
            validStartup.set(doStartupChecks());
            checksDone.set(true);

        }
        return validStartup.get();

    }

    private boolean doStartupChecks() {
        for (final IStartupCheck startupCheck : startupChecksList) {
            if (!startupCheck.isOk()) {
                // Log the Checker's fault message
                LOGGER.error(startupCheck.getFaultDescription());
                // Set the failedStartupCheck
                failedStartupCheck.set(startupCheck);
                return false;

            }

        }

        // All checks passed

        return true;

    }

    /**
     * Returns the StartupCheck that failed, if any.
     *
     * @return the StartupCheck that failed, if any.
     */
    public static IStartupCheck getFailedStartupCheck() {
        return failedStartupCheck.get();
    }

    /**
     * Allows an external operation to declare that the startup failed, and give the message to be displayed in Portal's
     * "Application is locked" web page.
     * <p>
     * This is used in ConsistencyLauncher, after the pre-startup checks have passed, but an error is thrown during the
     * actual startup.
     *
     * @param startupCheck
     *            The StartupCheck
     */
    public static void setFailedStartupCheck(final IStartupCheck startupCheck) {
        checksDone.set(true);
        validStartup.set(false);
        failedStartupCheck.set(startupCheck);
    }

}
