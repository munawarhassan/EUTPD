package com.pmi.tpd.web.logback.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.pmi.tpd.web.context.util.WebUtils;
import com.pmi.tpd.web.logback.LogLevelPropertyDefiner;
import com.pmi.tpd.web.logback.LogbackConfigurer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Convenience class that performs custom Logback initialisation for web environments, allowing for log file paths
 * within the web application.
 * <p/>
 * Supports two parameters at the servlet context level (that is, {@code context-param} entries in {@code web.xml}):
 * <ul>
 * <li><i>"logbackConfigLocation":</i><br>
 * Location of the Logback config file; either a "classpath:" location (e.g. "classpath:myLogback.xml"), an absolute
 * file URL (e.g. "file:C:/logback.properties), or a plain path relative to the web application root directory (e.g.
 * "/WEB-INF/logback.xml"). If not specified, default Logback initialisation will apply ("logback.xml" or
 * "logback-test.xml" in the class path; see Logback documentation for details).
 * <li><i>"logbackExposeWebAppRoot":</i><br>
 * Whether the web app root system property should be exposed, allowing for log file paths relative to the web
 * application root directory. Default is "false"; specify "true" to suppress expose of the web app root system
 * property. See below for details on how to use this system property in log file locations.
 * </ul>
 * <p/>
 * Note: {@link #initLogging(ServletContext)} should be called before any other Spring activity (when using Logback),
 * for proper initialisation before any Spring logging attempts.
 * <p/>
 * <p>
 * This configurer can optionally set the web app root system property, for "${key}" substitutions within log file
 * locations in the Logback config file, allowing for log file paths relative to the web application root directory. The
 * default system property key is "webapp.root", to be used in a Logback config file like as follows:
 *
 * <pre>
 * &lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
 *   &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *     &lt;pattern>%-4relative [%thread] %-5level %class - %msg%n&lt;/pattern&gt;
 *   &lt;/layout&gt;
 *   &lt;file&gt;${webapp.root}/WEB-INF/demo.log&lt;/File&gt;
 * &lt;/appender&gt;
 * </pre>
 *
 * Alternatively, specify a unique context-param "webAppRootKey" per web application. For example, with
 * {@code webAppRootKey = "demo.root"}:
 *
 * <pre>
 * &lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
 *   &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *     &lt;pattern&gt;%-4relative [%thread] %-5level %class - %msg%n&lt;/pattern&gt;
 *   &lt;/layout&gt;
 *   &lt;fil&gt;${demo.root}/WEB-INF/demo.log&lt;/File&gt;
 * &lt;/appender&gt;
 * </pre>
 *
 * <b>WARNING:</b> Some containers (like Tomcat) do <i>not</i> keep system properties separate per web app. You have to
 * use unique "webAppRootKey" context-params per web app then, to avoid clashes. Other containers like Resin do isolate
 * each web app's system properties: Here you can use the default key (i.e. no "webAppRootKey" context-param at all)
 * without worrying.
 *
 * @see org.springframework.util.Log4jConfigurer
 * @see org.springframework.web.util.Log4jConfigListener
 */
public final class LogbackWebConfigurer {

    /** logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackWebConfigurer.class);

    /** default configuration location parameter name. */
    static final String DEFAULT_CONFIG_LOCATION_PARAM = "logbackDefaultConfigLocation";

    /**
     * Parameter specifying the location of the Logback configuration file.
     * <p/>
     * Note: This parameter only needs to be specified if you are not using a standard Logback location. Logback will
     * automatically initialise itself if you are using the standard "logback.xml" name.
     */
    static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";

    /**
     * Parameter specifying whether to expose the web app root system property.
     * <p/>
     * This property must be <i>explicitly</i> set in order to expose the root; it is not exposed by default.
     */
    public static final String EXPOSE_WEB_APP_ROOT_PARAM = "logbackExposeWebAppRoot";

    /** */
    public static final String PROPERTIES_ATTRIBUTE = "logback.properties";

    private LogbackWebConfigurer() {
    }

    /**
     * @param container
     *            the servlet context.
     * @param defaultLocation
     *            the default location of the Logback configuration file.
     * @param location
     *            the location of the Logback configuration file.
     * @param defaultLevel
     *            the default log level
     */
    public static void initLogging(final ServletContext container,
        final String defaultLocation,
        final String location,
        final String defaultLevel) {
        String level = defaultLevel;
        if (StringUtils.isEmpty(level)) {
            level = Level.WARN.toString();
        }
        container.setInitParameter(LogbackWebConfigurer.DEFAULT_CONFIG_LOCATION_PARAM, defaultLocation);
        container.setInitParameter(LogbackWebConfigurer.CONFIG_LOCATION_PARAM, location);
        container.setAttribute(LogbackWebConfigurer.PROPERTIES_ATTRIBUTE,
            ImmutableMap.<String, Object> builder().put(LogLevelPropertyDefiner.LOG_LEVEL, level).build());
        initLogging(container);
    }

    /**
     * Initialize Logback, including setting the web app root system property.
     *
     * @param servletContext
     *            the current ServletContext
     * @see org.springframework.web.util.WebUtils#setWebAppRootSystemProperty
     */
    @SuppressWarnings("unchecked")
    public static void initLogging(final ServletContext servletContext) {
        // Expose the web app root system property.
        if (exposeWebAppRoot(servletContext)) {
            WebUtils.setWebAppRootSystemProperty(servletContext);
        }

        File logFile = null;
        try {
            logFile = ResourceUtils.getFile(getLocation(servletContext, CONFIG_LOCATION_PARAM));
            init(servletContext, logFile);
            // Initialise
            final Map<String, String> properties = (Map<String, String>) servletContext
                    .getAttribute(PROPERTIES_ATTRIBUTE);
            LogbackConfigurer.initLogging(logFile, properties);
        } catch (final IOException e) {
            LOGGER.error("Unable to initialize logback configuration", e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (final JoranException e) {
            throw new RuntimeException("Unexpected error while configuring logback", e);
        }

        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.install();
        }
    }

    /**
     * Shut down Logback, properly releasing all file locks and resetting the web app root system property.
     *
     * @param servletContext
     *            the current ServletContext
     * @see WebUtils#removeWebAppRootSystemProperty
     */
    public static void shutdownLogging(final ServletContext servletContext) {
        // Uninstall the SLF4J java.util.logging bridge *before* shutting down
        // the Logback framework.

        if (SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.uninstall();
        }
        try {
            servletContext.log("Shutting down Logback");
            LogbackConfigurer.shutdownLogging();
        } finally {
            // Remove the web app root system property.
            if (exposeWebAppRoot(servletContext)) {
                WebUtils.removeWebAppRootSystemProperty(servletContext);
            }
        }
    }

    /**
     * Return whether to expose the web app root system property, checking the corresponding ServletContext parameter.
     *
     * @param servletContext
     *            the servlet context
     * @return {@code true} if the webapp's root should be exposed; otherwise, {@code false}
     * @see #EXPOSE_WEB_APP_ROOT_PARAM
     */
    private static boolean exposeWebAppRoot(final ServletContext servletContext) {
        final String exposeWebAppRootParam = servletContext.getInitParameter(EXPOSE_WEB_APP_ROOT_PARAM);
        return exposeWebAppRootParam != null && Boolean.valueOf(exposeWebAppRootParam);
    }

    private static String getLocation(final ServletContext servletContext, final String param)
            throws FileNotFoundException {
        String location = servletContext.getInitParameter(param);
        location = SystemPropertyUtils.resolvePlaceholders(location);
        if (!ResourceUtils.isUrl(location)) {
            location = WebUtils.getRealPath(servletContext, location);
        }
        return location;
    }

    private static void init(final ServletContext servletContext, final File logFile) throws IOException {
        if (!logFile.exists()) {
            LOGGER.info("Generating default logback configuration file");
            // Copy default configuration file
            try {
                final File logFileDefault = ResourceUtils
                        .getFile(getLocation(servletContext, DEFAULT_CONFIG_LOCATION_PARAM));
                FileUtils.copyFile(logFileDefault, logFile);
            } catch (final FileNotFoundException ex) {
                String location = servletContext.getInitParameter(DEFAULT_CONFIG_LOCATION_PARAM);
                location = SystemPropertyUtils.resolvePlaceholders(location);
                // if war does not exploded try retrieve stream directly
                copyInputStreamToFile(WebUtils.getResourceAsStream(servletContext, location), logFile);
            }
        }
    }

    private static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try {
            final FileOutputStream output = FileUtils.openOutputStream(destination);
            try {
                IOUtils.copy(source, output);
                output.close(); // don't swallow close Exception if copy
                // completes normally
            } finally {
                Closeables.close(output, true);
            }
        } finally {
            Closeables.closeQuietly(source);
        }
    }
}
