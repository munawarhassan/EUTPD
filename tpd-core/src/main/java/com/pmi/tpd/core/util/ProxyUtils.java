package com.pmi.tpd.core.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility methods for simplifying use of Java's {@code Proxy} support.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class ProxyUtils {

    /**
     * Prevents instantiation of this class.
     *
     * @throws UnsupportedOperationException
     *             Thrown if this class is instantiated via Reflection, or some other mechanism which ignores
     *             visibility.
     */
    private ProxyUtils() {
        throw new UnsupportedOperationException(getClass() + " is a utility class and should not be instantiated");
    }

    /**
     * Creates a new {@code Proxy} instance which will implement the specified interfaces and delegate any calls to
     * those interfaces methods to the provided {@code InvocationHandler} for processing.
     * <p>
     * The proxy will be generated using the {@code ClassLoader} for the {@code InvocationHandler}, since all calls to
     * the proxy will be handled by that object. It is expected that all of the interfaces the proxy implements will be
     * available from the {@code InvocationHandler}'s {@code ClassLoader}, as well as any classes the handler uses.
     *
     * @param interfaceClass
     *            the {@code class} for the primary interface which should be proxied
     * @param invocationHandler
     *            the handler for proxied method invocations
     * @param additionalInterfaces
     *            zero or more additional interfaces which the created proxy should implement <i>in addition to</i> the
     *            specified primary {@code interfaceClass}
     * @param <T>
     *            The type of the primary interface, used to simplify working with the returned proxy.
     * @return a new {@code Proxy} instance, cast to the type of the primary interface class
     * @throws NullPointerException
     *             if the provided {@code interfaceClass} or {@code invocationHandler} is {@code null}
     * @see #createProxy(Class, java.lang.reflect.InvocationHandler, ClassLoader, Class[])
     */
    @Nonnull
    public static <T> T createProxy(@Nonnull final Class<T> interfaceClass,
        @Nonnull final InvocationHandler invocationHandler,
        final Class<?>... additionalInterfaces) {
        checkNotNull(invocationHandler, "invocationHandler"); // All other arguments are checked in the other method

        return createProxy(interfaceClass,
            invocationHandler,
            invocationHandler.getClass().getClassLoader(),
            additionalInterfaces);
    }

    /**
     * Creates a new {@code Proxy} instance which will implement the specified interfaces and delegate any calls to
     * those interfaces' methods to the provided {@code InvocationHandler} for processing.
     *
     * @param interfaceClass
     *            the {@code class} for the primary interface which should be proxied
     * @param invocationHandler
     *            the handler for proxied method invocations
     * @param classLoader
     *            the {@code ClassLoader}
     * @param additionalInterfaces
     *            zero or more additional interfaces which the created proxy should implement <i>in addition to</i> the
     *            specified primary {@code interfaceClass}
     * @param <T>
     *            The type of the primary interface, used to simplify working with the returned proxy.
     * @return a new {@code Proxy} instance, cast to the type of the primary interface class
     * @throws NullPointerException
     *             if the provided {@code classLoader}, {@code interfaceClass} or {@code invocationHandler} is
     *             {@code null}
     */
    @Nonnull
    public static <T> T createProxy(@Nonnull final Class<T> interfaceClass,
        @Nonnull final InvocationHandler invocationHandler,
        @Nonnull final ClassLoader classLoader,
        final Class<?>... additionalInterfaces) {
        checkNotNull(classLoader, "classLoader");
        checkNotNull(interfaceClass, "interfaceClass");
        checkNotNull(invocationHandler, "invocationHandler");

        final Class<?>[] interfaces = (Class<?>[]) ArrayUtils.add(additionalInterfaces, interfaceClass);
        final Object proxy = Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);

        return interfaceClass.cast(proxy);
    }
}
