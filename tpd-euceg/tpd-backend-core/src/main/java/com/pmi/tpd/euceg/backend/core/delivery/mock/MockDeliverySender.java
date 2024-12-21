package com.pmi.tpd.euceg.backend.core.delivery.mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.ISender;
import com.pmi.tpd.euceg.backend.core.delivery.mock.MessageHandlerFactory.BaseMessageHandler;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;

public class MockDeliverySender implements ISender<Object> {

    /** */
    private TaskScheduler taskScheduler;

    private final MessageHandlerFactory handlerFactory;

    /** */
    private final List<BaseMessageHandler<?>> handlers = Lists.newArrayList();

    private boolean started = false;

    private boolean autoStartup = false;

    public MockDeliverySender(@Nonnull final IEventPublisher eventPublisher) {
        this(eventPublisher, null);
    }

    public MockDeliverySender(@Nullable final IEventPublisher eventPublisher,
            @Nullable final TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.handlerFactory = new MessageHandlerFactory(eventPublisher);
        if (this.taskScheduler == null) {
            this.taskScheduler = new ConcurrentTaskScheduler();
        }
    }

    @Override
    public void start() {

        started = true;
    }

    @Override
    public void shutdown() {
        handlers.forEach(BaseMessageHandler::cancel);
        started = false;
    }

    @Override
    public void stop() {
        shutdown();
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    @Override
    public void setAutoStartup(final boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    @Override
    public void healthCheck() throws Exception {
        // nothing
    }

    protected <T> BaseMessageHandler<T> createHandler(@Nonnull final String messageId, @Nonnull final T payload) {
        return handlerFactory.createHandler(messageId, payload);
    }

    @Override
    public void send(@Nonnull final String messageId, @Nonnull final Object payload)
            throws IOException, BackendException {
        this.send(messageId, payload, null);
    }

    @Override
    public void send(final @Nonnull String messageId,
        final @Nonnull Object payload,
        final @Nullable Path workingDirectory) throws IOException, BackendException {
        final BaseMessageHandler<?> handler = createHandler(messageId, payload);
        handler.schedule(taskScheduler);
        handler.publish(handler.createSubmitResponse());
        // publish send message
        handler.publish(MessageSent.builder().messageId(messageId).correlationId(messageId).build());

    }

}
