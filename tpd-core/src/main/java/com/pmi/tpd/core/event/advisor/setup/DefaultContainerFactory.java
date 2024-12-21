package com.pmi.tpd.core.event.advisor.setup;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.core.event.advisor.IContainerFactory;
import com.pmi.tpd.core.event.advisor.support.DefaultEventContainer;

/**
 * This provides the old, non-multitenant functionality. This way existing apps don't need to worry about the
 * container-factory xml element and all that stuff
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultContainerFactory implements IContainerFactory {

    @Override
    @Nonnull
    public IEventContainer create() {
        return new DefaultEventContainer();
    }
}
