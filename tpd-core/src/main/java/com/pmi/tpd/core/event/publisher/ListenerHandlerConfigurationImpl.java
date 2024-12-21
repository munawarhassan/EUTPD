package com.pmi.tpd.core.event.publisher;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmi.tpd.core.event.config.IListenerHandlersConfiguration;

/**
 * <p>
 * The default configuration that only uses the
 * {@link com.pmi.tpd.core.event.publisher.AnnotatedMethodsListenerHandler}.
 * </p>
 * <p>
 * Products that need to remain backward compatible will have to override this configuration
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ListenerHandlerConfigurationImpl implements IListenerHandlersConfiguration {

    /** {@inheritDoc} */
    @Override
    public List<IListenerHandler> getListenerHandlers() {
        return Lists.<IListenerHandler> newArrayList(new AnnotatedMethodsListenerHandler());
    }
}
