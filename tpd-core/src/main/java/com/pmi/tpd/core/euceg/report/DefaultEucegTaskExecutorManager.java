package com.pmi.tpd.core.euceg.report;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.euceg.core.task.IEucegTaskFactory;
import com.pmi.tpd.euceg.core.task.ITrackingReportState;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;
import com.pmi.tpd.scheduler.exec.support.DefaultTaskMonitor;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

@Singleton
@Service
public class DefaultEucegTaskExecutorManager implements IEucegTaskExecutorManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEucegTaskExecutorManager.class);

    /** */
    @Nonnull
    private final I18nService i18nService;

    /** */
    @Nonnull
    private ExecutorService executorService;

    /** */
    @Nonnull
    private IEucegTaskFactory taskFactory;

    /** */
    @Nonnull
    private final ISecureTokenGenerator tokenGenerator;

    /** */
    @Nonnull
    private final IClusterService clusterService;

    /** */
    private final IRequestManager requestManager;

    private final ConcurrentMap<String, ITaskMonitor> runTasks = new ConcurrentHashMap<>(16);

    @Inject
    public DefaultEucegTaskExecutorManager(final @Nonnull ExecutorService executorService,
            final @Nonnull IEucegTaskFactory taskFactory, final @Nonnull I18nService i18nService,
            final ISecureTokenGenerator tokenGenerator, final IClusterService clusterService,
            final IRequestManager requestManager) {
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.executorService = Assert.checkNotNull(executorService, "executorService");
        this.taskFactory = Assert.checkNotNull(taskFactory, "taskFactory");
        this.tokenGenerator = Assert.checkNotNull(tokenGenerator, "tokenGenerator");
        this.clusterService = Assert.checkNotNull(clusterService, "clusterService");
        this.requestManager = Assert.checkNotNull(requestManager, "requestManager");
    }

    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    @Nonnull
    public ITaskMonitor trackingReport(final @Nonnull ITrackingReportState state) {
        return submitTask(taskFactory.trackingReportTask(state));
    }

    @Override
    public Optional<ITaskMonitor> getTaskMonitor(final String id) {
        return Optional.ofNullable(this.runTasks.get(id));
    }

    @Override
    public void cancelTask(@Nonnull final String cancelToken) {
        getTaskMonitor(cancelToken).ifPresent(task -> {
            if (TaskState.RUNNING.equals(task.getState())) {
                task.cancel(cancelToken, 15, TimeUnit.SECONDS);
            }
        });
    }

    private ITaskMonitor submitTask(final @Nonnull IRunnableTask task) {

        final IRequestContext requestContext = requestManager.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("Euceg task can only be started in the context of a user request");
        }

        final String cancelToken = tokenGenerator.generateToken();

        final DefaultTaskMonitor runningTask = new DefaultTaskMonitor(task, cancelToken, clusterService.getNodeId(),
                requestContext.getSessionId(), cancelToken, i18nService);
        runTasks.put(cancelToken, runningTask);
        runningTask.registerCallback(() -> {
            runTasks.remove(cancelToken);
        });
        runningTask.submitTo(executorService);
        return runningTask;

    }

}
