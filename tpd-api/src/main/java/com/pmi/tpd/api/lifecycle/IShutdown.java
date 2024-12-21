package com.pmi.tpd.api.lifecycle;

/**
 * Used to shut something down.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public interface IShutdown {

    /**
     * Shutdown. Should not throw any exceptions.
     */
    void shutdown();
}
