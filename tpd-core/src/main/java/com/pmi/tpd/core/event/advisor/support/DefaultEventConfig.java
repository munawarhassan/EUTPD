package com.pmi.tpd.core.event.advisor.support;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.api.event.advisor.event.IEventCheck;
import com.pmi.tpd.core.event.advisor.IContainerFactory;
import com.pmi.tpd.core.event.advisor.IRequestEventCheck;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletContainerFactory;
import com.pmi.tpd.core.event.advisor.setup.DefaultSetupConfig;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;

/**
 * A default implementation of {@link IEventConfig} which may be used as a failsafe when no other configuration is
 * available.
 * <p/>
 * All URIs are {@link #isIgnoredPath(String) ignored} by this implementation. All collection-returning properties are
 * implemented to return empty, immutable collections. All other methods generally return {@code null}, except for:
 * <ul>
 * <li>{@link SetupConfig} is provided by {@link DefaultSetupConfig}</li>
 * <li>{@link IContainerFactory} is provided by {@link ServletContainerFactory}</li>
 * </ul>
 * This class cannot be instantiated. A single, immutable instance is available via {@link #getInstance()}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DefaultEventConfig implements IEventConfig {

    /** */
    private static final DefaultEventConfig INSTANCE = new DefaultEventConfig();

    /** */
    private final IContainerFactory containerFactory;

    /** */
    private final ISetupConfig setupConfig;

    /**
     *
     */
    private DefaultEventConfig() {
        containerFactory = new ServletContainerFactory();
        setupConfig = new DefaultSetupConfig();
    }

    /**
     * Retrieves the immutable singleton instance of the default configuration.
     *
     * @return the default configuration singleton
     */
    @Nonnull
    public static IEventConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Always empty but non-{@code null}.
     *
     * @return an empty list
     */
    @Nonnull
    @Override
    public <CONTEXT> List<IApplicationEventCheck<CONTEXT>> getApplicationEventChecks() {
        return Collections.emptyList();
    }

    /**
     * Always an instance of {@link ServletContainerFactory}.
     *
     * @return a default container factory
     */
    @Nonnull
    @Override
    public IContainerFactory getContainerFactory() {
        return containerFactory;
    }

    /**
     * Always {@code "/unavailable"}. {@link #isIgnoredPath(String)} always returns {@code true}, so this path should
     * never be accessed; event processing is bypassed for all paths.
     *
     * @return {@code "/unavailable"}
     */
    @Nonnull
    @Override
    public String getErrorPath() {
        return "/unavailable";
    }

    /**
     * Always {@code null}.
     *
     * @param id
     *            ignored
     * @return {@code null}
     */
    @Override
    public IEventCheck getEventCheck(final int id) {
        return null;
    }

    /**
     * Always empty but non-{@code null}.
     *
     * @return an empty list
     */
    @Nonnull
    @Override
    public List<IEventCheck> getEventChecks() {
        return Collections.emptyList();
    }

    /**
     * Always {@code null}.
     *
     * @param level
     *            ignored
     * @return {@code null}
     */
    @Override
    public EventLevel getEventLevel(@Nonnull final String level) {
        checkNotNull(level, "level");

        return null;
    }

    /**
     * Always {@code null}.
     *
     * @param type
     *            ignored
     * @return {@code null}
     */
    @Override
    public EventType getEventType(@Nonnull final String type) {
        checkNotNull(type, "type");

        return null;
    }

    /**
     * Always empty but non-{@code null}.
     *
     * @return an empty list
     */
    @Nonnull
    @Override
    public List<String> getIgnorePaths() {
        return Collections.emptyList();
    }

    /**
     * Always empty but non-{@code null}.
     *
     * @return an empty map
     */
    @Nonnull
    @Override
    public Map<String, String> getParams() {
        return Collections.emptyMap();
    }

    /**
     * Always empty but non-{@code null}.
     *
     * @return an empty list
     */
    @Nonnull
    @Override
    public <R> List<IRequestEventCheck<R>> getRequestEventChecks() {
        return Collections.emptyList();
    }

    /**
     * Always an instance of {@link DefaultSetupConfig}.
     */
    @Nonnull
    @Override
    public <R> ISetupConfig getSetupConfig() {
        return setupConfig;
    }

    /**
     * Always {@code "/setup"}. The default configuration is always setup, so this path should never be accessed.
     *
     * @return {@code "/setup"}
     */
    @Nonnull
    @Override
    public String getSetupPath() {
        return "/setup";
    }

    /**
     * Always {@code true}. This has the net effect of disabling, because all URIs included for filtering will be
     * ignored and events will not be processed for them.
     *
     * @param uri
     *            ignored
     * @return {@code true}
     */
    @Override
    public boolean isIgnoredPath(@Nonnull final String uri) {
        checkNotNull(uri, "uri");

        return true;
    }
}
