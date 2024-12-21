package com.pmi.tpd.util;

import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.util.ClassLoaderUtils;

public final class ComponentUtils {

    private ComponentUtils() {

    }

    /**
     * This method will load and construct a class, to inject any dependencies.
     *
     * @param className
     *            the name of the class to load.
     * @param callingClass
     *            the class requesting the class be loaded.
     * @throws ClassNotFoundException
     *             if the class is not found on the classpath of the classloader of the calling class.
     * @see ClassLoaderUtils#loadClass(java.lang.String,java.lang.Class)
     * @see ComponentManager
     * @return the instance of the requested class.
     */
    public static <T> T loadComponent(final String className, final Class<?> callingClass)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        final Class<T> componentClass = (Class<T>) ClassLoaderUtils.loadClass(className, callingClass);
        return loadComponent(componentClass);
    }

    /**
     * @param componentClass
     * @return
     */
    public static <T> T loadComponent(final Class<T> componentClass) {
        if (componentClass.isAssignableFrom(Void.class)) {
            return null;
        }

        // register the class that we need.
        return ComponentManager.getInjector().registerSingletonComponentImplementation(componentClass);
    }
}
