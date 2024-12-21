package com.pmi.tpd.web.rest;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class HandlingErrorFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {

        context.register(UnhandledExceptionMapper.class);
        return true;
    }
}
