package com.pmi.tpd.web.logback.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.web.logback.LogLevelPropertyDefiner;

import ch.qos.logback.classic.Level;

/**
 * Bootstrap listener for custom Logback initialisation in a web environment. Delegates to {@link LogbackWebConfigurer}.
 * <p/>
 * This listener should be registered before Spring's {@code ContextLoaderListener} in {@code web.xml}, when using
 * custom Logback initialisation.
 *
 * @see LogbackWebConfigurer
 */
public class LogbackConfigListener implements ServletContextListener {

    /**
     *
     */
    public LogbackConfigListener() {
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
    public LogbackConfigListener(final ServletContext container, final String defaultLocation, final String location,
            final String defaultLevel) {
        String level = defaultLevel;
        if (StringUtils.isEmpty(level)) {
            level = Level.WARN.toString();
        }
        container.setInitParameter(LogbackWebConfigurer.DEFAULT_CONFIG_LOCATION_PARAM, defaultLocation);
        container.setInitParameter(LogbackWebConfigurer.CONFIG_LOCATION_PARAM, location);
        container.setAttribute(LogbackWebConfigurer.PROPERTIES_ATTRIBUTE,
            ImmutableMap.<String, Object> builder().put(LogLevelPropertyDefiner.LOG_LEVEL, level).build());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        LogbackWebConfigurer.shutdownLogging(event.getServletContext());
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        LogbackWebConfigurer.initLogging(event.getServletContext());
    }
}
