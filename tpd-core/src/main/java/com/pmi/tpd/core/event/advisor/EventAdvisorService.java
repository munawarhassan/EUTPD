package com.pmi.tpd.core.event.advisor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import org.springframework.util.ResourceUtils;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.advisor.config.ConfigurationEventException;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.config.XmlEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class EventAdvisorService implements IEventAdvisorService<Object> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private static final List<String> EVENT_LEVELS = ImmutableList
      .of(LEVEL_SYSTEM_MAINTENANCE, LEVEL_MAINTENANCE, EventLevel.WARNING, EventLevel.ERROR, EventLevel.FATAL);

  private transient static IEventAdvisorService<Object> INSTANCE;

  private transient IEventContainer eventContainer = null;

  private EventAdvisorService() {
  }

  private EventAdvisorService(final IEventContainer eventContainer) {
    this.eventContainer = eventContainer;
  }

  public static synchronized IEventAdvisorService<Object> getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EventAdvisorService();
    }
    return INSTANCE;
  }

  public static IEventAdvisorService<?> getInstance(@Nonnull final Object context) {
    IEventContainer eventContainer;
    if (context instanceof ServletContext) {
      eventContainer = ServletEventAdvisor.getInstance().getEventContainer((ServletContext) context);
    } else if (context instanceof IEventContainer) {
      eventContainer = (IEventContainer) context;
    } else {
      throw new UnsupportedOperationException();
    }
    return new EventAdvisorService(eventContainer);
  }

  /**
   * Retrieves the statically-bound {@link IEventConfig}.
   * <p/>
   * Note: If Event Application has not been {@link #initialize(String)
   * initialised}, this method <i>will not</i>
   * initialise it. Before attempting to use, it is left to the application
   * developer to ensure it has been correctly
   * initialised.
   *
   * @return the working configuration
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public IEventConfig getConfig() {
    return ServletEventAdvisor.getInstance().getConfig();
  }

  /**
   * Attempts to retrieve the {@link IEventConfig} from the provided context under
   * the key {@link #ATTR_CONFIG} before
   * falling back on {@link #getConfig()}.
   *
   * @param context
   *                the context
   * @return the working configuration
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  public static IEventConfig getConfig(@Nonnull final Object context) {
    if (context instanceof ServletContext) {
      return ServletEventAdvisor.getInstance().getConfig((ServletContext) context);
    }
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public IEventContainer getEventContainer() {
    if (eventContainer != null) {
      return eventContainer;
    }
    return ServletEventAdvisor.getInstance().getEventContainer();
  }

  /**
   * Initialises Event, additionally binding the {@link IEventConfig} and
   * {@link IEventContainer} to the provided
   * {@code context} and performing any {@link ApplicationEventCheck}s which have
   * been configured.
   * <p/>
   * If the {@link #PARAM_CONFIG_LOCATION} {@code init-param} is set, it is used
   * to determine the location of the
   * configuration file. Otherwise,
   * {@link XmlEventConfig#DEFAULT_CONFIGURATION_FILE} is assumed.
   * <p/>
   *
   * @param context
   *                the servlet context
   */
  public static IEventAdvisorService<?> initialize(@Nonnull final Object context) {
    if (context instanceof ServletContext) {
      ServletEventAdvisor.initialize((ServletContext) context);
      return getInstance();
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Initialises, loading its configuration from the provided location, and sets
   * the statically-bound instances of
   * {@link IEventConfig} and {@link IEventContainer}.
   * <p/>
   * If the location provided is {@code null} or empty,
   * {@link com.pmi.tpd.core.event.advisor.config.XmlEventConfig#DEFAULT_CONFIGURATION_FILE}
   * is assumed.
   * <p/>
   * Note: If the configuration fails to load,
   * {@link com.pmi.tpd.core.event.advisor.support.DefaultEventConfig
   * DefaultEventConfig} is used to provide defaults. For more information about
   * what those defaults are, see the
   * documentation for that class.
   *
   * @param location
   *                 the location of the configuration file
   * @deprecated use {@link #initialize(InputStream)} instead
   */
  @Deprecated
  public static IEventAdvisorService<?> initialize(@Nullable final String location) {
    try {
      ServletEventAdvisor.initialize(ResourceUtils.getURL(location).getPath());
      return getInstance();
    } catch (final FileNotFoundException e) {
      throw new ConfigurationEventException("Failed to load configuration from [" + location + "]", e);
    }
  }

  /**
   * Initialises, loading its configuration from the provided location, and sets
   * the statically-bound instances of
   * {@link IEventConfig} and {@link IEventContainer}.
   * <p/>
   * If the location provided is {@code null} or empty,
   * {@link com.pmi.tpd.core.event.advisor.config.XmlEventConfig#DEFAULT_CONFIGURATION_FILE}
   * is assumed.
   * <p/>
   * Note: If the configuration fails to load,
   * {@link com.pmi.tpd.core.event.advisor.support.DefaultEventConfig
   * DefaultEventConfig} is used to provide defaults. For more information about
   * what those defaults are, see the
   * documentation for that class.
   *
   * @param input
   *              the configuration file
   */
  public static IEventAdvisorService<?> initialize(@Nullable final InputStream input) {
    ServletEventAdvisor.initialize(input);
    return getInstance();
  }

  /**
   * Terminates, clearing the statically-bound {@link IEventConfig} and
   * {@link IEventContainer}.
   */
  @Override
  public void terminate() {
    ServletEventAdvisor.getInstance().terminate();
  }

  /**
   * Terminates , removing event-related attributes from the provided
   * {@code context} and clearing the
   * statically-bound {@link IEventConfig} and {@link IEventContainer}.
   *
   * @param context
   *                the servlet context
   */
  @Override
  public void terminate(@Nonnull final Object context) {
    if (context instanceof ServletContext) {
      ServletEventAdvisor.getInstance().terminate((ServletContext) context);
    }
    throw new UnsupportedOperationException();
  }

  /**
   * @return the highest event level within the event container or {@code null} if
   *         the no events are contained
   */
  @Override
  @Nullable
  public String findHighestEventLevel() {
    final int index = findHighestEventLevelIndex(getEventContainer());
    return index == -1 ? null : EVENT_LEVELS.get(index);
  }

  /**
   * @return {@code true} if the {@code level} is higher or equal to the
   *         {@code minimumLevel}
   */
  @Override
  public boolean isLevelAtLeast(@Nonnull final String level, @Nonnull final String minimumLevel) {
    final int index = EVENT_LEVELS.indexOf(level);
    Assert.state(index != -1, "Invalid level {}", level);

    final int minimumIndex = EVENT_LEVELS.indexOf(minimumLevel);
    Assert.state(minimumIndex != -1, "Invalid minimumLevel }{", minimumIndex);

    return minimumIndex <= index;
  }

  /**
   * @return {@code true} if the {@code level} is lower or equal to the
   *         {@code maximumLevel}
   */
  @Override
  public boolean isLevelAtMost(@Nonnull final String level, @Nonnull final String maximumLevel) {
    final int index = EVENT_LEVELS.indexOf(level);
    Assert.state(index != -1, "Invalid level {}", level);

    final int maximumIndex = EVENT_LEVELS.indexOf(maximumLevel);
    Assert.state(maximumIndex != -1, "Invalid maximumLevel {}", maximumLevel);

    return maximumIndex >= index;
  }

  private static int findHighestEventLevelIndex(final IEventContainer eventContainer) {
    // Note: Checking eventContainer.hasEvents() or events.isEmpty() has no meaning,
    // since it's a live view of
    // the data. The most stable solution is to simply iterate and use the result to
    // conclude whether any
    // events were present and, if so, where to go next
    int i = -1;
    for (final Event event : eventContainer.getEvents()) {
      // Once we're iterating, the events are stable; until then, no guarantees
      final int x = EVENT_LEVELS.indexOf(event.getLevel().getLevel());
      if (x > i) {
        i = x;
      }
    }
    return i;
  }

  @Override
  public void publishEvent(final Event event) {
    getEventContainer().publishEvent(event);
  }

  @Override
  public void discardEvent(final Event event) {
    getEventContainer().discardEvent(event);
  }

  @Override
  public Optional<EventLevel> getEventLevel(final String level) {
    return Optional.ofNullable(getConfig().getEventLevel(level));
  }

  @Override
  public Optional<EventType> getEventType(final String type) {
    return Optional.ofNullable(getConfig().getEventType(type));
  }
}
