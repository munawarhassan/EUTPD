package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultErrorHandler.class);
    
    @Override
    public void handleError(final Throwable t) {
        LOGGER.warn("In default jms error handler...");
        LOGGER.error("Error Message : {}", t.getMessage());
    }
}
