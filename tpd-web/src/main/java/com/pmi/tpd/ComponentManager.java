package com.pmi.tpd;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.InfrastructureException;
import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.core.inject.ComponentManagerFactoryBean;
import com.pmi.tpd.core.lifecycle.ApplicationBeforeStartEvent;
import com.pmi.tpd.core.lifecycle.UpgradeSuccessEvent;
import com.pmi.tpd.core.upgrade.IUpgradeManager;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.upgrade.UpgradeLauncher;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class ComponentManager extends ComponentManagerFactoryBean implements IShutdown {

    /**
     * The state of the {@link ComponentManager}.
     *
     * @since 1.0
     */
    public interface State {

        /**
         * Have the components registered been including plugin components.
         *
         * @return true if components have been registered.
         */
        boolean isComponentsRegistered();

        /**
         * Has the {@link ComponentManager} started.
         *
         * @return true if the component manager has started.
         */
        boolean isStarted();
    }

    /** */
    public static final String EXTENSION_PROVIDER_PROPERTY = "app.extension.container.provider";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentManager.class);

    // private static final CopyOnWriteMap<String, ServiceTracker> serviceTrackerCache =
    // CopyOnWriteMap.newHashMap();

    /** */
    private volatile State state = StateImpl.NOT_STARTED;

    /** */
    private final IEventPublisher eventPublisher;

    /**
     * @param eventPublisher
     *            a event publisher
     */
    @Inject
    public ComponentManager(@Nonnull final IEventPublisher eventPublisher) {
        this.eventPublisher = Assert.notNull(eventPublisher);
        eventPublisher.register(this);
    }

    /**
     * Initialisation registers components and then registers extensions.
     */
    public void initialise() {
        eventPublisher.publish(new ApplicationBeforeStartEvent());
        registerComponents();
    }

    /**
     * @param event
     */
    @EventListener
    public void onUpgradeSuccessEvent(final UpgradeSuccessEvent event) {
        try {
            initialise();
            start();
        } catch (final Exception e) {
            LOGGER.error(
                "An exception occurred while ComponentManager " + "was initialising. Portal not started properly. ",
                e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDestroy() {
        shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        try {
            state = StateImpl.NOT_STARTED;
            eventPublisher.unregister(this);
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * calls {@link ComponentManager#quickStart()}.
     */
    public synchronized void start() {
        quickStart();
    }

    /**
     *
     */
    public void quickStart() {
        state = StateImpl.COMPONENTS_REGISTERED;

        // now start all registered components
        final List<IStartable> startableComponents = getInjector().getComponentInstancesOfType(IStartable.class);
        if (startableComponents != null) {
            for (final IStartable startable : startableComponents) {
                try {
                    // skip componentManager
                    if (startable instanceof ComponentManager) {
                        continue;
                    }
                    startable.start();
                } catch (final Exception e) {
                    LOGGER.error("Error occurred while starting component '" + startable.getClass().getName() + "'.",
                        e);
                    throw new InfrastructureException(
                            "Error occurred while starting component '" + startable.getClass().getName() + "'.", e);
                }
            }
        }
        state = StateImpl.STARTED;
    }

    /**
     * What {@link State} is the {@link ComponentManager} in.
     *
     * @return the current state.
     */
    public State getState() {
        return state;
    }

    /**
     * This method registers all components with the internal injector container.
     */
    private void registerComponents() {

    }

    /**
     * Returns a singleton instance of this class.
     *
     * @return a singleton instance of this class
     */
    public static ComponentManager getInstance() {
        return getInjector().getComponentInstanceOfType(ComponentManager.class);
    }

    /**
     * Retrieves and returns a component which is an instance of given class.
     * <p/>
     *
     * @param clazz
     *            class to find a component instance by
     * @return found component
     * @param <T>
     *            a returned type object.
     */
    public static <T> T getComponentInstance(final Class<T> clazz) {
        return getInjector().getComponentInstanceOfType(clazz);
    }

    /**
     * This method is for testing purposes only.
     *
     * @param args
     *            arguments array
     */
    public static void main(final String[] args) {
        for (int i = 0; i < 300; i++) {
            ComponentManager.getInstance().initialise();
        }
    }

    /**
     * @return
     */
    public IApplicationProperties getApplicationProperties() {

        return getInjector().getComponentInstanceOfType(IApplicationProperties.class);
    }

    /**
     * @return
     */
    public IApplicationConfiguration getGlobalApplicationConfiguration() {
        return getInjector().getComponentInstanceOfType(IApplicationConfiguration.class);
    }

    /**
     * @return
     */
    public UpgradeLauncher getUpgradeLauncher() {
        return getInjector().getComponentInstanceOfType(UpgradeLauncher.class);
    }

    /**
     * @return
     */
    public IUpgradeManager getUpgradeManager() {
        return getInjector().getComponentInstanceOfType(IUpgradeManager.class);
    }

    /**
     * @return
     */
    public IUserPreferencesManager getUserPreferencesManager() {
        return getInjector().getComponentInstanceOfType(IUserPreferencesManager.class);
    }

    public IEventPublisher getEventPublisher() {
        return getInjector().getComponentInstanceOfType(IEventPublisher.class);
    }

    public IAuthenticationContext getAuthenticationContext() {
        return getInjector().getComponentInstanceOfType(IAuthenticationContext.class);
    }

    /**
     *
     */
    private enum StateImpl implements State {
        /**
         * Not registered, plugins haven't started.
         */
        NOT_STARTED {

            @Override
            public boolean isComponentsRegistered() {
                return false;
            }

            @Override
            public boolean isStarted() {
                return false;
            }
        },
        /**
         * All components registered including plugin components and plugin system has started.
         */
        COMPONENTS_REGISTERED {

            @Override
            public boolean isComponentsRegistered() {
                return true;
            }

            @Override
            public boolean isStarted() {
                return false;
            }
        },
        /**
         * All components registered including plugin components and plugin system has started.
         */
        STARTED {

            @Override
            public boolean isComponentsRegistered() {
                return true;
            }

            @Override
            public boolean isStarted() {
                return true;
            }
        }
    }

}
