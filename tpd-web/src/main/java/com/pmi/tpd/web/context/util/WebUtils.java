package com.pmi.tpd.web.context.util;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.springframework.util.StringUtils;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.web.logback.web.LogbackWebConfigurer;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class WebUtils {

    /**
     * Web app root key parameter at the servlet context level (i.e. a context-param in {@code web.xml}):
     * "webAppRootKey".
     */
    public static final String WEB_APP_ROOT_KEY_PARAM = "webAppRootKey";

    /** Default web app root key: "webapp.root". */
    public static final String DEFAULT_WEB_APP_ROOT_KEY = "webapp.root";

    private WebUtils() {
    }

    /**
     * Return the real path of the given path within the web application, as provided by the servlet container.
     * <p>
     * Prepends a slash if the path does not already start with a slash, and throws a FileNotFoundException if the path
     * cannot be resolved to a resource (in contrast to ServletContext's {@code getRealPath}, which returns null).
     *
     * @param servletContext
     *            the servlet context of the web application (can <b>not</b> be {@code null}).
     * @param path
     *            the path within the web application (can <b>not</b> be {@code null} or empty).
     * @return the corresponding real path
     * @throws FileNotFoundException
     *             if the path cannot be resolved to a resource
     * @see javax.servlet.ServletContext#getRealPath
     */
    public static String getRealPath(final ServletContext servletContext, String path) throws FileNotFoundException {
        Assert.checkHasText(path, "path");
        Assert.checkNotNull(servletContext, "servletContext");
        // Interpret location as relative to the web application root directory.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        final String realPath = servletContext.getRealPath(path);
        if (realPath == null) {
            throw new FileNotFoundException("ServletContext resource [" + path
                    + "] cannot be resolved to absolute file path - " + "web application archive not expanded?");
        }
        return realPath;
    }

    /**
     * Gets the {@code resource} located at the named path as an {@link InputStream} object.
     *
     * @param servletContext
     *            the servlet context of the web application (can <b>not</b> be {@code null}).
     * @param path
     *            the path within the web application (can <b>not</b> be {@code null} or empty).
     * @return Returns the {@code resource} located at the named path as an {@link InputStream} object.
     * @throws FileNotFoundException
     *             if resource doesn't exist.
     */
    public static InputStream getResourceAsStream(@Nonnull final ServletContext servletContext, String path)
            throws FileNotFoundException {
        Assert.checkHasText(path, "path");
        Assert.checkNotNull(servletContext, "servletContext");
        // Interpret location as relative to the web application root directory.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        final InputStream input = servletContext.getResourceAsStream(path);
        if (input == null) {
            throw new FileNotFoundException("ServletContext resource [" + path + "] cannot be resolved");
        }
        return input;
    }

    /**
     * Set a system property to the web application root directory. The key of the system property can be defined with
     * the "webAppRootKey" context-param in {@code web.xml}. Default is "webapp.root".
     * <p>
     * Can be used for tools that support substition with {@code System.getProperty} values, like log4j's "${key}"
     * syntax within log file locations.
     *
     * @param servletContext
     *            the servlet context of the web application (can <b>not</b> be {@code null}).
     * @throws IllegalStateException
     *             if the system property is already set, or if the WAR file is not expanded
     * @see #WEB_APP_ROOT_KEY_PARAM
     * @see #DEFAULT_WEB_APP_ROOT_KEY
     * @see org.springframework.web.util.WebAppRootListener
     * @see LogbackWebConfigurer
     */
    public static void setWebAppRootSystemProperty(@Nonnull final ServletContext servletContext)
            throws IllegalStateException {
        Assert.checkNotNull(servletContext, "servletContext");
        final String root = servletContext.getRealPath("/");
        if (root == null) {
            throw new IllegalStateException("Cannot set web app root system property when WAR file is not expanded");
        }
        final String param = servletContext.getInitParameter(WEB_APP_ROOT_KEY_PARAM);
        final String key = param != null ? param : DEFAULT_WEB_APP_ROOT_KEY;
        final String oldValue = System.getProperty(key);
        if (oldValue != null && !StringUtils.pathEquals(oldValue, root)) {
            throw new IllegalStateException("Web app root system property already set to different value: '" + key
                    + "' = [" + oldValue + "] instead of [" + root + "] - "
                    + "Choose unique values for the 'webAppRootKey' context-param in your web.xml files!");
        }
        System.setProperty(key, root);
        servletContext.log("Set web app root system property: '" + key + "' = [" + root + "]");
    }

    /**
     * Remove the system property that points to the web app root directory. To be called on shutdown of the web
     * application.
     *
     * @param servletContext
     *            the servlet context of the web application (can <b>not</b> be {@code null}).
     * @see #setWebAppRootSystemProperty
     */
    public static void removeWebAppRootSystemProperty(final ServletContext servletContext) {
        Assert.checkNotNull(servletContext, "servletContext");
        final String param = servletContext.getInitParameter(WEB_APP_ROOT_KEY_PARAM);
        final String key = param != null ? param : DEFAULT_WEB_APP_ROOT_KEY;
        System.getProperties().remove(key);
    }
}
