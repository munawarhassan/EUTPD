/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.scheduler.exec.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.exec.IAggregator;
import com.pmi.tpd.api.exec.ICallableTask;
import com.pmi.tpd.api.scheduler.ITaskProcessor;
import com.pmi.tpd.api.util.Assert;

/**
 * Simple {@link ITaskProcessor} implementation.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class SimpleTaskProcessor implements ITaskProcessor {

    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTaskProcessor.class);

    /** the default prefix to use for the names of newly created threads. */
    private static final String THREAD_NAME_PREFIX = "task-processor-";

    /** the associated task executor. */
    private final ThreadPoolExecutor taskExecutor;

    /** the maximum time (seconds) to wait before shutdown. */
    private final int shutdownTimeout = 5;

    /** the default core thread pool size. */
    private static final int DEFAULT_CORE_POOL_SIZE = 10;

    /**
     * Create a new {@link SimpleTaskProcessor} instance with default value.
     */
    public SimpleTaskProcessor() {
        this(DEFAULT_CORE_POOL_SIZE);
    }

    /**
     * Create a new {@link SimpleTaskProcessor} instance.
     *
     * @param corePoolSize
     *                     core pool size (Default is {@code 10}).
     */
    public SimpleTaskProcessor(final int corePoolSize) {
        this.taskExecutor = createTaskExecutor(corePoolSize, THREAD_NAME_PREFIX);
    }

    /**
     * Create a new {@link SimpleTaskProcessor} instance.
     *
     * @param corePoolSize
     *                         core pool size (default is 10).
     * @param threadNamePrefix
     *                         the prefix to use for the names of newly created threads (default is
     *                         {@code "task-processor-"}).
     */
    public SimpleTaskProcessor(final int corePoolSize, @Nullable final String threadNamePrefix) {
        this.taskExecutor = createTaskExecutor(corePoolSize, threadNamePrefix);
    }

    /**
     * Create a new {@link SimpleTaskProcessor} instance.
     *
     * @param taskExecutor
     *                     task executor to use (can <b>not</b> be {@code null}).
     */
    public SimpleTaskProcessor(@Nonnull final ThreadPoolExecutor taskExecutor) {
        this.taskExecutor = Assert.checkNotNull(taskExecutor, "taskExecutor");
    }

    /** {@inheritDoc} */
    @Override
    public <T> List<T> runAndWait(final List<ICallableTask<T>> tasks) throws InterruptedException, ExecutionException {
        final List<T> results = Lists.newCopyOnWriteArrayList();
        final IAggregator<T> aggregator = result -> results.add(result);
        runAndWait(tasks, aggregator);
        return results;
    }

    /** {@inheritDoc} */
    @Override
    public <T> void runAndWait(@Nonnull final List<ICallableTask<T>> tasks, @Nullable final IAggregator<T> aggregator)
            throws InterruptedException, ExecutionException {
        Assert.checkNotNull(tasks, "tasks");
        waiting(run(tasks), aggregator);
    }

    /** {@inheritDoc} */
    @Override
    @PreDestroy
    public void shutdown() {
        if (this.taskExecutor != null) {
            this.taskExecutor.shutdown();
            try {
                this.taskExecutor.awaitTermination(shutdownTimeout, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Create a new {@link ThreadPoolExecutor} instance.
     *
     * @param corePoolSize
     *                         core pool size (can <b>not</b> lesser than {@code 1}).
     * @param threadNamePrefix
     *                         the prefix to use for the names of newly created threads (default is
     *                         {@code "task-processor-"}).
     * @return Returns new instance of {@link ThreadPoolExecutor}.
     */
    @Nonnull
    protected ThreadPoolExecutor createTaskExecutor(final int corePoolSize, @Nullable final String threadNamePrefix) {
        Assert.isTrue(corePoolSize > 0, "The core pool size for the taskExecutor must be greater than 0.");

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(Strings.isNullOrEmpty(threadNamePrefix) ? THREAD_NAME_PREFIX : threadNamePrefix);
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }

    /**
     * Executes the given tasks.
     *
     * @param tasks
     *              tasks to execute (can <b>not</b> be {@code null}).
     * @return Returns the list of {@code FutureTask wrapped tasks}.
     * @param <T>
     *            the result type of a {@link ICallableTask task}.
     */
    @Nonnull
    protected <T> List<FutureTask<T>> run(@Nonnull final List<ICallableTask<T>> tasks) {
        Assert.checkNotNull(tasks, "tasks");
        final List<FutureTask<T>> list = new ArrayList<FutureTask<T>>(tasks.size());
        for (final ICallableTask<T> task : tasks) {
            final FutureTask<T> futureTask = new FutureTask<T>(task);
            list.add(futureTask);
            taskExecutor.execute(futureTask);
        }
        return list;
    }

    /**
     * Blocks until all tasks have completed execution.
     *
     * @param tasks
     *                   list of tasks to listen (can <b>not</b> be {@code null}).
     * @param aggregator
     *                   used to aggregate returned values (can be {@code null}).
     * @throws InterruptedException
     *                              if the current thread was interrupted while waiting.
     * @throws ExecutionException
     *                              if the computation threw an exception.
     * @param <T>
     *            the result type of a {@link ICallableTask task}.
     */
    protected <T> void waiting(@Nonnull final List<FutureTask<T>> tasks, @Nullable final IAggregator<T> aggregator)
            throws InterruptedException, ExecutionException {
        Assert.checkNotNull(tasks, "futureTasks");
        // Waiting
        int remain = 0;
        while (!tasks.isEmpty()) {
            for (final Iterator<FutureTask<T>> iterator = tasks.iterator(); iterator.hasNext();) {
                final FutureTask<T> task = iterator.next();
                if (task.isDone()) {
                    final T returnedValue = task.get();
                    if (aggregator != null) {
                        aggregator.aggregate(returnedValue);
                    }
                    iterator.remove();
                }
            }
            if (LOGGER.isDebugEnabled()) {
                if (!tasks.isEmpty()) {
                    if (tasks.size() != remain) {
                        LOGGER.debug("Remain : " + tasks.size());
                        remain = tasks.size();
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {

            }
        }
    }
}
