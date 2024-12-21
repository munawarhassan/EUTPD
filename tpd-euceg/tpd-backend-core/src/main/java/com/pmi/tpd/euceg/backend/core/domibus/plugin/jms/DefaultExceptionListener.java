package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionListener implements ExceptionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionListener.class);

    @Override
    public void onException(final JMSException exception) {
        LOGGER.warn("In default jms error handler...");
        LOGGER.error("Error Message : {}", exception.getMessage());
    }
}
