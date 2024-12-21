package com.pmi.tpd.core.inject;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.InfrastructureException;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IComponentManager {

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    enum Scope {
        /** */
        Singleton,
        /** */
        Prototype
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    abstract class Factory implements IComponentManager {

        /** */
        private static Factory instance;

        /**
         * @param factory
         */
        private static synchronized void setInstance(final Factory factory) {
            instance = factory;
        }

        /**
         *
         */
        public Factory() {
            setInstance(this);
        }

        /**
         * @return
         */
        public static Factory getInstance() {
            return instance;
        }

    }

    /**
     * Gets the bean instance that uniquely matches the given object type, if any.
     *
     * @param <T>
     *            the given class type to retrieve.
     * @param requiredType
     *            type the bean must match; can be an interface or superclass (can <b>not</b> be {@code null}).
     * @return Returns an instance, which may be shared or independent, of the specified bean.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T getComponentInstanceOfType(@Nonnull final Class<T> requiredType) throws InfrastructureException;

    /**
     * @param <T>
     *            the given class type to retrieve.
     * @param clazz
     *            a type the bean must match. Can be an interface or superclass of the actual class, or null for any
     *            match. For example, if the value is {@code Object.class}, this method will succeed whatever the class
     *            of the returned instance.
     * @param name
     *            the name of the bean to retrieve (can <b>not</b> be {@code null} or empty).
     * @return Returns an instance, which may be shared or independent, of the specified bean.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T getComponentInstanceOfType(@Nullable final Class<T> clazz, @Nonnull String name)
            throws InfrastructureException;

    /**
     * Gets the bean instances that match the given object type (including subclasses).
     *
     * @param <T>
     *            the given class type to retrieve.
     * @param clazz
     *            type the class or interface to match, or {@code null} for all concrete beans.
     * @return Returns a list of instance, which may be shared or independent, of the specified bean.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> List<T> getComponentInstancesOfType(@Nullable final Class<T> clazz) throws InfrastructureException;

    /**
     * Fully create a new singleton scoped bean instance of the given class with the specified autowire strategy. All
     * constants defined in this interface are supported here.
     *
     * @param <T>
     *            the given class type.
     * @param clazz
     *            a given class to create (can <b>not</b> be {@code null}).
     * @return Returns the new bean instance.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T registerSingletonComponentImplementation(@Nonnull final Class<T> clazz) throws InfrastructureException;

    /**
     * Fully create a new singleton scoped bean instance of the given class with the specified autowire strategy. All
     * constants defined in this interface are supported here.
     *
     * @param <T>
     *            the given class type.
     * @param clazz
     *            a given class to create (can <b>not</b> be {@code null}).
     * @param name
     *            the name of this bean (can <b>not</b> be {@code null} or empty).
     * @return Returns the new bean instance.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T registerSingletonComponentImplementation(@Nonnull final Class<T> clazz, @Nonnull String name)
            throws InfrastructureException;

    /**
     * Fully create a new bean instance of the given class with the specified autowire strategy. All constants defined
     * in this interface are supported here.
     *
     * @param <T>
     *            the given class type.
     * @param clazz
     *            a given class to create (can <b>not</b> be {@code null}).
     * @param scope
     *            the target scope of this bean (can be {@code null}), default is {@link Scope#Singleton}.
     * @return Returns the new bean instance.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T registerComponentImplementation(@Nonnull final Class<T> clazz, @Nullable Scope scope)
            throws InfrastructureException;

    /**
     * Fully create a new bean instance of the given class with the specified autowire strategy. All constants defined
     * in this interface are supported here.
     *
     * @param <T>
     *            the given class type.
     * @param clazz
     *            a given class to create (can <b>not</b> be {@code null}).
     * @param name
     *            the name of this bean (can <b>not</b> be {@code null} or empty).
     * @param scope
     *            the target scope of this bean (can be {@code null}), default is {@link Scope#Singleton}.
     * @return Returns the new bean instance.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T registerComponentImplementation(@Nonnull final Class<T> clazz, @Nonnull String name, @Nullable Scope scope)
            throws InfrastructureException;

    /**
     * register a given bean as singleton with the specified autowire strategy. All constants defined in this interface
     * are supported here.
     *
     * @param <T>
     *            the given class type.
     * @param bean
     *            a given bean to register (can <b>not</b> be {@code null}).
     * @param name
     *            the name of this bean (can <b>not</b> be {@code null} or empty).
     * @return Returns the new bean instance.
     * @throws InfrastructureException
     *             if the DI has not started yet.
     */
    @Nonnull
    <T> T registerComponent(@Nonnull T bean, @Nonnull String name) throws InfrastructureException;

}
