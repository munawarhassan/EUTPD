package com.pmi.tpd.web.core.rs.container;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>
 * SysFilteringFeature class.
 * </p>
 *
 * @author icode
 */
public class RequestFilteringFeature implements Feature {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration configuration = context.getConfiguration();

        if (!configuration.isRegistered(ForwardedProtocolRequestFilter.class)) {
            context.register(ForwardedProtocolRequestFilter.class);
        }
        return true;
    }
}