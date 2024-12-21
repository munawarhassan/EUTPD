package com.pmi.tpd.core.event.advisor.spring.lifecycle;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.google.common.base.MoreObjects;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class LifecycleUtils {

    /** */
    private static final String ATTR_STATE = LifecycleState.class.getName() + ":Current";

    private LifecycleUtils() {
        throw new UnsupportedOperationException(getClass() + " is a utility class and should not be instantiated.");
    }

    @Nonnull
    public static LifecycleState getCurrentState(@Nonnull final ServletContext servletContext) {
        final LifecycleState state = (LifecycleState) servletContext.getAttribute(ATTR_STATE);

        return MoreObjects.firstNonNull(state, LifecycleState.CREATED);
    }

    public static boolean isStarting(@Nonnull final ServletContext servletContext) {
        final LifecycleState state = getCurrentState(servletContext);

        return state == LifecycleState.CREATED || state == LifecycleState.STARTING;
    }

    public static void updateState(@Nonnull final ServletContext servletContext, @Nonnull final LifecycleState state) {
        servletContext.setAttribute(ATTR_STATE, state);
    }
}
