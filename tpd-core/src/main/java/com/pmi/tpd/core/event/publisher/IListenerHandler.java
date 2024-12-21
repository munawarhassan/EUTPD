package com.pmi.tpd.core.event.publisher;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Interface to find invokers for a given listener objects. A typical example might be listeners that implement a
 * specific interface or that have annotated listener methods.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IListenerHandler {

    /**
     * Retrieves the list of invokers for the given listener.
     *
     * @param listener
     *            the listener object to get invokers for
     * @return a list of invokers linked to the listener object.
     * @throws java.lang.IllegalArgumentException
     *             if the {@code listener} is {@code null}
     */
    @Nonnull
    List<? extends IListenerInvoker> getInvokers(@Nonnull final Object listener);

    /**
     * Gets the indicating whether the {@code listener} contains at least a method supported by this
     * {@link com.pmi.tpd.core.event.publisher.IListenerHandler}.
     *
     * @param listener
     *            listener to check the support of this {@link com.pmi.tpd.core.event.publisher.IListenerHandler}.
     * @return Returns <code>true</code> whether the <code>listener</code> contains at least a method supported by this
     *         {@link com.pmi.tpd.core.event.publisher.IListenerHandler}, otherwise returns <code>false</code>.
     * @throws java.lang.IllegalArgumentException
     *             if the {@code listener} is {@code null}
     */
    boolean supportsHandler(@Nonnull final Object listener);
}
