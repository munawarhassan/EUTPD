package com.pmi.tpd.core.event.advisor.servlet;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.core.event.advisor.IContainerFactory;
import com.pmi.tpd.core.event.advisor.config.ConfigurationEventException;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.config.XmlEventConfig;
import com.pmi.tpd.core.event.advisor.support.DefaultEventConfig;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@SuppressWarnings("serial")
public final class ServletEventAdvisor implements IEventAdvisorAccessor {

  /**
   * When used in a web environment, the {@link IEventConfig} will be exposed in
   * the {@code ServletContext} under an
   * attribute with this key.
   */
  public static final String ATTR_CONFIG = ServletEventAdvisor.class.getName() + ":Config";

  /**
   * When used in a web environment, the {@link IEventContainer} will be exposed
   * in the {@code ServletContext} under
   * an attribute with this key.
   */
  public static final String ATTR_EVENT_CONTAINER = ServletEventAdvisor.class.getName() + ":EventContainer";

  /**
   * During {@link #initialize(javax.servlet.ServletContext) initialisation}, the
   * {@code ServletContext} is examined
   * for an {@code init-param} with this name. If one is found, it controls the
   * location from which the configuration
   * is loaded. Otherwise, {@link XmlEventConfig#DEFAULT_CONFIGURATION_FILE} is
   * used as the default.
   */
  public static final String PARAM_CONFIG_LOCATION = "eventConfigLocation";

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(ServletEventAdvisor.class);

  private transient static ServletEventAdvisor INSTANCE = null;

  /** */
  private IEventConfig config;

  /** */
  private IEventContainer eventContainer;

