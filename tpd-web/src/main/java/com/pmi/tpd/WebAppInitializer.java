package com.pmi.tpd;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.util.Assert;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.annotation.Production;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventContextListener;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventListener;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleDelegatingFilterProxy;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleDispatcherServlet;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleHttpRequestHandlerServlet;
import com.pmi.tpd.core.maintenance.IInternalMaintenanceService;
import com.pmi.tpd.event.EventServlet;
import com.pmi.tpd.event.filter.BypassableEvent503Filter;
import com.pmi.tpd.event.filter.BypassableEventFilter;
import com.pmi.tpd.hazelcast.ConfigurableWebFilter;
import com.pmi.tpd.hazelcast.HazelcastSessionMode;
import com.pmi.tpd.metrics.servlet.HealthCheckRequestHandler;
import com.pmi.tpd.metrics.servlet.InstrumentedFilter;
import com.pmi.tpd.metrics.servlet.MetricRequestHandler;
import com.pmi.tpd.metrics.servlet.ThreadDumpRequestHandler;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.LifecycleApplicationContextInitializer;
import com.pmi.tpd.spring.context.ConfigFileLoader;
import com.pmi.tpd.startup.DefaultStartupManager;
import com.pmi.tpd.startup.SetupRedirectInterceptor;
import com.pmi.tpd.startup.StartupUtils;
import com.pmi.tpd.web.core.request.CurrentRequestContext;
import com.pmi.tpd.web.core.request.DefaultHttpRequestInfoHelper;
import com.pmi.tpd.web.core.request.DefaultRequestManager;
import com.pmi.tpd.web.core.request.IHttpRequestInfoHelper;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;
import com.pmi.tpd.web.core.rs.error.DefaultUnhandledExceptionMapperHelper;
import com.pmi.tpd.web.core.rs.error.IUnhandledExceptionMapperHelper;
import com.pmi.tpd.web.core.servlet.CachingHttpHeadersFilter;
import com.pmi.tpd.web.core.servlet.FilterLocation;
import com.pmi.tpd.web.core.servlet.RequestAttributeFilter;
import com.pmi.tpd.web.core.servlet.gzip.GZipServletFilter;
import com.pmi.tpd.web.logback.web.LogbackConfigListener;
import com.pmi.tpd.web.servlet.EventContainerServlet;
import com.pmi.tpd.web.servlet.ServletContextProviderListener;
import com.pmi.tpd.web.servlet.StartupProgressServlet;
import com.pmi.tpd.web.servlet.SystemInfoServlet;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebAppInitializer implements WebApplicationInitializer {

    /** */
    private static final EnumSet<DispatcherType> DEFAULT = EnumSet.noneOf(DispatcherType.class);

    /** */
    private static final String URL_ALL = "/*";

    /** */
    private static final String URL_REST = "/rest/*";

    /** */
    private static final String URL_WEBSOCKET = "/websocket/*";

    /** */
    private static final String[] COMPRESSED_MAPPING_PATTERNS = { "*.css", "*.json", "*.html", "*.js", "*.svg",
            "*.ttf" };

    /** */
    private static final String[] CACHED_MAPPING_PATTERNS = { "/js/*", "/lib/*" };

    /** */
    private static final Class<?>[] ANNOTATED_CLASSES = { AppConfig.class };

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppInitializer.class);

    /**
     * The default servlet name. Can be customized by overriding {@link #getServletName}.
     */
    public static final String DEFAULT_SERVLET_NAME = "dispatcher";

    /**
     * Spring specific configuration associated to web web application initializer.
     *
     * @author Christophe Friederich
     */
    @Configuration
    @EnableWebMvc
    @EnableHypermediaSupport(type = { HypermediaType.HAL })
    @ComponentScan("com.pmi.tpd.web.rest")
    public static class WebConfig implements WebMvcConfigurer {

        /** */
        @Inject
        private IApplicationProperties settings;

        @Override
        public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
            configurer.enable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
            localeChangeInterceptor.setParamName("language");
            registry.addInterceptor(localeChangeInterceptor);
            registry.addInterceptor(new SetupRedirectInterceptor(settings));
        }

        // /**
        // * @param servletContext
        // * @return
        // */
        // @Bean
        // ServletEventListener eventListener(final ServletContext servletContext) {
        // return new ServletEventListener(servletContext);
        // }

        @Bean(name = "hazelcastFilter")
        public Filter hazelcastFilter(final HazelcastInstance hazelcastInstance) {
            /* hazelcast.http.sessions */
            final HazelcastSessionMode mode = HazelcastSessionMode.LOCAL;
            final Properties properties = new Properties();
            properties.setProperty("cookie-http-only", "true");
            properties.setProperty("shutdown-on-destroy", "false");
            properties.setProperty("sticky-session", mode.getStickySessionProperty());
            final WebFilter filter = new WebFilter(properties);
            return new ConfigurableWebFilter(mode, filter);
        }

        @Bean
        public IHttpRequestInfoHelper httpRequestInfoHelper() {
            return new DefaultHttpRequestInfoHelper();
        }

        @Bean
        public IRequestManager requestManager(final IAuthenticationContext authenticationContext,
            final IEventPublisher eventPublisher,
            final IClusterService clusterService) {
            return new DefaultRequestManager(authenticationContext, eventPublisher, clusterService);
        }

        @Bean
        public IRequestContext requestContext() {
            return new CurrentRequestContext();
        }

        @Bean(name = "jerseyApiFilter")
        public ServletContainer jerseyApiServlet(@Named("RestApplication") final ResourceConfig resourceConfig) {
            return new ServletContainer(resourceConfig);
        }

        @Bean(name = "requestAttributeFilter")
        public RequestAttributeFilter requestAttributeFilter(final IHttpRequestInfoHelper helper,
            final IRequestManager requestManager) {
            return new RequestAttributeFilter(helper, requestManager);
        }

        @Bean(name = "instrumentedFilter")
        public Filter instrumentedFilter(final MetricRegistry registry) {
            return new InstrumentedFilter(registry);
        }

        @Bean(name = "openEntityManagerInViewFilter")
        Filter openEntityManagerInViewFilter(final IInternalMaintenanceService internalMaintenanceService) {
            final OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter() {

                @Override
                protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
                    return internalMaintenanceService.getNodeLock() != null
                            || !ServletEventAdvisor.getInstance().getConfig().getSetupConfig().isSetup();
                }
            };
            filter.setEntityManagerFactoryBeanName(ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME);
            return filter;
        }

        @Bean(name = "metricsServlet")
        public MetricRequestHandler metricsServlet(final MetricRegistry registry) {
            return new MetricRequestHandler(registry, null);
        }

        @Bean(name = "healthcheck")
        public HealthCheckRequestHandler healthcheck(final HealthCheckRegistry registry) {
            return new HealthCheckRequestHandler(registry);
        }

        @Bean(name = "threadDump")
        public ThreadDumpRequestHandler threadDumpRequestHandler(final ObjectMapper objectMapper) {
            return new ThreadDumpRequestHandler(objectMapper);
        }

        @Bean
        public ServletEventListener servletEventListener(@Nonnull final ServletContext servletContext) {
            return new ServletEventListener(servletContext);
        }

        @Bean
        public IUnhandledExceptionMapperHelper unhandledExceptionMapperHelper(
            final IAuthenticationContext authenticationContext,
            final I18nService i18nService,
            final IRequestManager requestManager) {
            return new DefaultUnhandledExceptionMapperHelper(authenticationContext, i18nService, requestManager);
        }

    }

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        StartupUtils.setStartupManager(servletContext, new DefaultStartupManager(servletContext));
        servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));

        servletContext.setInitParameter("contextConfigLocation", this.getClass().getPackage().getName());

        final ConfigurableEnvironment environment = bootstrap(servletContext);

        addListeners(servletContext);
        addFilters(servletContext);
        addServlets(servletContext, environment);

    }

    public ConfigurableEnvironment bootstrap(final ServletContext servletContext) throws ServletException {

        final ConfigurableEnvironment environment = new StandardServletEnvironment();

        initializeActiveProfiles(environment);

        initializeHomePath(environment);

        // Initialise Logback
        final String logLevel = environment.getProperty(ApplicationConstants.PropertyKeys.LOG_LEVEL_PROPERTY, "WARN");

        // Initialising Logback should always be the first listener in the list.
        servletContext.addListener(new LogbackConfigListener(servletContext, "classpath:logback.xml",
                "file:${app.home}/conf/logback.xml", logLevel));

        final String finded = System.getProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);
        if (finded == null) {
            LOGGER.info("No home path configured.");
        } else {
            LOGGER.info("Home path detected : {}", finded);
        }

        if (environment.getActiveProfiles().length == 0) {
            LOGGER.warn("No Spring profile configured, running with default configuration.");
        } else {
            for (final String profile : environment.getActiveProfiles()) {
                LOGGER.info("Startup Configuration detected Spring profile: {}", profile);
            }
        }
        return environment;
    }

    public static void initializeActiveProfiles(final ConfigurableEnvironment environment) throws ServletException {
        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        ResourcePropertySource propertySource;
        String profiles = null;
        try {
            propertySource = new ResourcePropertySource(
                    resourceLoader.getResource(ApplicationConstants.BOOTSTRAP_PROPERTIES_RESOURCE));
            profiles = (String) propertySource.getProperty(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY);
        } catch (final IOException e) {
            LOGGER.warn("Bootstrap file is missing.");
        }
        if (StringUtils.isEmpty(profiles)) {
            profiles = getEnvVariable(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY);
        }
        // activate profiles
        final String envProfiles = getEnvVariable(ApplicationConstants.PROFILES_ENV_VARIABLE);
        if (StringUtils.isNotEmpty(envProfiles)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Set variable Environment {} with value: {}",
                    ApplicationConstants.PROFILES_ENV_VARIABLE,
                    envProfiles);
            }
            environment.setActiveProfiles(envProfiles.split(","));
        } else if (StringUtils.isNotEmpty(profiles)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Use variable Environment {} with value: {}",
                    ConfigFileLoader.ACTIVE_PROFILES_PROPERTY,
                    profiles);
            }
            environment.setActiveProfiles(profiles);
        } else {
            // set production profile
            final Profile profile = AnnotationUtils.getAnnotation(Production.class, Profile.class);
            environment.setActiveProfiles(profile.value());
        }
        try {
            profiles = String.join(",", environment.getActiveProfiles());
            System.setProperty(ConfigFileLoader.ACTIVE_PROFILES_PROPERTY, profiles);
            LOGGER.info("Loading Spring config file with profile '{}'", profiles);
            ConfigFileLoader.load(environment);
        } catch (final IOException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    public static void initializeHomePath(final Environment environment) throws ServletException {

        final String homePath = environment.getProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY);
        final String home = getEnvVariable(ApplicationConstants.HOME_ENV_VARIABLE);
        // override app.home property in application properties files.
        if (StringUtils.isNotEmpty(home)) {
            System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, home);
        } else if (StringUtils.isNotEmpty(homePath)) {
            System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, homePath);
        }

    }

    private void addListeners(final ServletContext servletContext) {

        servletContext.addListener(ServletContextProviderListener.class);

        // Initialising Application Event should always happen immediately after logging, before attempting to start any
        // of the
        // other pieces of the system.
        ServletEventContextListener.register(servletContext);

        servletContext.addListener(ContextCleanupListener.class);
        servletContext.addListener(RequestContextListener.class);

        // Allow Hazelcast to track underlying sessions so it knows when to invalidate its proxies
        servletContext.addListener(com.hazelcast.web.SessionListener.class);
        servletContext.addListener(new SessionListener());

    }

    private void addFilters(final ServletContext servletContext) {
        // instead of ISO-8859-1. For more info, see http://wiki.apache.org/tomcat/FAQ/CharacterEncoding#Q1 and
        // http://wiki.apache.org/tomcat/FAQ/CharacterEncoding#Q3
        final FilterRegistration.Dynamic requestCharsetFilter = servletContext.addFilter("DefaultCharsetRequestFilter",
            CharacterEncodingFilter.class);
        requestCharsetFilter.setInitParameter("encoding", "UTF-8");
        requestCharsetFilter.setAsyncSupported(isAsyncSupported());
        requestCharsetFilter.addMappingForUrlPatterns(DEFAULT, true, URL_ALL);

        // Bind request-related properties to the MDC as early as possible. This filter does _not_ include
        // authentication information, but it gives significant other useful information
        registerLifecycleFilter(servletContext, "requestAttributeFilter")
                .addMappingForUrlPatterns(EnumSet.of(REQUEST, ERROR, FORWARD), true, URL_ALL);

        // Hazelcast wants to be first, but we still map our character set filters first
        final FilterRegistration.Dynamic hazelcastFilter = registerLifecycleFilter(servletContext, "hazelcastFilter");
        hazelcastFilter.setInitParameters(
            ImmutableMap.of("map-name", HttpSession.class.getName(), "targetFilterLifecycle", "true"));
        hazelcastFilter.setAsyncSupported(isAsyncSupported());
        hazelcastFilter.addMappingForUrlPatterns(EnumSet.of(REQUEST, ERROR), true, URL_ALL);

        // Spring Security Chain Filter
        final FilterRegistration.Dynamic springSecurityFilterChain = registerLifecycleFilter(servletContext,
            "springSecurityFilterChain");
        springSecurityFilterChain.addMappingForUrlPatterns(getDispatcherTypes(), true, URL_ALL);
        springSecurityFilterChain.setAsyncSupported(isAsyncSupported());

        // Intercept requests and process events before just about anything
        // Filter "event503" returns status code 503 Service Unavailable, and should be applied to any URLs which will
        // not understand HTML responses. All such URLs should be set _before_ the "event" filter's mappings
        servletContext.addFilter("event503", BypassableEvent503Filter.class)
                .addMappingForUrlPatterns(DEFAULT, true, URL_ALL);

        // Filter "event" redirects to another web page on error and should be applied to any URL which will
        // understand
        // an HTML response. Applying this after the 503 filter (which will not invoke the rest of the chain if there is
        // an error) allows us to apply it to any URL
        servletContext.addFilter("event", BypassableEventFilter.class).addMappingForUrlPatterns(DEFAULT, true, URL_ALL);

        registerModuleContainerFilters(servletContext, FilterLocation.AFTER_ENCODING);
        registerModuleContainerFilters(servletContext, FilterLocation.BEFORE_LOGIN);

        //
        final FilterRegistration.Dynamic urlRewriteFilter = servletContext.addFilter("urlRewriteFilter",
            UrlRewriteFilter.class);
        urlRewriteFilter.setInitParameters(ImmutableMap.of("confReloadCheckInterval",
            "-1", // No runtime reloading for urlrewrite.xml
            "statusEnabled",
            "false")); // Mustn't ask us... Not their business...
        urlRewriteFilter.addMappingForUrlPatterns(DEFAULT, true, URL_ALL);

        initGzipFilter(servletContext, COMPRESSED_MAPPING_PATTERNS);
        initCachingHttpHeadersFilter(servletContext, CACHED_MAPPING_PATTERNS);

        //
        final FilterRegistration.Dynamic instrumentedFilter = registerLifecycleFilter(servletContext,
            "instrumentedFilter");
        instrumentedFilter.addMappingForUrlPatterns(getDispatcherTypes(), true, URL_ALL);
        instrumentedFilter.setInitParameters(
            ImmutableMap.of("name-prefix", "InstrumentedRequest", "targetFilterLifecycle", "true"));

        final FilterRegistration.Dynamic openEntityManagerInViewFilter = registerLifecycleFilter(servletContext,
            "openEntityManagerInViewFilter");
        openEntityManagerInViewFilter.addMappingForUrlPatterns(getDispatcherTypes(), true, URL_ALL);

        //
        final FilterRegistration.Dynamic jerseyApiFilter = registerLifecycleFilter(servletContext, "jerseyApiFilter");
        jerseyApiFilter.setInitParameters(ImmutableMap.<String, String> builder()
                .put("targetFilterLifecycle", "true")
                .put("jersey.config.servlet.filter.contextPath", "/rest")
                .build());
        jerseyApiFilter.addMappingForUrlPatterns(getDispatcherTypes(), true, URL_REST);
        jerseyApiFilter.setAsyncSupported(isAsyncSupported());

    }

    private void addServlets(final ServletContext servletContext, final ConfigurableEnvironment environment)
            throws ServletException {

        servletContext.addServlet("eventServlet", EventServlet.class).addMapping("/unavailable");

        servletContext.addServlet("startupProgressServlet", StartupProgressServlet.class)
                .addMapping(StartupProgressServlet.PATH);
        servletContext.addServlet("eventContainerServlet", EventContainerServlet.class)
                .addMapping(EventContainerServlet.PATH);

        servletContext.addServlet("systemInfoServlet", SystemInfoServlet.class).addMapping(SystemInfoServlet.PATH);

        // Register SpringMVC. Note that SpringMVC is not _initialized_ here. The LifecycleDispatcherServlet will
        // start a thread during normal servlet initialization which will allow the web container to come up
        // fully without SpringMVC ready. When SpringMVC initialization completes the dispatcher servlet will
        // automatically begin handling requests. Prior to that, it will redirect to Event's error page
        final LifecycleDispatcherServlet dispatcherServlet = new LifecycleDispatcherServlet();
        dispatcherServlet.setEnvironment(environment);
        final ServletRegistration.Dynamic dispatcher = servletContext.addServlet(getServletName(), dispatcherServlet);
        dispatcher.setInitParameters(ImmutableMap.<String, String> builder()
                .put("contextClass", AnnotationConfigWebApplicationContext.class.getName())
                .put("contextInitializerClasses",
                    Joiner.on(',').join(Arrays.asList(LifecycleAnnotatedConfigInitializer.class.getName())))
                .put("dispatchOptionsRequest", "true")
                .build());
        dispatcher.setAsyncSupported(isAsyncSupported());

        dispatcher.setLoadOnStartup(2); // Load after all other servlets
        dispatcher.addMapping(URL_WEBSOCKET);

        initMetricsServlet(servletContext);
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    public static class LifecycleAnnotatedConfigInitializer extends LifecycleApplicationContextInitializer {

        @Override
        public void initialize(final AnnotationConfigWebApplicationContext applicationContext) {
            applicationContext.register(ANNOTATED_CLASSES);
            super.initialize(applicationContext);
        };
    }

    private void initMetricsServlet(final ServletContext servletContext) {
        final ServletRegistration.Dynamic metricsServlet = registerLifecycleServlet(servletContext, "metricsServlet");

        metricsServlet.addMapping("/metrics/metrics/*");
        metricsServlet.setAsyncSupported(isAsyncSupported());
        metricsServlet.setLoadOnStartup(4);
        final ServletRegistration.Dynamic healthcheck = registerLifecycleServlet(servletContext, "healthcheck");

        healthcheck.addMapping("/metrics/healthcheck/*");
        healthcheck.setAsyncSupported(isAsyncSupported());
        healthcheck.setLoadOnStartup(5);

        final ServletRegistration.Dynamic threadDump = registerLifecycleServlet(servletContext, "threadDump");

        threadDump.addMapping("/metrics/threads/*");
        threadDump.setAsyncSupported(isAsyncSupported());
        threadDump.setLoadOnStartup(6);

        // final ServletRegistration.Dynamic cpuProfile = registerLifecycleServlet(servletContext, "cpuProfile");
        //
        // cpuProfile.setInitParameter(Application.class.getName(), RestApplication.class.getName());
        // cpuProfile.addMapping("/metrics/pprof/raw");
        // cpuProfile.setAsyncSupported(isAsyncSupported());
        // cpuProfile.setLoadOnStartup(7);
    }

    /**
     * Initializes the GZip filter.
     */
    private void initGzipFilter(final ServletContext servletContext, final String... compressedMappingPatterns) {
        final FilterRegistration.Dynamic compressingFilter = registerServletFilter(servletContext,
            new GZipServletFilter());
        final Map<String, String> parameters = new HashMap<>();
        compressingFilter.setInitParameters(parameters);
        compressingFilter.addMappingForUrlPatterns(getDispatcherTypes(), true, compressedMappingPatterns);
        compressingFilter.setAsyncSupported(isAsyncSupported());
    }

    /**
     * Initializes the cachig HTTP Headers Filter.
     */
    private void initCachingHttpHeadersFilter(final ServletContext servletContext,
        final String... cachedMappingPatterns) {
        final FilterRegistration.Dynamic cachingHttpHeadersFilter = registerServletFilter(servletContext,
            new CachingHttpHeadersFilter());

        cachingHttpHeadersFilter.addMappingForUrlPatterns(getDispatcherTypes(), true, cachedMappingPatterns);
        cachingHttpHeadersFilter.setAsyncSupported(isAsyncSupported());
    }

    protected String getServletName() {
        return DEFAULT_SERVLET_NAME;
    }

    private FilterRegistration.Dynamic registerLifecycleFilter(final ServletContext servletContext, final String name) {
        final FilterRegistration.Dynamic filter = servletContext.addFilter(name, LifecycleDelegatingFilterProxy.class);
        return filter;
    }

    protected FilterRegistration.Dynamic registerServletFilter(final ServletContext servletContext,
        final Filter filter) {
        final String filterName = Conventions.getVariableName(filter);
        Dynamic registration = servletContext.addFilter(filterName, filter);
        if (registration == null) {
            int counter = -1;
            while (counter == -1 || registration == null) {
                counter++;
                registration = servletContext.addFilter(filterName + "#" + counter, filter);
                Assert.isTrue(counter < 100,
                    "Failed to register filter '" + filter + "'."
                            + "Could the same Filter instance have been registered already?");
            }
        }
        registration.setAsyncSupported(isAsyncSupported());
        return registration;
    }

    private ServletRegistration.Dynamic registerLifecycleServlet(final ServletContext context, final String name) {
        final ServletRegistration.Dynamic servlet = context.addServlet(name, LifecycleHttpRequestHandlerServlet.class);
        servlet.setLoadOnStartup(1);

        return servlet;
    }

    private void registerModuleContainerFilters(final ServletContext context, final FilterLocation location) {
        registerModuleContainerFilter(context, location, REQUEST);
        registerModuleContainerFilter(context, location, FORWARD);
        registerModuleContainerFilter(context, location, INCLUDE);
        registerModuleContainerFilter(context, location, ERROR);
    }

    private FilterRegistration.Dynamic registerModuleContainerFilter(final ServletContext context,
        final FilterLocation location,
        final DispatcherType dispatcher) {
        // final String name = ("filter-plugin-dispatcher-"
        // + location + "-" + dispatcher).replace("_", "-") // For the FilterLocations
        // .toLowerCase(Locale.US); // For both enum names
        //
        // final FilterRegistration.Dynamic filter = context.addFilter(name,
        // EventServletFilterModuleContainerFilter.class);
        // filter.setInitParameters(ImmutableMap.of("location", location.name(), "dispatcher", dispatcher.name()));
        // filter.addMappingForUrlPatterns(EnumSet.of(dispatcher), true, URL_ALL);
        //
        // return filter;
        return null;
    }

    private EnumSet<DispatcherType> getDispatcherTypes() {
        return isAsyncSupported() ? EnumSet.of(REQUEST, FORWARD, INCLUDE, ASYNC)
                : EnumSet.of(REQUEST, FORWARD, INCLUDE);
    }

    /**
     * A single place to control the {@code asyncSupported} flag for the {@code DispatcherServlet} and all filters
     * added.
     * <p>
     * The default value is "true".
     */
    protected boolean isAsyncSupported() {
        return true;
    }

    private static String getEnvVariable(final String key) {
        String value = System.getenv(key);
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(key);
        }
        return value;
    }

    public class SessionListener implements HttpSessionListener {

        @Override
        public void sessionCreated(HttpSessionEvent event) {

            event.getSession().setMaxInactiveInterval(15);
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent event) {
        }
    }
}
