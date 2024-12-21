package com.pmi.tpd.web.logback.web;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.html.DefaultThrowableRenderer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.html.IThrowableRenderer;
import ch.qos.logback.core.read.CyclicBufferAppender;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class ViewLastLogProvider {

    /** */
    private final Logger logger = LoggerFactory.getLogger(ViewLastLogProvider.class);

    /** */
    private CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender;

    /** */
    private final IThrowableRenderer<ILoggingEvent> throwableRenderer = new DefaultThrowableRenderer();

    /**
     * Default Constructor.
     */
    public ViewLastLogProvider() {
        init();
    }

    /**
     * Initialize retrieving the cyclic buffer appender.
     */
    protected void init() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        initialize(lc);
    }

    void reacquireCBA() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) context.getLogger(Logger.ROOT_LOGGER_NAME)
                .getAppender(ApplicationConstants.Log.CYCLIC_BUFFER_APPENDER_NAME);
    }

    /**
     * @return Returns a new instance of {@link LogEvents} containing a list of last buffered log.
     */
    public LogEvents printLogs() {
        reacquireCBA();

        int count = -1;
        final StringBuilder buf = new StringBuilder();
        if (cyclicBufferAppender != null) {
            count = cyclicBufferAppender.getLength();
        }

        if (count <= 0) {
            return LogEvents.emtpy();
        } else {
            final List<LogEvent> events = Lists.newArrayListWithCapacity(count);
            for (int i = 0; i < count; i++) {
                buf.setLength(0);
                final LoggingEvent le = (LoggingEvent) cyclicBufferAppender.get(i);
                if (le.getThrowableProxy() != null) {
                    throwableRenderer.render(buf, le);
                }
                events.add(new LogEvent(le.getTimeStamp(), le.getLevel().toString(), le.getThreadName(),
                        le.getFormattedMessage(), buf.length() == 0 ? null : buf.toString()));
            }
            return new LogEvents(events);
        }
    }

    private void initialize(final LoggerContext context) {
        logger.debug("Initializing ViewLastLog Servlet");
        cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) context.getLogger(Logger.ROOT_LOGGER_NAME)
                .getAppender(ApplicationConstants.Log.CYCLIC_BUFFER_APPENDER_NAME);

    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonSerialize
    public static class LogEvents {

        /** */
        private static final LogEvents EMPTY = new LogEvents(Collections.emptyList());

        /** */
        private final List<LogEvent> events;

        /**
         * @return Returns generic empty instance.
         */
        public static LogEvents emtpy() {
            return EMPTY;
        }

        /**
         * @param events
         *            list of {@link LogEvent}.
         */
        public LogEvents(@JsonProperty(value = "events") final List<LogEvent> events) {
            super();
            this.events = events;
        }

        /**
         * @return Returns list of {@link LogEvent}.
         */
        public List<LogEvent> getEvents() {
            return events;
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonSerialize
    public static class LogEvent {

        /** */
        private final long timestamp;

        /** */
        private final String level;

        /** */
        private final String threadName;

        /** */
        private final String message;

        /** */
        private final String throwable;

        /**
         * Default constructor.
         *
         * @param timestamp
         *            the timestamp.
         * @param level
         *            the level of log.
         * @param threadName
         *            the thread name.
         * @param message
         *            the log message.
         * @param throwable
         *            a throwable.
         */
        @JsonCreator
        public LogEvent(@JsonProperty(value = "timestamp") final long timestamp,
                @JsonProperty(value = "level") final String level,
                @JsonProperty(value = "threadName") final String threadName,
                @JsonProperty(value = "message") final String message,
                @JsonProperty(value = "throwable") final String throwable) {
            super();
            this.timestamp = timestamp;
            this.message = message;
            this.level = level;
            this.threadName = threadName;
            this.throwable = throwable;
        }

        /**
         * @return Return the timestamp.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * @return Returns the thread name.
         */
        public String getThreadName() {
            return threadName;
        }

        /**
         * @return Returns the log level.
         */
        public String getLevel() {
            return level;
        }

        /**
         * @return Returns the message of log.
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return Returns a html representation of throwable exception.
         */
        public String getThrowable() {
            return throwable;
        }

    }

}