  @Nullable
  public static ServletEventAdvisor getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ServletEventAdvisor();
    }
    return INSTANCE;
  }

  /**
   * Retrieves the statically-bound {@link IEventConfig}.
   * <p/>
   * Note: If has not been {@link #initialize(String) initialised}, this method
   * <i>will not</i> initialise it. Before
   * attempting to use, it is left to the application developer to ensure it has
   * been correctly initialised.
   *
   * @return the working configuration
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public IEventConfig getConfig() {
    isTrue(config != null, "EventsServlet.getConfig() was called before initialisation");

    return config;
  }

  /**
   * Attempts to retrieve the {@link IEventConfig} from the provided
   * {@code ServletContext} under the key
   * {@link #ATTR_CONFIG} before falling back on {@link #getConfig()}.
   * <p/>
   * Note: If has not been {@link #initialize(ServletContext) initialised}, this
   * method <i>will not</i> initialise it.
   * Before attempting to use, it is left to the application developer to ensure
   * it has been correctly initialised.
   *
   * @param context
   *                the servlet context
   * @return the working configuration
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public IEventConfig getConfig(@Nonnull final ServletContext context) {
    final Object attribute = checkNotNull(context, "context").getAttribute(ATTR_CONFIG);
    if (attribute != null) {
      return (IEventConfig) attribute;
    }
    return getConfig();
  }

  /**
   * Retrieves the statically-bound {@link IEventContainer}.
   * <p/>
   * Note: If has not been {@link #initialize(String) initialised}, this method
   * <i>will not</i> initialise it. Before
   * attempting to use, it is left to the application developer to ensure it has
   * been correctly initialised.
   *
   * @return the working event container
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public IEventContainer getEventContainer() {
    isTrue(eventContainer != null, "EventsServlet.getEventContainer() was called before initialisation");

    return eventContainer;
  }

  /**
   * Attempts to retrieve the {@link IEventContainer} from the provided
   * {@code ServletContext} under the key
   * {@link #ATTR_EVENT_CONTAINER} before falling back on the statically-bound
   * instance.
   * <p/>
   * Note: If has not been {@link #initialize(ServletContext) initialised}, this
   * method <i>will not</i> initialise it.
   * Before attempting to use, it is left to the application developer to ensure
   * it has been correctly initialised.
   *
   * @param context
   *                the servlet context
   * @return the working event container
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public IEventContainer getEventContainer(@Nonnull final ServletContext context) {
    final Object attribute = checkNotNull(context, "context").getAttribute(ATTR_EVENT_CONTAINER);
    if (attribute != null) {
      return (IEventContainer) attribute;
    }
    return getEventContainer();
  }

  /**
   * Initialises Event Advisor, additionally binding the {@link IEventConfig} and
   * {@link IEventContainer} to the
   * provided {@code ServletContext} and performing any
   * {@link ApplicationEventCheck}s which have been configured.
   * <p/>
   * If the {@link #PARAM_CONFIG_LOCATION} {@code init-param} is set, it is used
   * to determine the location of the
   * configuration file. Otherwise,
   * {@link XmlEventConfig#DEFAULT_CONFIGURATION_FILE} is assumed.
   * <p/>
   * Note: This method is <i>not synchronised</i> and <i>not thread-safe</i>. It
   * is left to the <i>calling code</i> to
   * ensure proper synchronisation. The easiest way to do this is to initialise by
   * adding the
   * {@link ServletEventContextListener} to {@code web.xml}.
   *
   * @param context
   *                the servlet context
   */
  public static void initialize(@Nonnull final ServletContext context) {
    final String location = StringUtils.defaultIfEmpty(
        checkNotNull(context, "context").getInitParameter(PARAM_CONFIG_LOCATION),
        XmlEventConfig.DEFAULT_CONFIGURATION_FILE);

    initialize(location);

    context.setAttribute(ATTR_CONFIG, INSTANCE.config);
    context.setAttribute(ATTR_EVENT_CONTAINER, INSTANCE.eventContainer);

    final List<IApplicationEventCheck<ServletContext>> checks = INSTANCE.config
        .<ServletContext>getApplicationEventChecks();
    for (final IApplicationEventCheck<ServletContext> check : checks) {
      check.check(INSTANCE, context);
    }
  }

  /**
   * Initialises, loading its configuration from the provided location, and sets
   * the statically-bound instances of
   * {@link IEventConfig} and {@link IEventContainer}.
   * <p/>
   * If the location provided is {@code null} or empty,
   * {@link XmlEventConfig#DEFAULT_CONFIGURATION_FILE} is assumed.
   * <p/>
   * Note: If the configuration fails to load, {@link DefaultEventConfig} is used
   * to provide defaults. For more
   * information about what those defaults are, see the documentation for that
   * class.
   *
   * @param location
   *                 the location of the configuration file
   */
  public static void initialize(@Nullable String location) {
    location = StringUtils.defaultIfEmpty(location, XmlEventConfig.DEFAULT_CONFIGURATION_FILE);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Initialising Event Advisor with configuration from [{}]", location);
    }
    final ServletEventAdvisor instance = new ServletEventAdvisor();
    try {
      instance.config = XmlEventConfig.fromFile(location);
    } catch (final ConfigurationEventException e) {
      LOGGER.warn("Failed to load configuration from [" + location + "]", e);
      instance.config = DefaultEventConfig.getInstance();
    }

    final IContainerFactory containerFactory = instance.config.getContainerFactory();
    instance.eventContainer = containerFactory.create();
    INSTANCE = instance;
  }

  /**
   * Initialises, loading its configuration from the provided location, and sets
   * the statically-bound instances of
   * {@link IEventConfig} and {@link IEventContainer}.
   * <p/>
   * If the location provided is {@code null} or empty,
   * {@link XmlEventConfig#DEFAULT_CONFIGURATION_FILE} is assumed.
   * <p/>
   * Note: If the configuration fails to load, {@link DefaultEventConfig} is used
   * to provide defaults. For more
   * information about what those defaults are, see the documentation for that
   * class.
   *
   * @param input
   *              the configuration file
   */
  public static void initialize(@Nonnull final InputStream input) {
    final ServletEventAdvisor instance = new ServletEventAdvisor();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Initialising Event Advisor");
    }
    try {
      instance.config = XmlEventConfig.fromInputStream(input);
    } catch (final ConfigurationEventException e) {
      LOGGER.warn("Failed to load configuration", e);
      instance.config = DefaultEventConfig.getInstance();
    }

    final IContainerFactory containerFactory = instance.config.getContainerFactory();
    instance.eventContainer = containerFactory.create();
    INSTANCE = instance;
  }

  /**
   * Terminates, clearing the statically-bound {@link IEventConfig} and
   * {@link IEventContainer}.
   */
  public void terminate() {
    config = null;
    eventContainer = null;
  }

  /**
   * Terminates , removing event-related attributes from the provided
   * {@code ServletContext} and clearing the
   * statically-bound {@link IEventConfig} and {@link IEventContainer}.
   *
   * @param context
   *                the servlet context
   */
  public void terminate(@Nonnull final ServletContext context) {
    checkNotNull(context, "context");

    terminate();

    context.removeAttribute(ATTR_CONFIG);
    context.removeAttribute(ATTR_EVENT_CONTAINER);
  }

  @Override
  @Nonnull
  public Optional<EventLevel> getEventLevel(final String level) {
    return Optional.ofNullable(getConfig().getEventLevel(level));
  }

  @Override
  @Nonnull
  public Optional<EventType> getEventType(final String type) {
    return Optional.ofNullable(getConfig().getEventType(type));
  }

  @Override
  public void publishEvent(final Event event) {
    getEventContainer().publishEvent(event);
  }

  @Override
  public void discardEvent(final Event event) {
    getEventContainer().discardEvent(event);
  }

}
