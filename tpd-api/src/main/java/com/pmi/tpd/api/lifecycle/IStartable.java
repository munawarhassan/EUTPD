package com.pmi.tpd.api.lifecycle;

/**
 * Implementing this interface allows Components to be notified of when the application has started.
 * <p>
 * After the system is initialised and components added to the dependency injection framework, then components
 * implementing this interface will have their {@link #start()} method called. Note that only plugin modules of type
 * Component will be considered as "Startable".
 *
 * @author devacfr
 * @since 1.0
 */
public interface IStartable {

    /**
     * This method will be called after the system is fully initialised and all components added to the dependency
     * injection framework.
     *
     * @throws java.lang.Exception
     *             Allows implementations to throw an java.lang.Exception.
     */
    void start();
}
