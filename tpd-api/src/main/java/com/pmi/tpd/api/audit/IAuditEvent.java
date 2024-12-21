package com.pmi.tpd.api.audit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Persist AuditEvent.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAuditEvent {

    /**
     * <p>
     * getPrincipal.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getPrincipal();

    /**
     * <p>
     * gets action.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAction();

    /**
     * <p>
     * getData.
     * </p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, String> getData();

    /**
     * Gets list of channels
     * 
     * @return Returns a list of {@code String} representing associated channels.
     */
    Set<String> getChannels();

    /**
     * <p>
     * getTimestamp.
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getTimestamp();

}
