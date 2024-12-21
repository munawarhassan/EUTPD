package com.pmi.tpd.web.logback;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class ConsoleEnabledPropertyDefiner extends ContextAwareBase implements PropertyDefiner {

    @Override
    public String getPropertyValue() {
        return "true";
    }

}
