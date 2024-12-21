package com.pmi.tpd.core.event.advisor.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.advisor.event.Event;

/**
 * Describes a strategy for translating an exception to an {@link Event}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IEventExceptionTranslator {

    /**
     * Attempt to translate the provided {@code Throwable} to an {@link Event event}. Implementors may return
     * {@code null} if an event should not be created, or to allow for chaining translators to handle specific cases
     * separately.
     *
     * @param thrown
     *            the exception to translate
     * @return an {@link Event event}, or {@code null} if the exception could not, or should not, be translated
     */
    @Nullable
    Event translate(@Nonnull Throwable thrown);
}
