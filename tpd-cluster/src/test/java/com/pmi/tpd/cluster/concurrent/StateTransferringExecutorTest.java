package com.pmi.tpd.cluster.concurrent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class StateTransferringExecutorTest extends MockitoTestCase {

    @Mock
    private IStatefulService statefulService;

    private final List<String> s = Collections.synchronizedList(new LinkedList<String>());

    private ExecutorService executor;

    private StateTransferringExecutor stateTransferringExecutor;

    @BeforeEach
    public void setUp() throws Exception {
        executor = newExecutor();
        when(statefulService.getState()).thenReturn(STATE);
        stateTransferringExecutor = new StateTransferringExecutor(executor);
        final ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansOfType(IStatefulService.class))
                .thenReturn(ImmutableMap.of("service", statefulService));
        when(event.getApplicationContext()).thenReturn(applicationContext);

        stateTransferringExecutor.onApplicationEvent(event);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = null;
    }

    @Test
    public void testExecute() throws Exception {
        stateTransferringExecutor.execute(NOOP);
        executor.shutdown();
        executor.awaitTermination(30L, TimeUnit.SECONDS);

        assertArrayEquals(new String[] { "apply", "run", "remove" }, s.toArray());
    }

    private final Runnable NOOP = () -> s.add("run");

    private final ITransferableState STATE = new ITransferableState() {

        @Override
        public void apply() {
            s.add("apply");
        }

        @Override
        public void remove() {
            s.add("remove");
        }
    };

    private static ExecutorService newExecutor() {
        return Executors.newFixedThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, this.getClass().getSimpleName());
            }
        });
    }

}
