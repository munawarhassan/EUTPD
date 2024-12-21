package com.pmi.tpd.api.scheduler;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITaskFactory {

    /**
     * @param cl
     * @return
     */
    <T> T getInstance(Class<T> requiredType);

    /**
     * @param <T>
     * @param requiredType
     * @param name
     * @return
     */
    <T> T getBean(final Class<T> requiredType, String name);

    /**
     * @param obj
     */
    void injectMembers(Object obj);

    /**
     * @param clazz
     * @return
     */
    <T> T createInstance(final Class<T> clazz);

}
