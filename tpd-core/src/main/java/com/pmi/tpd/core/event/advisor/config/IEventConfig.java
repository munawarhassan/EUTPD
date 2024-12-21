package com.pmi.tpd.core.event.advisor.config;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.api.event.advisor.event.IEventCheck;
import com.pmi.tpd.core.event.advisor.IContainerFactory;
import com.pmi.tpd.core.event.advisor.IRequestEventCheck;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IEventConfig {

    /**
     * @return
     */
    @Nonnull
    <CONTEXT> List<IApplicationEventCheck<CONTEXT>> getApplicationEventChecks();

    /**
     * @return
     */
    @Nonnull
    IContainerFactory getContainerFactory();

    /**
     * @return
     */
    @Nonnull
    String getErrorPath();

    /**
     * @param id
     * @return
     */
    @Nullable
    IEventCheck getEventCheck(int id);

    /**
     * @return
     */
    @Nonnull
    List<IEventCheck> getEventChecks();

    /**
     * @param level
     * @return
     */
    @Nullable
    EventLevel getEventLevel(@Nonnull String level);

    /**
     * @param type
     * @return
     */
    @Nullable
    EventType getEventType(@Nonnull String type);

    /**
     * @return
     */
    @Nonnull
    List<String> getIgnorePaths();

    /**
     * @return
     */
    @Nonnull
    Map<String, String> getParams();

    /**
     * @return
     */
    @Nonnull
    <REQUEST> List<IRequestEventCheck<REQUEST>> getRequestEventChecks();

    /**
     * @return
     */
    @Nonnull
    <REQUEST> ISetupConfig getSetupConfig();

    /**
     * @return
     */
    @Nonnull
    String getSetupPath();

    /**
     * @param uri
     * @return
     */
    boolean isIgnoredPath(@Nonnull String uri);
}
