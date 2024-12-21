package com.pmi.tpd.api;

import org.apache.commons.lang3.StringUtils;

/**
 * Constants related to how Logback should be configured.
 */
public final class LoggingConstants {

    /**
     * The complete logback format string that is used for the access logs.
     */
    public static final String ACCESS_LOG_FORMAT;

    /**
     * The format property for including MDC variables in logback logs. If the value of the MDC variable is empty, a '-'
     * character will be included in the log.
     */
    public static final String FORMAT_DEFAULTING_MDC = "dX";

    /**
     * The format property for including request details.
     *
     * @see com.pmi.tpd.web.logback.pattern.RequestContextConverter
     */
    public static final String FORMAT_REQUEST_CONTEXT = "request";

    /**
     * The format property for including nicely formatted throwable stack frames.
     *
     * @see com.pmi.tpd.web.logback.pattern.EnhancedThrowableConverter
     */
    public static final String FORMAT_ENHANCED_THROWABLE = "eThrowable";

    /**
     * The complete Logback format string which should be used for all messages.
     * <p/>
     * Note: We have to include an explicit throwable handler in the format or Logback will automatically add one at the
     * tail of the converter list and the one it adds won't be the one we want to use.
     */
    public static final String LOG_FORMAT = "%date %-5level [%thread] %" + FORMAT_REQUEST_CONTEXT + " %logger{36} %m%n%"
            + FORMAT_ENHANCED_THROWABLE;

    /**
     * The name of the slf4j Logger that is used for the access log.
     */
    public static final String LOGGER_ACCESS = ApplicationConstants.APPLICATION_KEY + ".access-log";

    /**
     * The name of the slf4j Logger that is used for the audit log.
     */
    public static final String LOGGER_AUDIT = ApplicationConstants.APPLICATION_KEY + ".audit-log";

    /**
     * The MDC property for binding/retrieving whether we're logging a request as it comes in, or when we've completed
     * handling the request. Used for the access log only.
     */
    public static final String MDC_ACCESSLOG_IN_OUT = "a-in-out";

    /**
     * The MDC property for binding/retrieving the protocol for the connection.
     * <p/>
     * Currently expected possible values are: ssh, http and https.
     */
    public static final String MDC_PROTOCOL = "a-protocol";

    /**
     * The MDC property for binding/retrieving the remote address for the connection.
     * <p/>
     * The value bound for this property is expected to be proxy-aware. If the request has been proxied, the IP
     * addresses for both the proxy and the remote host should be included in the value, comma-delimited.
     */
    public static final String MDC_REMOTE_ADDRESS = "a-remote-address";

    /**
     * The MDC property for binding/retrieving further details about the request. Used for access logging only.
     */
    public static final String MDC_REQUEST_DETAILS = "a-request-details";

    /**
     * The MDC property for binding/retrieving the request ID.
     * <p/>
     * The value bound for this property is expected to be unique to a given request <i>and</i> to be constant for the
     * duration of the request. The uniqueness window for requests should be at least one day, since the log files for
     * the system rotate nightly.
     */
    public static final String MDC_REQUEST_ID = "a-request-id";

    /**
     * The MDC variable for binding/retrieving labels associated with the request. Labels are logged as part of the
     * access log and provide more information about a request in the access logs for supportability or analysis.
     */
    public static final String MDC_REQUEST_LABELS = "a-request-labels";

    /**
     * The MDC property for binding/retrieving the requested URL.
     * <p/>
     * The value bound for this property is expected to omit scheme, host, port and context path information, as well as
     * any query parameters (since we can't easily know what might be there that shouldn't be logged).
     */
    public static final String MDC_REQUEST_ACTION = "a-request-action";

    /**
     * The MDC property for binding/retrieving the time it took to service the request.
     * <p/>
     * The value bound for this property is expected to be expressed in milliseconds.
     */
    public static final String MDC_REQUEST_TIME = "a-request-time";

    /**
     * The MDC property for binding/retrieving the session ID.
     * <p/>
     * The value bound for this property is expected to be unique across the system <i>and</i> to be constant for the
     * duration of a given user session. The uniqueness window for sessions should ideally but longer than a day, but
     * even daily uniqueness should be sufficient with log file rotation.
     */
    public static final String MDC_SESSION_ID = "a-session-id";

    /**
     * The MDC property for binding/retrieving the username.
     */
    public static final String MDC_USERNAME = "a-username";

    /**
     * The complete logback format string that is used for the profile logs.
     */
    public static final String PROFILE_LOG_FORMAT;

    static {
        ACCESS_LOG_FORMAT = StringUtils.join(
            new String[] { dX(MDC_REMOTE_ADDRESS), dX(MDC_PROTOCOL),
                    "%X{" + MDC_ACCESSLOG_IN_OUT + "}" + dX(MDC_REQUEST_ID), dX(MDC_USERNAME), "%date",
                    dX(MDC_REQUEST_ACTION), dX(MDC_REQUEST_DETAILS), dX(MDC_REQUEST_LABELS), dX(MDC_REQUEST_TIME),
                    dX(MDC_SESSION_ID), "%n" },
            " | ");

        PROFILE_LOG_FORMAT = StringUtils.join(
            new String[] { "%date", "%thread", dX(MDC_REQUEST_ID), dX(MDC_USERNAME), dX(MDC_SESSION_ID) + "%n%m%n" },
            " | ");
    }

    /**
     * Helper method for building up a Logback format string.
     *
     * @param variable
     *            the MDC variable name
     * @return the string "%dX{variable}"
     */
    private static String dX(final String variable) {
        return "%dX{" + variable + "}";
    }

    private LoggingConstants() {
        throw new UnsupportedOperationException("LoggingConstants shouldn't be instantiated even by Reflection!");
    }
}
