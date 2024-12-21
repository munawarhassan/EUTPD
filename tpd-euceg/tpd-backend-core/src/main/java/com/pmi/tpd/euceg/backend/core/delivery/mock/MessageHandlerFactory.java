package com.pmi.tpd.euceg.backend.core.delivery.mock;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AppResponse;
import org.eu.ceg.Attachment;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.TobaccoProductSubmission;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import com.google.common.base.Strings;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.delivery.RejectedMessageException;
import com.pmi.tpd.euceg.backend.core.delivery.mock.MockDeliveryHelper.PayloadStatus;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;

public class MessageHandlerFactory {

    private final IEventPublisher eventPublisher;

    public MessageHandlerFactory() {
        this(null);
    }

    public MessageHandlerFactory(@Nullable final IEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @SuppressWarnings("unchecked")
    public <T> BaseMessageHandler<T> createHandler(@Nonnull final String messageId, @Nonnull T payload) {
        Assert.checkNotNull(messageId, "messageId");
        if (payload instanceof String) {
            payload = Eucegs.unmarshal((String) payload);
        }
        if (payload instanceof TobaccoProductSubmission) {
            return (BaseMessageHandler<T>) new TobaccoProductSubmissionHandler(eventPublisher, messageId,
                    (TobaccoProductSubmission) payload);
        } else if (payload instanceof EcigProductSubmission) {
            return (BaseMessageHandler<T>) new EcigProductSubmissionHandler(eventPublisher, messageId,
                    (EcigProductSubmission) payload);
        } else if (payload instanceof Attachment) {
            return (BaseMessageHandler<T>) new AttachmentHandler(eventPublisher, messageId, (Attachment) payload);
        } else if (payload instanceof SubmitterDetails) {
            return (BaseMessageHandler<T>) new SubmitterDetailsHandler(eventPublisher, messageId,
                    (SubmitterDetails) payload);
        }
        throw new IllegalArgumentException();
    }

    public static abstract class BaseMessageHandler<T> {

        protected volatile IEventPublisher eventPublisher;

        @Nonnull
        protected final T payload;

        @Nonnull
        protected final String messageId;

        private ScheduledFuture<?> task;

        private final TimeUnit timeUnit;

        private volatile long initialDelay;

        public BaseMessageHandler(@Nullable final IEventPublisher eventPublisher, @Nonnull final String messageId,
                @Nonnull final T payload, @Nullable final TimeUnit timeUnit) {
            this.eventPublisher = eventPublisher;
            this.payload = payload;
            this.messageId = Assert.checkNotNull(messageId, "messageId");
            this.timeUnit = timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS;
        }

        public void setInitialDelay(final long initialDelay) {
            this.initialDelay = this.timeUnit.toMillis(initialDelay);
        }

        public void cancel() {
            task.cancel(true);
        }

        @Nonnull
        public SubmitResponse createSubmitResponse() {
            return SubmitResponse.builder().correlationId(messageId).messageId(messageId).build();
        }

        public void schedule(final TaskScheduler taskScheduler) {
            taskScheduler.schedule(this::doRun, createTrigger());
        }

        @Nonnull
        public Trigger createTrigger() {
            return triggerContext -> {
                final Date lastExecution = triggerContext.lastScheduledExecutionTime();
                if (lastExecution == null) {
                    return new Date(triggerContext.getClock().millis() + initialDelay);
                }
                // execute once
                return null;
            };
        }

        public void publish(@Nonnull final IBackendMessage message) {
            if (eventPublisher != null) {
                eventPublisher.publish(new EventBackendReceived<>(this, message));
            }
        }

        @SuppressWarnings("null")
        protected void doRun() {
            try {
                createRespone().ifPresent(response -> publish(
                    Response.<AppResponse> builder().conversationId(messageId).response(response).build()));

            } catch (final RejectedMessageException e) {
                publish(e.getFailureMessage());
            }
        }

        public Optional<AppResponse> createRespone() throws RejectedMessageException {
            return Optional.empty();
        }

    }

    protected static class TobaccoProductSubmissionHandler extends BaseMessageHandler<TobaccoProductSubmission> {

        public TobaccoProductSubmissionHandler(final IEventPublisher eventPublisher, @Nonnull final String messageId,
                @Nonnull final TobaccoProductSubmission payload) {
            super(eventPublisher, messageId, payload, TimeUnit.SECONDS);
            setInitialDelay(5);
        }

        @Override
        public Optional<AppResponse> createRespone() throws RejectedMessageException {
            PayloadStatus status = null;
            AppResponse response = null;
            if (payload.getProduct() != null) {
                final String productNumber = MockDeliveryHelper.getProductNumber(payload.getProduct());
                status = MockDeliveryHelper.extractStatus(productNumber);
            } else {
                response = MockDeliveryHelper.responseError(messageId);
            }
            if (status != null && !SubmissionTypeEnum.CORRECTION.equals(payload.getSubmissionType().getValue())) {
                if (status.isError()) {
                    response = MockDeliveryHelper.errorTobaccoProductSubmissionResponse(payload, messageId);
                }
                if (TransmitStatus.REJECTED.equals(status.getStatus())) {
                    final MessageSendFailure failure = MessageSendFailure.builder()
                            .messageId(messageId)
                            .errorCode("ESM-120")
                            .status(status.getStatus())
                            .build();
                    throw new RejectedMessageException(failure);
                }
            }
            if (response == null) {
                response = MockDeliveryHelper.okTobaccoProductSubmissionResponse(payload, messageId);
            }
            return Optional.ofNullable(response);
        }
    }

    protected static class EcigProductSubmissionHandler extends BaseMessageHandler<EcigProductSubmission> {

        public EcigProductSubmissionHandler(final IEventPublisher eventPublisher, @Nonnull final String messageId,
                @Nonnull final EcigProductSubmission payload) {
            super(eventPublisher, messageId, payload, TimeUnit.SECONDS);
            setInitialDelay(5);
        }

        @Override
        public Optional<AppResponse> createRespone() throws RejectedMessageException {
            PayloadStatus status = null;
            AppResponse response = null;
            if (payload.getProduct() != null) {
                final String productNumber = MockDeliveryHelper.getProductNumber(payload.getProduct());
                status = MockDeliveryHelper.extractStatus(productNumber);
            } else {
                response = MockDeliveryHelper.responseError(messageId);
            }
            if (status != null) {
                if (status.isError()) {
                    response = MockDeliveryHelper.errorEcigProductSubmissionResponse(payload, messageId);
                }
                if (TransmitStatus.REJECTED.equals(status.getStatus())) {
                    final MessageSendFailure failure = MessageSendFailure.builder()
                            .messageId(messageId)
                            .errorCode("ESM-120")
                            .status(status.getStatus())
                            .build();
                    throw new RejectedMessageException(failure);
                }
            }
            if (response == null) {
                response = MockDeliveryHelper.okEcigProductSubmissionResponse(payload, messageId);
            }
            return Optional.ofNullable(response);
        }
    }

    protected static class AttachmentHandler extends BaseMessageHandler<Attachment> {

        public AttachmentHandler(final IEventPublisher eventPublisher, @Nonnull final String messageId,
                @Nonnull final Attachment payload) {
            super(eventPublisher, messageId, payload, TimeUnit.SECONDS);
            setInitialDelay(1);
        }

        @Override
        public Optional<AppResponse> createRespone() throws RejectedMessageException {
            if (!Strings.isNullOrEmpty(payload.getFilename()) && payload.getFilename().contains("error")) {
                return Optional.of(MockDeliveryHelper.rejectedAttachmentResponse(payload, messageId));
            } else if (!Strings.isNullOrEmpty(payload.getFilename()) && payload.getFilename().contains("pending")) {
                return Optional.empty();
            } else {

                return Optional.of(MockDeliveryHelper.okAttachmentResponse(payload, messageId));
            }
        }
    }

    protected static class SubmitterDetailsHandler extends BaseMessageHandler<SubmitterDetails> {

        public SubmitterDetailsHandler(final IEventPublisher eventPublisher, @Nonnull final String messageId,
                @Nonnull final SubmitterDetails payload) {
            super(eventPublisher, messageId, payload, TimeUnit.SECONDS);
            setInitialDelay(3);
        }

        @Override
        public Optional<AppResponse> createRespone() throws RejectedMessageException {
            return Optional.of(MockDeliveryHelper.okSubmitterDetailsResponse(payload, messageId));
        }
    }
}
