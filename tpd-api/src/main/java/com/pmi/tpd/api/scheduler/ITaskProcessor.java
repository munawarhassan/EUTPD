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
package com.pmi.tpd.api.scheduler;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exec.IAggregator;
import com.pmi.tpd.api.exec.ICallableTask;

/**
 * Task processor interface.
 *
 * @author Christophe Friederich
 */
public interface ITaskProcessor {

    /**
     * Run a list of task.
     *
     * @param task
     *            list of tasks to execute (can <b>not</b> be {@code null}).
     * @return Returns of unordered list of the result of a {@link ICallableTask task}.
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting.
     * @throws ExecutionException
     *             if the computation threw an exception
     * @param <T>
     *            the result type of a {@link ICallableTask task}.
     */
    <T> List<T> runAndWait(@Nonnull List<ICallableTask<T>> task) throws InterruptedException, ExecutionException;

    /**
     * Run a list of tasks. Result can be retrieved by using an aggregator.
     *
     * @param tasks
     *            list of tasks to execute (can <b>not</b> be {@code null}).
     * @param aggregator
     *            aggregator allowing to capture value.
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting.
     * @throws ExecutionException
     *             if the computation threw an exception
     * @param <T>
     *            the result type of a {@link ICallableTask task}.
     */
    <T> void runAndWait(List<ICallableTask<T>> tasks, IAggregator<T> aggregator)
            throws InterruptedException, ExecutionException;

    /**
     * Perform a shutdown on the ThreadPoolExecutor.
     *
     * @see java.util.concurrent.ThreadPoolExecutor#shutdown()
     */
    void shutdown();
}
