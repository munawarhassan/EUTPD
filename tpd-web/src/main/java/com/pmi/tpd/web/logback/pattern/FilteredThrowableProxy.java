package com.pmi.tpd.web.logback.pattern;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.StringUtils;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.PublicThrowableProxyUtil;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

/**
 * Decorator around a {@link IThrowableProxy} that filters out uninteresting classes in its stacktrace (e.g. Tomcat
 * classes, Spring AOP decorators, etc.).
 */
public class FilteredThrowableProxy implements IThrowableProxy {

    /** */
    private static final Set<String> IGNORABLE_PREFIXES = ImmutableSet.<String> builder()
            .add("$Proxy")
            .add("com.sun.jersey.")
            .add("com.sun.jmx.")
            .add("com.sun.proxy.")
            .add("java.lang.ClassLoader")
            .add("java.lang.reflect.")
            .add("java.util.concurrent.ThreadPoolExecutor$Worker")
            .add("javax.management.remote.rmi.")
            .add("javax.servlet.http.HttpServlet")
            .add("org.apache.catalina.")
            .add("org.apache.coyote.")
            .add("org.apache.felix.")
            .add("org.apache.tomcat.")
            .add("org.springframework.aop.")
            .add("org.springframework.beans.factory.")
            .add("org.springframework.osgi.")
            .add("org.springframework.security.")
            .add("org.springframework.transaction.interceptor.")
            .add("org.springframework.web.filter.")
            .add("org.springframework.web.method.support.InvocableHandlerMethod")
            .add("org.springframework.web.servlet.")
            .add("org.tuckey.")
            .add("sun.reflect.")
            .add("sun.rmi.")
            .build();

    /** */
    private final IThrowableProxy throwable;

    /** */
    private final FilteredThrowableProxy cause;

    /** */
    private final FilteredThrowableProxy[] suppressed;

    /** */
    private final StackTraceElementProxy[] stackTrace;

    /** */
    private int commonFrames;

    /**
     * Default Construtor.
     * 
     * @param throwable
     *                  the throwable.
     */
    public FilteredThrowableProxy(final IThrowableProxy throwable) {
        this.throwable = throwable;
        this.stackTrace = filterElements(throwable.getStackTraceElementProxyArray());

        final IThrowableProxy cause = throwable.getCause();
        if (cause == null) {
            this.cause = null;
        } else {
            this.cause = new FilteredThrowableProxy(cause);
            this.cause.commonFrames = findNumberOfCommonFrames(this.cause, this);
        }

        final IThrowableProxy[] suppressed = throwable.getSuppressed();
        final FilteredThrowableProxy[] proxies = new FilteredThrowableProxy[suppressed.length];
        for (int i = 0; i < suppressed.length; i++) {
            proxies[i] = new FilteredThrowableProxy(suppressed[i]);
            proxies[i].commonFrames = findNumberOfCommonFrames(proxies[i], this);
        }
        this.suppressed = proxies;
    }

    @Override
    public String getClassName() {
        return throwable.getClassName();
    }

    @Override
    public IThrowableProxy getCause() {
        return cause;
    }

    @Override
    public int getCommonFrames() {
        return commonFrames;
    }

    @Override
    public String getMessage() {
        return throwable.getMessage();
    }

    @Override
    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return stackTrace.clone();
    }

    @Override
    public IThrowableProxy[] getSuppressed() {
        return suppressed.clone();
    }

    IThrowableProxy getThrowableProxy() {
        return throwable;
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    static class Placeholder extends StackTraceElementProxy {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** */
        private final String placeholder;

        Placeholder(final String placeholder) {
            super(new StackTraceElement("Unknown frame", "unknown", "Unknown frame", -1));
            this.placeholder = placeholder;
        }

        @Override
        public String toString() {
            return placeholder;
        }
    }

    private static int findNumberOfCommonFrames(final FilteredThrowableProxy cause,
        final FilteredThrowableProxy exception) {
        final StackTraceElementProxy[] causeProxyStack = cause.getStackTraceElementProxyArray();
        final StackTraceElement[] causeStack = Lists
                .transform(Arrays.asList(causeProxyStack), @Nullable StackTraceElementProxy::getStackTraceElement)
                .toArray(new StackTraceElement[causeProxyStack.length]);

        return PublicThrowableProxyUtil.findNumberOfCommonFrames(causeStack,
            exception.getStackTraceElementProxyArray());
    }

    private static StackTraceElementProxy[] filterElements(final StackTraceElementProxy[] stackTrace) {
        final List<StackTraceElementProxy> filteredStackTrace = Lists
                .newArrayListWithExpectedSize(estimateStackSize(stackTrace));

        if (stackTrace.length > 0) {
            // always add the first frame, in case it would otherwise be
            // filtered
            filteredStackTrace.add(stackTrace[0]);

            // iterate over the remaining frames and filter any of them that
            // matches the set of configured prefixes
            for (int i = 1; i < stackTrace.length; i++) {
                final StackTraceElementProxy element = stackTrace[i];
                final String clazz = element.getStackTraceElement().getClassName();
                boolean match = false;
                for (final String prefix : IGNORABLE_PREFIXES) {
                    match |= clazz.startsWith(prefix);
                    if (match) {
                        break;
                    }
                }
                if (!match) {
                    filteredStackTrace.add(element);
                }
            }
        }

        final int trimmed = stackTrace.length - filteredStackTrace.size();
        if (trimmed > 0) {
            filteredStackTrace.add(new Placeholder(
                    "... " + trimmed + ' ' + StringUtils.pluralise("frame", "frames", trimmed) + " trimmed"));
        }

        return filteredStackTrace.toArray(new StackTraceElementProxy[filteredStackTrace.size()]);
    }

    private static int estimateStackSize(final StackTraceElementProxy[] stackTrace) {
        return stackTrace.length / 10;
    }

    @Override
    public boolean isCyclic() {
        return this.throwable.isCyclic();
    }

}
