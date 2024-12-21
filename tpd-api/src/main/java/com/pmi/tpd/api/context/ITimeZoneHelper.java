package com.pmi.tpd.api.context;

import java.util.TimeZone;

import javax.annotation.Nonnull;

/**
 * A helper which can retrieve the current time zone for the server.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITimeZoneHelper {

  @Nonnull
  TimeZone getTimeZone();

}
