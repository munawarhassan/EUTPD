package com.pmi.tpd.euceg.backend.core;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.config.annotation.DurationUnit;
import com.pmi.tpd.api.config.annotation.NoPersistent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the spring configuration of backend service of domibus.
 *
 * @author Christophe Friederich
 */
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@ConfigurationProperties("app.domibus")
public class BackendProperties {

    public static final String WEB_SERVICE_PATH = "/services/backend";

    public static final String PLUGIN_WEB_SERVICE_PATH = "/services/wsplugin";

    public static final String REST_PATH = "/rest";

    public static final String HEALTCHECK_PATH = REST_PATH + "/application/name";

    public enum ConnectionType {
        Jms,
        Ws,
        WsPlugin
    }

    private boolean enable;

    private ConnectionType connectionType;

    /** */
    private String url;

    /** */
    @Builder.Default
    private boolean tlsInsecure = false;

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private String action;

    /** */
    private String service;

    /** */
    private String serviceType;

    /** */
    private String originalSender;

    /** */
    private String finalRecipient;

    /** */
    private String fromPartyId;

    /** */
    private String toPartyId;

    /** */
    private String partyIdType;

    /** */
    private String keyPairAlias;

    /** */
    private String trustedCertificateAlias;

    /**
     * @return
     */
    public String getWebServiceUrl() {
        var servicePath = PLUGIN_WEB_SERVICE_PATH;
        switch (connectionType) {
            case Ws:
                servicePath = WEB_SERVICE_PATH;
                break;
            case WsPlugin:
                servicePath = PLUGIN_WEB_SERVICE_PATH;
            default:
                break;
        }
        return cleanupPath(url) + servicePath;
    }

    /**
     * @return
     */
    public String getRestUrl() {
        return cleanupPath(url) + REST_PATH;
    }

    /**
     * @return
     */
    public String getHealthCheckUrl() {
        return getHealthCheckUrl(url);
    }

    /**
     * @param url
     * @return
     */
    public static String getHealthCheckUrl(@Nonnull final String url) {
        return cleanupPath(url) + HEALTCHECK_PATH;
    }

    private static String cleanupPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.length() > 0) {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    @Builder.Default
    private Option options = Option.builder().build();

    /** */
    @Builder.Default
    private JmsOption jmsOptions = JmsOption.builder().build();

    /** */
    @Builder.Default
    private WsOption wsOptions = WsOption.builder().build();

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @ToString
    public static class Option {

        /** The interval in second to check payloads to send. */
        @Builder.Default()
        @NoPersistent
        @DurationUnit(ChronoUnit.SECONDS)
        private long sendInterval = 5;

        /** Controls the number of sent submission to treat. */
        @Builder.Default()
        @NoPersistent
        private int sendBatchSize = 10;

        /** The minimum time interval (in seconds) between runs. */
        @Builder.Default()
        @NoPersistent
        @DurationUnit(ChronoUnit.SECONDS)
        private long deferredInterval = 10;

        /** The minimum time interval (in seconds) between runs. */
        @Builder.Default()
        @NoPersistent
        private int deferredBatchSize = 5;

        /** The interval in hour to verify if exist old pending submissions to cancel */
        @Builder.Default()
        @NoPersistent
        @DurationUnit(ChronoUnit.HOURS)
        private Duration cancelInterval = Duration.ofHours(24);

        /** The number of days await before cancel pending submission. */
        @Builder.Default()
        @NoPersistent
        @DurationUnit(ChronoUnit.DAYS)
        private Duration awaitBeforeCancel = Duration.ofDays(30);

    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @ToString
    public static class JmsOption {

        /** */
        private String url;

        /** */
        @Builder.Default()
        private long receiveTimeout = 3;

        /** */
        @Builder.Default()
        private String concurrency = "1-1";

        /** */
        private String username;

        /** */
        private String password;

    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @ToString
    public static class WsOption {

        /** */
        @Builder.Default()
        private String authorizationType = "BASIC";

        /**
         * The interval in second that backend service should poll pending message in Domibus.
         */
        @Builder.Default()
        private long pendingInterval = 5L;

    }
}
