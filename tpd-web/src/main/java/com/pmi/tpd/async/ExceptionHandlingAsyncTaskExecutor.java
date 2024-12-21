package com.pmi.tpd.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor {

    private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);

    private final AsyncTaskExecutor executor;

    public ExceptionHandlingAsyncTaskExecutor(final AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(final Runnable task) {
        executor.execute(task);
    }

    @Override
    public void execute(final Runnable task, final long startTimeout) {
        executor.execute(createWrappedRunnable(task), startTimeout);
    }

    private <T> Callable<T> createCallable(final Callable<T> task) {
        return new Callable<T>() {

            @Override
            public T call() throws Exception {
                try {
                    return task.call();
                } catch (final Exception e) {
                    handle(e);
                    throw e;
                }
            }
        };
    }

    private Runnable createWrappedRunnable(final Runnable task) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    task.run();
                } catch (final Exception e) {
                    handle(e);
                }
            }
        };
    }

    protected void handle(final Exception e) {
        log.error("Caught async exception", e);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return executor.submit(createWrappedRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return executor.submit(createCallable(task));
    }

}
