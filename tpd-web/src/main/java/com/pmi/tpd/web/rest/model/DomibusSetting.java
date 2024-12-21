package com.pmi.tpd.web.rest.model;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.BackendProperties.ConnectionType;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 */
@Getter
@Builder(toBuilder = true)
@ToString
@Jacksonized
public class DomibusSetting {

    private boolean enable;

    private ConnectionType connectionType;

    /** */
    private String url;

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private boolean tlsInsecure;

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
    private String partyIdType;

    /** */
    private String fromPartyId;

    /** */
    private String toPartyId;

    /** */
    private String keyPairAlias;

    /** */
    private String trustedCertificateAlias;

    /** */
    @Builder.Default
    private SettingJmsOption jmsOptions = SettingJmsOption.builder().build();

    /** */
    @Builder.Default
    private SettingWsOption wsOptions = SettingWsOption.builder().build();

    /**
     * Create and initialise a new instance of {@link MailSetting} with given application properties.
     *
     * @param props
     *            a application properties
     * @return Returns new initialised instance of {@link MailSetting}.
     */
    @Nonnull
    public static DomibusSetting create(@Nonnull final IApplicationProperties applicationProperties) {
        final BackendProperties configuration = applicationProperties.getConfiguration(BackendProperties.class);
        return DomibusSetting.builder()
                .enable(configuration.isEnable())
                .connectionType(configuration.getConnectionType())
                .url(configuration.getUrl())
                .username(configuration.getUsername())
                .password(configuration.getPassword())
                .tlsInsecure(configuration.isTlsInsecure())
                .action(configuration.getAction())
                .service(configuration.getService())
                .serviceType(configuration.getServiceType())
                .originalSender(configuration.getOriginalSender())
                .finalRecipient(configuration.getFinalRecipient())
                .partyIdType(configuration.getPartyIdType())
                .fromPartyId(configuration.getFromPartyId())
                .toPartyId(configuration.getToPartyId())
                .keyPairAlias(configuration.getKeyPairAlias())
                .trustedCertificateAlias(configuration.getTrustedCertificateAlias())
                .jmsOptions(SettingJmsOption.builder()
                        .url(configuration.getJmsOptions().getUrl())
                        .receiveTimeout(configuration.getJmsOptions().getReceiveTimeout())
                        .concurrency(configuration.getJmsOptions().getConcurrency())
                        .username(configuration.getJmsOptions().getUsername())
                        .password(configuration.getJmsOptions().getPassword())
                        .build())
                .wsOptions(SettingWsOption.builder()
                        .authorizationType(configuration.getWsOptions().getAuthorizationType())
                        .pendingInterval(configuration.getWsOptions().getPendingInterval())
                        .build())
                .build();

    }

    /**
     * @param applicationProperties
     */
    public void save(final IApplicationProperties applicationProperties) {
        final BackendProperties configuration = applicationProperties.getConfiguration(BackendProperties.class);
        applicationProperties.storeConfiguration(configuration.toBuilder()
                .enable(enable)
                .connectionType(connectionType)
                .url(url)
                .username(username)
                .password(password)
                .tlsInsecure(tlsInsecure)
                .action(action)
                .service(service)
                .serviceType(serviceType)
                .originalSender(originalSender)
                .finalRecipient(finalRecipient)
                .partyIdType(partyIdType)
                .fromPartyId(fromPartyId)
                .toPartyId(toPartyId)
                .keyPairAlias(keyPairAlias)
                .trustedCertificateAlias(trustedCertificateAlias)
                .jmsOptions(configuration.getJmsOptions()
                        .toBuilder()
                        .url(jmsOptions.getUrl())
                        .receiveTimeout(jmsOptions.getReceiveTimeout())
                        .concurrency(jmsOptions.getConcurrency())
                        .username(jmsOptions.getUsername())
                        .password(jmsOptions.getPassword())
                        .build())
                .wsOptions(configuration.getWsOptions()
                        .toBuilder()
                        .authorizationType(wsOptions.getAuthorizationType())
                        .pendingInterval(wsOptions.getPendingInterval())
                        .build())
                .build());
    }

    @Getter
    @Builder(toBuilder = true)
    @ToString
    @Jacksonized
    public static class SettingJmsOption {

        /** */
        private String url;

        /** the timeout to use for receive calls (in seconds). */
        private long receiveTimeout;

        /**
         * Specify concurrency limits via a "lower-upper" String, e.g. "5-10", or a simple upper limit String, e.g. "10"
         * (the lower limit will be 1 in this case).
         */
        private String concurrency;

        /** */
        private String username;

        /** */
        private String password;

    }

    @Getter
    @Builder(toBuilder = true)
    @ToString
    @Jacksonized
    public static class SettingWsOption {

        /** */
        private String authorizationType;

        /** The interval in second that backend service should poll pending message in Domibus. */
        private long pendingInterval;

    }

}