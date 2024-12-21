package com.pmi.tpd.api.util;

import static com.pmi.tpd.api.util.ClassUtils.cast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class is extremely useful for loading resources and classes in a fault tolerant manner that works across
 * different applications servers.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ClassLoaderUtils {

    private ClassLoaderUtils() {
    }

    /**
     * Load a class with a given name.
     * <p>
     * It will try to load the class in the following order:
     * <ul>
     * <li>From {@link java.lang.Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     * <li>Using the basic {@link java.lang.Class#forName(java.lang.String)}
     * <li>From {@link java.lang.Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     * <li>From the {@link java.lang.Class#getClassLoader() callingClass.getClassLoader()}
     * </ul>
     *
     * @param className
     *            The name of the class to load
     * @param callingClass
     *            The Class object of the calling object
     * @throws java.lang.ClassNotFoundException
     *             If the class cannot be found anywhere.
     * @return a {@link java.lang.Class} object.
     * @param <T>
     *            a T object
     */
    public static <T> Class<T> loadClass(final String className, final Class<?> callingClass)
            throws ClassNotFoundException {
        try {
            return cast(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (final ClassNotFoundException e) {
            try {
                return cast(Class.forName(className));
            } catch (final ClassNotFoundException ex) {
                try {
                    return cast(ClassLoaderUtils.class.getClassLoader().loadClass(className));
                } catch (final ClassNotFoundException exc) {
                    if (callingClass != null && callingClass.getClassLoader() != null) {
                        return cast(callingClass.getClassLoader().loadClass(className));
                    } else {
                        throw exc;
                    }
                }
            }
        }
    }

    /**
     * Load a class with a given name.
     * <p>
     * It will try to load the class in the following order:
     * <ul>
     * <li>From {@link java.lang.Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     * <li>Using the basic {@link java.lang.Class#forName(java.lang.String)}
     * <li>From {@link java.lang.Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     * <li>From the {@link java.lang.Class#getClassLoader() callingClass.getClassLoader()}
     * </ul>
     *
     * @param className
     *            The name of the class to load
     * @param callingClassLoader
     *            The ClassLoader the calling object which will be used to look up className
     * @throws java.lang.ClassNotFoundException
     *             If the class cannot be found anywhere.
     * @return a {@link java.lang.Class} object.
     * @param <T>
     *            a T object
     */
    public static <T> Class<T> loadClass(final String className, final ClassLoader callingClassLoader)
            throws ClassNotFoundException {
        try {
            return cast(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (final ClassNotFoundException e) {
            try {
                return cast(Class.forName(className));
            } catch (final ClassNotFoundException ex) {
                try {
                    return cast(ClassLoaderUtils.class.getClassLoader().loadClass(className));
                } catch (final ClassNotFoundException exc) {
                    if (callingClassLoader != null) {
                        return cast(callingClassLoader.loadClass(className));
                    } else {
                        throw exc;
                    }
                }

            }
        }
    }

    /**
     * Load a given resource.
     * <p>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>From {@link java.lang.Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     * <li>From {@link java.lang.Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     * <li>From the {@link java.lang.Class#getClassLoader() callingClass.getClassLoader()}
     * </ul>
     *
     * @param resourceName
     *            The name of the resource to load
     * @param callingClass
     *            The Class object of the calling object
     * @return a {@link java.net.URL} object.
     */
    public static URL getResource(final String resourceName, final Class<?> callingClass) {
        URL url = null;

        url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null) {
            url = ClassLoaderUtils.class.getClassLoader().getResource(resourceName);
        }

        if (url == null) {
            url = callingClass.getClassLoader().getResource(resourceName);
        }
        return url;
    }

    /**
     * getBundle() version of getResource() (that checks against the same list of class loaders).
     *
     * @param resourceName
     *            a {@link java.lang.String} object.
     * @param locale
     *            a {@link java.util.Locale} object.
     * @param callingClass
     *            a {@link java.lang.Class} object.
     * @return a {@link java.util.ResourceBundle} object.
     */
    public static ResourceBundle getBundle(final String resourceName,
        final Locale locale,
        final Class<?> callingClass) {
        ResourceBundle bundle = null;

        bundle = ResourceBundle.getBundle(resourceName, locale, Thread.currentThread().getContextClassLoader());

        if (bundle == null) {
            bundle = ResourceBundle.getBundle(resourceName, locale, ClassLoaderUtils.class.getClassLoader());
        }

        if (bundle == null) {
            bundle = ResourceBundle.getBundle(resourceName, locale, callingClass.getClassLoader());
        }
        return bundle;
    }

    /**
     * returns all found resources as java.net.URLs.
     * <p>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>From {@link java.lang.Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     * <li>From {@link java.lang.Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     * <li>From the {@link java.lang.Class#getClassLoader() callingClass.getClassLoader()}
     * </ul>
     *
     * @param resourceName
     *            The name of the resource to load
     * @param callingClass
     *            The Class object of the calling object
     * @return a {@link java.util.Enumeration} object.
     * @throws java.io.IOException
     *             if any.
     */
    public static Enumeration<URL> getResources(final String resourceName, final Class<?> callingClass)
            throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resourceName);
        if (urls == null) {
            urls = ClassLoaderUtils.class.getClassLoader().getResources(resourceName);
            if (urls == null) {
                urls = callingClass.getClassLoader().getResources(resourceName);
            }
        }

        return urls;
    }

    /**
     * This is a convenience method to load a resource as a stream. The algorithm used to find the resource is given in
     * getResource()
     *
     * @param resourceName
     *            The name of the resource to load
     * @param callingClass
     *            The Class object of the calling object
     * @return a {@link java.io.InputStream} object.
     */
    public static InputStream getResourceAsStream(final String resourceName, final Class<?> callingClass) {
        final URL url = getResource(resourceName, callingClass);
        try {
            return url != null ? url.openStream() : null;
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Prints the current classloader hierarchy - useful for debugging.
     */
    public static void printClassLoader() {
        // System.out.println("ClassLoaderUtils.printClassLoader");
        printClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Prints the classloader hierarchy from a given classloader - useful for debugging.
     *
     * @param cl
     *            a {@link java.lang.ClassLoader} object.
     */
    public static void printClassLoader(final ClassLoader cl) {
        // System.out.println("ClassLoaderUtils.printClassLoader(cl = " + cl + ")");
        if (cl != null) {
            printClassLoader(cl.getParent());
        }
    }
}
