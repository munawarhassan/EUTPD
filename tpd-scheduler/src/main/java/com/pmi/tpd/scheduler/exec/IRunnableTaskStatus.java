package com.pmi.tpd.scheduler.exec;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exec.IProgressReporter;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Describes the status of a {@link IRunnableTask}.
 */
public interface IRunnableTaskStatus extends IProgressReporter {

    /**
     * Retrieves the token which may be used to cancel the in-progress maintenance.
     *
     * @return the cancellation token for the in-progress maintenance
     */
    @Nonnull
    String getCancelToken();

    /**
     * @return the task's unique id
     **/
    @Nonnull
    String getId();

    /**
     * @return the node ID of the cluster member that is performing the maintenance
     */
    @Nonnull
    UUID getOwnerNodeId();

    /**
     * @return the ID of session that started the maintenance
     */
    @Nullable
    String getOwnerSessionId();

    /**
     * @return the timestamp for when the maintenance task started
     * @since 3.5
     */
    @Nonnull
    Date getStartTime();

    /**
     * @return the task's current state
     */
    @Nonnull
    TaskState getState();

    /**
     * Retrieves a flag indicating whether the specified {@link IRequestContext request} was made by the owner of the
     * in-progress maintenance (where maintenance is considered to be "owned" by the user who started it).
     * <p>
     * Implementation note: Since the database may be inaccessible, owner detection is done using session IDs, rather
     * than user IDs. In other words, the request will be considered as belonging to the maintenance owner if it was
     * made with the same session that started the process.
     *
     * @param requestContext
     *                       the current request context
     * @return {@code true} if the current request originates from the same session which started the in-progress
     *         maintenance; otherwise, {@code false}
     */
    boolean isOwner(@Nonnull IRequestContext requestContext);
}
