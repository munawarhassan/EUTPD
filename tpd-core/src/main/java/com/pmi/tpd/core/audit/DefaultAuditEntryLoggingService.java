package com.pmi.tpd.core.audit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.api.LoggingConstants;
import com.pmi.tpd.api.audit.IAuditEntry;

/**
 * Handles logging {@link AuditEntry} entries in a common (fixed) format
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class DefaultAuditEntryLoggingService implements IAuditEntryLoggingService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingConstants.LOGGER_AUDIT);

    // The log is a " | " delimited string with static value positions, if adding a new value append it to the end to
    // keep any parsers happy
    // Format: sourceIpAddress | action | user | timestamp | target | details
    private static final String LOG_MESSAGE_FORMAT = "%1$s | %2$s | %3$s | %4$d | %5$s | %6$s | %7$s | %8$s";

    /** */
    private final int maxDetails;

    /** */
    private final ObjectMapper mapper;

    @Inject
    public DefaultAuditEntryLoggingService(final ObjectMapper mapper, final int maxDetails) {
        this.maxDetails = maxDetails;
        this.mapper = mapper;
    }

    @Override
    public void log(@Nonnull final IAuditEntry entry) {
        checkNotNull(entry);
        final String message = getLogMessage(entry);
        LOGGER.info(message);
    }

    @VisibleForTesting
    String getLogMessage(final IAuditEntry event) {
        try {
            return String.format(LOG_MESSAGE_FORMAT,
                convertStringsToLogFormat(event.getSourceIpAddress()),
                convertStringsToLogFormat(event.getAction()),
                convertStringsToLogFormat(event.getUser() == null ? null : event.getUser().getName()),
                event.getTimestamp().getTime(),
                convertStringsToLogFormat(event.getTarget()),
                truncate(convertStringsToLogFormat(convertToJsonString(event.getDetails()))),
                convertStringsToLogFormat(MDC.get(LoggingConstants.MDC_REQUEST_ID)),
                convertStringsToLogFormat(MDC.get(LoggingConstants.MDC_SESSION_ID)));
        } catch (IllegalArgumentException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String truncate(final String s) {
        return StringUtils.abbreviate(s, maxDetails);
    }

    private String convertStringsToLogFormat(final String input) {
        if (input == null) {
            return "-";
        } else {
            // escape end of line and pipe characters
            return input.replaceAll("[\r\n]+", "\\n").replaceAll(" \\| ", " PIPE_CHAR ");
        }
    }

    protected String convertToJsonString(final Object dataValues) throws IOException {
        return mapper.writeValueAsString(dataValues);
    }
}