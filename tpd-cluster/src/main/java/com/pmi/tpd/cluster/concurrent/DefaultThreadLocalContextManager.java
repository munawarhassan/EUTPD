package com.pmi.tpd.cluster.concurrent;

import static java.util.Arrays.asList;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;

/**
 * Implementation of SAL's {@link IThreadLocalContextManager} that uses {@link ITransferableState} and
 * {@link IStatefulService} to pass thread-local state between threads.
 */
public class DefaultThreadLocalContextManager
        implements IThreadLocalContextManager, ApplicationListener<ContextRefreshedEvent> {

    /** */
    private final Set<IStatefulService> statefulServices = new CopyOnWriteArraySet<>();

    /** */
    private final ThreadLocal<CompositeTransferableState> threadState = new ThreadLocal<>();

    @Override
    public void clearThreadLocalContext() {
        final CompositeTransferableState currentThreadState = threadState.get();
        if (currentThreadState != null) {
            currentThreadState.remove();
        }
        threadState.remove();
    }

    @Override
    public CompositeTransferableState getThreadLocalContext() {
        return new CompositeTransferableState(Collections2.transform(statefulServices, IStatefulService.TO_STATE));
    }

    /**
     * We have to postpone injection of the {@link IStatefulService} instances because a number of basic platform
     * services that are provided by application depend on {@link IThreadLocalContextManager}. If we were to
     * constructor-inject or setter-inject the stateful services, we'd run into circular dependencies on creation.
     * Specifically:
     * <ul>
     * <li>{@link com.pmi.tpd.core.event.spi.TransactionAwareEventPublisher} (indirectly) depends on
     * IThreadLocalContextManager</li>
     * <li>{@link com.pmi.tpd.core.request.IRequestManager} is a {@link IStatefulService} AND depends on
     * {@link com.pmi.tpd.api.event.publisher.IEventPublisher}</li>
     * </ul>
     **/
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        statefulServices.addAll(event.getApplicationContext().getBeansOfType(IStatefulService.class).values());
    }

    @Override
    public void setThreadLocalContext(final Object context) {
        if (context instanceof CompositeTransferableState) {
            final CompositeTransferableState currentThreadState = (CompositeTransferableState) context;
            this.threadState.set(currentThreadState);
            currentThreadState.apply();
        }
    }

    @VisibleForTesting
    void setStatefulServices(final IStatefulService... services) {
        this.statefulServices.addAll(asList(services));
    }
}
