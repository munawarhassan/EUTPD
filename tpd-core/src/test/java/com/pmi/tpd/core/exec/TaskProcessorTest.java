package com.pmi.tpd.core.exec;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.exec.IAggregator;
import com.pmi.tpd.api.exec.ICallableTask;
import com.pmi.tpd.scheduler.exec.support.SimpleTaskProcessor;
import com.pmi.tpd.testing.junit5.TestCase;

public class TaskProcessorTest extends TestCase {

    @Test
    public void validWrongCorePoolSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleTaskProcessor(0);
        });
    }

    @Test
    public void runAndWaitWithTaskFailed() throws InterruptedException, ExecutionException {
        assertThrows(ExecutionException.class, () -> {
            final SimpleTaskProcessor taskProcessor = new SimpleTaskProcessor();
            try {
                final List<String> expected = ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
                final List<ICallableTask<String>> tasks = Lists.newArrayList();
                for (final String result : expected) {
                    tasks.add(() -> {
                        if ("4".equals(result)) {
                            throw new RuntimeException("failed");
                        }
                        return result;
                    });
                }
                taskProcessor.runAndWait(tasks);
            } finally {
                taskProcessor.shutdown();
            }
        });
    }

    @Test
    public void runAndWaitWithoutExternalAggregator() throws InterruptedException, ExecutionException {
        final SimpleTaskProcessor taskProcessor = new SimpleTaskProcessor();
        try {
            final List<String> expected = ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            final List<ICallableTask<String>> tasks = Lists.newArrayList();
            for (final String result : expected) {
                tasks.add(() -> result);
            }
            final List<String> results = taskProcessor.runAndWait(tasks);
            MatcherAssert.assertThat(expected.toArray(), Matchers.arrayContainingInAnyOrder(results.toArray()));
        } finally {
            taskProcessor.shutdown();
        }
    }

    @Test
    public void runAndWaitWithAggregator() throws InterruptedException, ExecutionException {
        final SimpleTaskProcessor taskProcessor = new SimpleTaskProcessor();
        try {
            final List<String> expected = ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            final List<ICallableTask<String>> tasks = Lists.newArrayList();
            for (final String result : expected) {
                tasks.add(() -> result);
            }
            final List<String> results = Lists.newCopyOnWriteArrayList();
            final IAggregator<String> aggregator = aggregatable -> results.add(aggregatable);
            taskProcessor.runAndWait(tasks, aggregator);
            MatcherAssert.assertThat(expected.toArray(), Matchers.arrayContainingInAnyOrder(results.toArray()));
        } finally {
            taskProcessor.shutdown();
        }
    }

    @Test
    public void runAndWaitWithNullResult() throws InterruptedException, ExecutionException {
        final SimpleTaskProcessor taskProcessor = new SimpleTaskProcessor();
        try {
            final List<String> expected = Lists.newArrayList("1", "2", "3", null, "5", "6", "7", null, "9", "10");
            final List<ICallableTask<String>> tasks = Lists.newArrayList();
            for (final String result : expected) {
                tasks.add(() -> result);
            }
            final List<String> results = taskProcessor.runAndWait(tasks);
            MatcherAssert.assertThat(expected.toArray(), Matchers.arrayContainingInAnyOrder(results.toArray()));
        } finally {
            taskProcessor.shutdown();
        }
    }
}
