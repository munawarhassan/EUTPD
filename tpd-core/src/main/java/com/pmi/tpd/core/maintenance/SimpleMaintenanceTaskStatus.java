package com.pmi.tpd.core.maintenance;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleMaintenanceTaskStatus implements IRunnableMaintenanceTaskStatus, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String cancelToken;

    /** */
    private final String id;

    /** */
    private final UUID memberId;

    /** */
    private final IProgress progress;

    /** */
    private final String sessionId;

    /** */
    private final long startTime;

    /** */
    private final TaskState state;

    /** */
    private final MaintenanceType type;

    public SimpleMaintenanceTaskStatus(@Nonnull final IRunnableMaintenanceTaskStatus status) {
        this.cancelToken = status.getCancelToken();
        this.id = status.getId();
        this.memberId = status.getOwnerNodeId();
        this.progress = new ProgressImpl(status.getProgress());
        this.sessionId = status.getOwnerSessionId();
        this.type = status.getType();
        this.startTime = status.getStartTime().getTime();
        this.state = status.getState();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Nonnull
    @Override
    public String getCancelToken() {
        return cancelToken;
    }

    @Nonnull
    @Override
    public UUID getOwnerNodeId() {
        return memberId;
    }

    @Override
    public String getOwnerSessionId() {
        return sessionId;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return progress;
    }

    @Nonnull
    @Override
    public Date getStartTime() {
        return new Date(startTime);
    }

    @Nonnull
    @Override
    public TaskState getState() {
        return state;
    }

    @Nonnull
    @Override
    public MaintenanceType getType() {
        return type;
    }

    @Override
    public boolean isOwner(@Nonnull final IRequestContext requestContext) {
        Assert.checkNotNull(requestContext, "requestContext");

        // IRequestContext.getSessionId() is @Nullable, so this call won't create a session if one doesn't already
        // exist. If the session doesn't exist it couldn't possibly be the owning session for the maintenance, so
        // the functionality here is as-desired.
        return sessionId != null && sessionId.equals(requestContext.getSessionId());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cancelToken", cancelToken)
                .add("id", id)
                .add("memberId", memberId)
                .add("progress", progress)
                .add("sessionId", sessionId)
                .add("startTime", startTime)
                .add("state", state)
                .add("type", type)
                .toString();
    }

}
