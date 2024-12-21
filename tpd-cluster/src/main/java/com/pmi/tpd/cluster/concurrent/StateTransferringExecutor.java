package com.pmi.tpd.cluster.concurrent;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.ImmutableList;

/**
 * A wrapper around the standard Java {@code Executor} interface which captures the state of the current thread at the
 * time a job is executed and applies that state to and removes that state from the worker thread on which the job is
 * executed.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class StateTransferringExecutor implements Executor, ApplicationListener<ContextRefreshedEvent> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(StateTransferringExecutor.class);

    /** */
    private final Executor delegate;

    /** */
    private volatile List<IStatefulService> services;

    /**
     * @param delegate
     */
    public StateTransferringExecutor(final Executor delegate) {
        this.delegate = delegate;

        services = ImmutableList.of();
    }

    @Override
    public void execute(@Nonnull final Runnable runnable) {
        delegate.execute(wrap(runnable));
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final ApplicationContext context = event.getApplicationContext();

        services = ImmutableList.copyOf(context.getBeansOfType(IStatefulService.class).values());
    }

    protected void clearServices() {
        this.services = ImmutableList.of();
    }

    protected ITransferableState getState() {
        return new CompositeTransferableState(
                services.stream().map(IStatefulService.TO_STATE).collect(Collectors.toList()));
    }

    protected Runnable wrap(final Runnable runnable) {
        return new StateTransferringRunnable(runnable, getState());
    }

    protected static class StateTransferringRunnable implements Runnable {

        private final Runnable delegate;

        private final ITransferableState state;

        public StateTransferringRunnable(final Runnable delegate, final ITransferableState state) {
            this.delegate = delegate;
            this.state = state;
        }

        @Override
        public void run() {
            try {
                state.apply();
                try {
                    delegate.run();
                } finally {
                    state.remove();
                }
            } catch (final RuntimeException e) {
                LOGGER.error("Error while processing asynchronous task", e);
                throw e;
            } catch (final Error e) {
                LOGGER.error("Error while processing asynchronous task", e);
                throw e;
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StateTransferringRunnable)) {
                return false;
            }

            final StateTransferringRunnable that = (StateTransferringRunnable) o;

            return delegate.equals(that.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }
}
