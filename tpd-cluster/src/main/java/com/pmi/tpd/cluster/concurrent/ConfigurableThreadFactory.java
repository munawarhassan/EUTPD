package com.pmi.tpd.cluster.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * Extends Spring's {@code CustomizableThreadFactory} with the ability to set a configured context {@code ClassLoader}
 * on all new threads.
 * <p>
 * The behavior for {@link #setThreadNamePrefix(String) thread name prefixes} is also altered slightly, as well as the
 * {@link #getDefaultThreadNamePrefix() default prefix}, to make the naming consistent with {@code ThreadFactories},
 * which this factory is replacing.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ConfigurableThreadFactory extends CustomizableThreadFactory {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private ClassLoader classLoader;

    /**
     *
     */
    public ConfigurableThreadFactory() {
    }

    @Override
    public Thread createThread(final Runnable runnable) {
        final Thread thread = super.createThread(runnable);
        thread.setContextClassLoader(classLoader);

        return thread;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setThreadNamePrefix(String threadNamePrefix) {
        threadNamePrefix = StringUtils.trimToNull(threadNamePrefix);
        if (threadNamePrefix != null) {
            // This more closely mimics the naming from Atlassian Concurrent's ThreadFactories
            threadNamePrefix += ":" + getDefaultThreadNamePrefix();
        }
        super.setThreadNamePrefix(threadNamePrefix);
    }

    @Override
    protected String getDefaultThreadNamePrefix() {
        return "thread-";
    }
}
