package com.pmi.tpd.core.lifecycle;

import javax.annotation.Nonnull;

import org.springframework.context.ApplicationEvent;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.util.Assert;

/**
 * An <i>internal-only</i> event for updating startup {@link Progress progress}.
 * <p>
 * <b>Note</b>: This event is not intended to be published using the Application Events {@code IEventPublisher}; it
 * should be published using the Spring {@code ApplicationContext} or {@code ApplicationEventPublisher} instead so that
 * it does not get dispatched to general listeners.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class StartupProgressEvent extends ApplicationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final IProgress progress;

    /**
     * @param source
     * @param progress
     */
    public StartupProgressEvent(@Nonnull final Object source, @Nonnull final IProgress progress) {
        super(source);

        this.progress = Assert.checkNotNull(progress, "progress");
    }

    @Nonnull
    public IProgress getProgress() {
        return progress;
    }
}
