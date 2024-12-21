package com.pmi.tpd.core.event.config;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.event.publisher.IListenerHandler;

/**
 * Specifies a listener handler configuration to use.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IListenerHandlersConfiguration {

    /**
     * Gets the list of listener handlers used to find a invokers for a given listener objects.
     *
     * @return Returns the list of listener handlers used.
     */
    @Nonnull
    List<IListenerHandler> getListenerHandlers();
}
