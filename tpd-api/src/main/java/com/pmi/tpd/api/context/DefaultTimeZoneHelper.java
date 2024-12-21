package com.pmi.tpd.api.context;

import java.util.TimeZone;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultTimeZoneHelper implements ITimeZoneHelper {

    @SuppressWarnings("null")
    @Nonnull
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

}
