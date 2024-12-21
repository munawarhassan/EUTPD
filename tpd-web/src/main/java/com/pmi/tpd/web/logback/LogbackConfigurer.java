package com.pmi.tpd.web.logback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.util.Throwables;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Convenience class that features simple methods for custom Logback configuration.
 * <p/>
 * Only needed for non-default Logback initialisation with a custom config location. By default, Logback will simply
 * read its configuration from a "logback.xml" or "logback-test.xml" file in the root of the class path.
 * <p/>
 * For web environments, the analogous {@link com.pmi.tpd.web.logback.web.LogbackWebConfigurer LogbackWebConfigurer} can
 * be found in the web package, reading in its configuration from {@code context-params} in {@code web.xml}. In a web
 * application, Logback is usually set up via {@link com.pmi.tpd.web.logback.web.LogbackConfigListener
 * LogbackConfigListener}, delegating to {@code LogbackWebConfigurer} underneath.
 *
 * @see com.pmi.tpd.web.logback.web.LogbackWebConfigurer
 * @see com.pmi.tpd.web.logback.web.LogbackConfigListener
 */
public final class LogbackConfigurer {

    private LogbackConfigurer() {
    }

    /**
     * Initialise Logback from the given file.
     *
     * @param location
     *                   the location of the config file: either a "classpath:" location (e.g. "classpath:logback.xml"),
     *                   an absolute file URL (e.g. "file:C:/logback.xml), or a plain absolute path in the file system
     *                   (e.g. "C:/logback.xml")
     * @param properties
     *                   optional properties to add to logback context.
     * @throws FileNotFoundException
     *                               Thrown if the location specifies an invalid file path
     * @throws JoranException
     *                               Thrown if the location specifies an invalid configuration file
     */
    public static void initLogging(@Nonnull final File location, @Nullable final Map<String, String> properties)
            throws FileNotFoundException, JoranException {
        final ContextSelector selector = getContextSelector();
        final LoggerContext loggerContext = selector.getLoggerContext();
        // in the current version Logback automatically configures the context
        // at startup, so we have to reset it
        loggerContext.reset();
        if (properties != null) {
            for (final Entry<String, String> entry : properties.entrySet()) {
                loggerContext.putProperty(entry.getKey(), entry.getValue());
            }
        }
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(location);
    }

    /**
     * Shut down Logback.
     * <p/>
     * This isn't strictly necessary, but recommended for shutting down Logback in a scenario where the host VM stays
     * alive (for example, when shutting down an application in a J2EE environment).
     */
    public static void shutdownLogging() {
        final ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        final LoggerContext loggerContext = selector.getLoggerContext();
        final String loggerContextName = loggerContext.getName();
        final LoggerContext context = selector.detachLoggerContext(loggerContextName);
        context.reset();
    }

    public static ContextSelector getContextSelector() {

        final ContextSelectorStaticBinder contextSelectorBinder = ContextSelectorStaticBinder.getSingleton();
        ContextSelector selector = contextSelectorBinder.getContextSelector();
        if (selector == null) {
            LoggerContext defaultLoggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            if (defaultLoggerContext == null) {
                defaultLoggerContext = new LoggerContext();
                defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
            }
            try {
                contextSelectorBinder.init(defaultLoggerContext, null);
                selector = contextSelectorBinder.getContextSelector();
            } catch (final Exception e) {
                Throwables.throwUnchecked(e);
            }
        }
        return selector;
    }

}
