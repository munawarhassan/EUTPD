package com.pmi.tpd.web.logback;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Defines the default level to be used in the majority of log files.
 */
public class LogLevelPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    /** */
    public static final String LOG_LEVEL = "loglevel";

    @Override
    public String getPropertyValue() {
        final String loglevel = getContext().getProperty(LOG_LEVEL);
        // if (StringUtils.isEmpty(loglevel)) {
        // loglevel = System.getProperty(LOG_LEVEL, Level.WARN.toString());
        // }
        return loglevel;
    }

}
