package com.pmi.tpd.core.event.server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.core.context.BaseApplicationProperties;

/**
 * Event that is published when an application property is changed through {@link BaseApplicationProperties}.
 * <p>
 * This event is internally audited with a HIGH priority.
 */
public class ApplicationConfigurationChangedEvent<T> extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String property;

    /** */
    private final T oldValue;

    /** */
    private final T newValue;

    public ApplicationConfigurationChangedEvent(@Nonnull final Object source, @Nonnull final String property,
            @Nullable final T oldValue, @Nullable final T newValue) {
        super(source);
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Nonnull
    public String getProperty() {
        return property;
    }

    @Nullable
    public T getOldValue() {
        return oldValue;
    }

    @Nullable
    public T getNewValue() {
        return newValue;
    }
}