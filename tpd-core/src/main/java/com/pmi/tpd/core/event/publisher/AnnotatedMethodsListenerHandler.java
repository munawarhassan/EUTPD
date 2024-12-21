package com.pmi.tpd.core.event.publisher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * A listener handler that will check for single parameter methods annotated with the given annotation.
 * </p>
 * <p>
 * The default annotation for methods is {@link com.pmi.tpd.api.event.annotation.EventListener}.
 * </p>
 *
 * @see EventListener
 * @since 1.0
 * @author Christophe Friederich
 */
public final class AnnotatedMethodsListenerHandler implements IListenerHandler {

    /** associate logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedMethodsListenerHandler.class);

    /** annotation used (default @link {@link EventListener}). */
    private final Class<? extends Annotation> annotation;

    /**
     * Create new instance initialised by default.
     */
    public AnnotatedMethodsListenerHandler() {
        this(EventListener.class);
    }

    /**
     * Create new instance with specific {@code annotation}.
     *
     * @param annotation
     *            annotation used to define the listener handler method.
     * @throws java.lang.IllegalArgumentException
     *             if the {@code annotation} is {@code null}
     */
    public AnnotatedMethodsListenerHandler(@Nonnull final Class<? extends Annotation> annotation) {
        this.annotation = Assert.notNull(annotation);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<? extends IListenerInvoker> getInvokers(@Nonnull final Object listener) {
        final List<Method> validMethods = getValidMethods(Assert.notNull(listener));

        if (validMethods.isEmpty()) {
            LOGGER.debug("Couldn't find any valid listener methods on class <{}>", listener.getClass().getName());
        }

        return Lists.transform(validMethods, new Function<Method, IListenerInvoker>() {

            @Override
            public IListenerInvoker apply(final Method method) {
                return new SingleParameterMethodListenerInvoker(listener, method);
            }
        });
    }

    @Nonnull
    private List<Method> getValidMethods(@Nonnull final Object listener) {
        final List<Method> annotatedMethods = Lists.newArrayList();
        for (final Method method : listener.getClass().getMethods()) {
            if (isValidMethod(method)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsHandler(@Nonnull final Object listener) {
        for (final Method method : Assert.notNull(listener).getClass().getMethods()) {
            if (isValidMethod(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidMethod(@Nonnull final Method method) {
        if (isAnnotated(method)) {
            if (hasOneAndOnlyOneParameter(method)) {
                return true;
            } else {
                throw new RuntimeException(
                        "Method <" + method + "> of class <" + method.getDeclaringClass() + "> " + "is annotated with <"
                                + annotation.getClass().getName() + "> but has 0 or more than 1 parameters! "
                                + "Listener methods MUST have 1 and only 1 parameter.");
            }
        }
        return false;
    }

    private boolean isAnnotated(@Nonnull final Method method) {
        return method.getAnnotation(annotation) != null;
    }

    private boolean hasOneAndOnlyOneParameter(@Nonnull final Method method) {
        return method.getParameterTypes().length == 1;
    }
}
