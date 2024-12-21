package com.pmi.tpd.web.websocket;

import java.security.Principal;
import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.pmi.tpd.WebsocketConfig;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.ProgressEvent;
import com.pmi.tpd.api.lifecycle.notification.NotificationEvent;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.UpdatedSubmissionEvent;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

import lombok.Builder;
import lombok.Data;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Controller
@RequestMapping("/")
public class WebSocketController {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    /** */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    /** */
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Default constructor.
     *
     * @param messagingTemplate
     *                          operations for sending messages to destionation supporting for Simple Messaging
     *                          Protocols (like STOMP)
     * @param eventPublisher
     *                          the event publisher.
     */
    @Inject
    public WebSocketController(@Nonnull final SimpMessageSendingOperations messagingTemplate,
            @Nonnull final IEventPublisher eventPublisher) {
        this.messagingTemplate = Assert.checkNotNull(messagingTemplate, "messagingTemplate");
    }

    /**
     * Send the updated current activity.
     *
     * @param activity
     *                            the current activity of user
     * @param stompHeaderAccessor
     *                            STOMP accessor.
     * @return Returns Upadate {@link Activity}
     */
    @MessageMapping("/activity")
    @SendTo("/topic/activity")
    public Activity sendActivity(@Payload final ActivityState activityState,
        final StompHeaderAccessor stompHeaderAccessor) {
        final Principal principal = stompHeaderAccessor.getUser();
        final String username = principal != null ? principal.getName() : "anonymous";
        final Activity activity = Activity.builder()
                .page(activityState.getUrl())
                .userLogin(username)
                .sessionId(stompHeaderAccessor.getSessionId())
                .ipAddress(stompHeaderAccessor.getSessionAttributes().get(WebsocketConfig.IP_ADDRESS).toString())
                .time(dateTimeFormatter.print(Calendar.getInstance().getTimeInMillis()))
                .build();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending user tracking data {}", activity);
        }
        return activity;
    }

    /**
     * Handle on {@link SessionDisconnectEvent}.
     *
     * @param event
     *              an event raised when the session of a WebSocket client using a Simple Messaging Protocol (e.g.
     *              STOMP) as the WebSocket sub-protocol is closed.
     */
    @org.springframework.context.event.EventListener
    public void onSessionDisconnectEvent(final SessionDisconnectEvent event) {
        final Activity activity = Activity.builder().sessionId(event.getSessionId()).page("logout").build();
        messagingTemplate.convertAndSend("/topic/activity", activity);
    }

    /**
     * Handle on {@link UpdatedSubmissionEvent}.
     *
     * @param event
     *              an event raised when submission has changed.
     */
    @EventListener
    public void onSubmissionUpdated(final UpdatedSubmissionEvent event) {
        final ISubmissionEntity submissionEntity = event.getSubmission();
        messagingTemplate.convertAndSend("/topic/submissions",
            new SubmissionMessage(submissionEntity.getId(), submissionEntity.getProgress(),
                    submissionEntity.getSubmissionStatus(), submissionEntity.getPirStatus()));

    }

    /**
     * Handle on {@link NotificationEvent}.
     *
     * @param event
     *              an event raised when a task need notify the front end.
     */
    @EventListener
    public void onNotification(final NotificationEvent event) {
        messagingTemplate.convertAndSend("/topic/notification", event.getNotification());

    }

    /**
     * Handle on {@link ProgressEvent}.
     *
     * @param event
     *              an event raised when the progression of schedule task has changed.
     */
    @EventListener
    public void onProgress(final ProgressEvent event) {
        messagingTemplate.convertAndSend("/topic/progress", event.getProgress());
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class SubmissionMessage {

        /** */
        private final Long submissionId;

        /** */
        private final float progress;

        /** */
        private final SubmissionStatus submissionStatus;

        private final ProductPirStatus pirStatus;

        /** */
        private final boolean cancelable;

        /** */
        private final boolean exportable;

        /**
         * Default constructor.
         *
         * @param submissionId
         *                         submission identifier
         * @param progress
         *                         the progession %
         * @param submissionStatus
         *                         the current submission status
         */
        public SubmissionMessage(final Long submissionId, final float progress, final SubmissionStatus submissionStatus,
                final ProductPirStatus pirStatus) {
            super();
            this.submissionId = submissionId;
            this.progress = progress;
            this.submissionStatus = submissionStatus;
            this.cancelable = submissionStatus.cancelable();
            this.exportable = submissionStatus.exportable();
            this.pirStatus = pirStatus;
        }

        /**
         * @return Returns a {@link String} representing the submission identifier.
         */
        public Long getSubmissionId() {
            return submissionId;
        }

        /**
         * @return Returns a {@link Float} representing the progress of submission in percentage.
         */
        public float getProgress() {
            return progress;
        }

        /**
         * @return Returns the current status of submission
         */
        public SubmissionStatus getSubmissionStatus() {
            return submissionStatus;
        }

        /**
         * @return Returns the current PIR status
         */
        public ProductPirStatus getPirStatus() {
            return pirStatus;
        }

        /**
         * @return Returns {@code true} if the submission is cancelable, otherwise {@code false}.
         */
        public boolean isCancelable() {
            return cancelable;
        }

        /**
         * @return Returns {@code true} if the submission is exportable, otherwise {@code false}.
         */
        public boolean isExportable() {
            return exportable;
        }
    }

    /**
     * DTO for storing a user's activity.
     */
    @Data
    @Builder
    public static class Activity {

        /** */
        private String sessionId;

        /** */
        private String userLogin;

        /** */
        private String ipAddress;

        /** */
        private String page;

        /** */
        private String time;

    }

    @Data
    public static class ActivityState {

        private String name;

        private int id;

        private String url;
    }

}
