package com.pmi.tpd.euceg.backend.core.domibus.api;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Objects;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.ErrorLogResponse;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLogResponse;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.User;
import com.pmi.tpd.euceg.backend.core.internal.WebServiceFactory;

import lombok.Builder;
import lombok.Getter;

public class ClientRest implements IClientRest {

    private static final String SESSION_ID = "JSESSIONID";

    private static final String XSRF_TOKEN = "XSRF-TOKEN";

    private static final String X_XSRF_TOKEN = "X-XSRF-TOKEN";

    /** */
    private Client client;

    /** */
    private WebTarget webResource;

    /** */
    private final IApplicationProperties applicationProperties;

    @Nonnull
    private final I18nService i18nService;

    /** */
    private final IKeyManagerProvider keyManagerProvider;

    /** */
    private BackendProperties backendProperties;

    /** */
    private Level logLevel = Level.INFO;

    private boolean started = false;

    /** */
    private static ThreadLocal<Session> localSession = new ThreadLocal<>();

    public ClientRest(@Nonnull final IApplicationProperties applicationProperties, final I18nService i18nService) {
        this(applicationProperties, i18nService, null);
    }

    @Inject
    public ClientRest(@Nonnull final IApplicationProperties applicationProperties, final I18nService i18nService,
            @Nullable final IKeyManagerProvider keyManagerProvider) {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.keyManagerProvider = keyManagerProvider;
        this.i18nService = checkNotNull(i18nService, "i18nService");
    }

    @PostConstruct
    public void initialize() {
        final BackendProperties backendProperties = getBackendProperties();
        client = ClientBuilder.newBuilder()
                .sslContext(WebServiceFactory.configureTls(
                    backendProperties.isTlsInsecure() || keyManagerProvider == null ? null : keyManagerProvider))
                .hostnameVerifier(backendProperties.isTlsInsecure() ? (hostname, session) -> true : null)
                .register(JacksonFeature.class)
                .register(RequestClientReaderInterceptor.class)
                .build();
        // client.register(new CsrfProtectionFilter());
        final LoggingFeature loggingFeature = new LoggingFeature(
                java.util.logging.Logger.getLogger(ClientRest.class.getName()), logLevel, null, null);
        client.register(loggingFeature);
        webResource = client.target(backendProperties.getRestUrl());
        started = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (started) {
            return;
        }
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (!started) {
            return;
        }
        if (this.client != null) {
            this.client.close();
        }
        started = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogLevel(final @Nonnull Level logLevel) {
        this.logLevel = Assert.checkNotNull(logLevel, "logLevel");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackendProperties(final BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BackendProperties getBackendProperties() {
        if (this.backendProperties != null) {
            return this.backendProperties;
        }
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthenticated() {
        return localSession.get() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void healthCheck(@Nonnull final String url) throws Exception {
        final String healthCheckUrl = BackendProperties.getHealthCheckUrl(url);

        final Response response = client.target(healthCheckUrl).request(MediaType.TEXT_PLAIN).get();
        try {
            final int status = response.getStatus();
            final String content = response.readEntity(String.class);
            if (status != 200) {
                throw new Exception("Unexpected status code:" + status + ", message:" + content);
            }

            if (content == null || !content.toLowerCase().contains("domibus")) {
                throw new Exception("Unexpected content message:" + content + ", should contains 'domibus'.");
            }
        } finally {
            response.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageLogResponse getMessageLogs(@Nonnull final String conversationId, @Nonnull final Pageable pageable) {
        final var from = DateTime.now()
                .withZone(DateTimeZone.UTC)
                .withYear(0)
                .withDayOfMonth(1)
                .withMonthOfYear(1)
                .toString(ISODateTimeFormat.dateTime());
        final var to = DateTime.now().withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime());
        return executeAndRetry("GET",
            webResource.path("messagelog")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("pageSize", pageable.getPageSize())
                    .queryParam("orderBy", "received")
                    .queryParam("asc", false)
                    .queryParam("conversationId", conversationId)
                    .queryParam("messageType", "USER_MESSAGE")
                    .queryParam("isTestMessage", false)
                    // required, and don't know why
                    .queryParam("receivedTo", to)
                    .queryParam("receivedFrom", from)
                    // TODO use reflection on MessageLogResponse class to generate array of fields instead
                    .queryParam("fields",
                        "messageId",
                        "messageType",
                        "messageStatus",
                        "notificationStatus",
                        "mshRole",
                        "fromPartyId",
                        "toPartyId",
                        "originalSender",
                        "finalRecipient",
                        "refToMessageId")
                    .request(MediaType.APPLICATION_JSON),
            MessageLogResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorLogResponse getErrorLogs(@Nonnull final String messageId, @Nonnull final Pageable pageable) {
        return executeAndRetry("GET",
            webResource.path("errorlogs")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("pageSize", pageable.getPageSize())
                    .queryParam("orderBy", "timestamp")
                    .queryParam("asc", false)
                    .queryParam("messageInErrorId", messageId)
                    .request(MediaType.APPLICATION_JSON),
            ErrorLogResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response getCurrentPMode() {
        return executeAndRetry("GET",
            webResource.path("pmode/current").request(MediaType.APPLICATION_JSON),
            Response.class);
    }

    private void auth() {
        final String username = this.getBackendProperties().getUsername();
        final String password = this.getBackendProperties().getPassword();
        final Response resp = webResource.path("security/authentication")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(User.builder().username(username).password(password).build(),
                    MediaType.APPLICATION_JSON));
        final String sessionId = resp.getCookies().get(SESSION_ID).getValue();
        final String token = resp.getCookies().get(XSRF_TOKEN).getValue();

        localSession.set(Session.builder().sessionId(sessionId).token(token).build());
        resp.close();
    }

    private void logout() {
        try {
            if (isAuthenticated()) {
                addSession(webResource.path("security/authentication").request()).delete();
            }
        } finally {
            localSession.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T executeAndRetry(@Nonnull final String methodName,
        @Nonnull final Invocation.Builder builder,
        @Nonnull final Class<T> responseType) {

        Response response = null;
        try {
            if (!isAuthenticated()) {
                this.auth();
            }
            response = addSession(builder).build(methodName).invoke();
            if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                response.close();
                localSession.remove();
                this.auth();
                response = builder.build(methodName).invoke();
            }
            if (Response.class.equals(responseType)) {
                return (T) response;
            }
            return response.readEntity(responseType);
        } catch (final Throwable ex) {
            if (ex.getCause() instanceof ConnectException) {
                throw new EucegException(
                        i18nService.createKeyedMessage("app.service.euceg.backend.clientrest.connection.failed"),
                        ex.getCause());
            }
            throw ex;
        } finally {
            if (response != null) {
                response.close();
            }
            logout();
        }
    }

    private Invocation.Builder addSession(final Invocation.Builder builder) {
        final Session session = localSession.get();
        return builder.cookie(SESSION_ID, session.getSessionId())
                .cookie(XSRF_TOKEN, session.getToken())
                .header(X_XSRF_TOKEN, session.getToken());
    }

    @Getter
    @Builder
    private static class Session {

        private final String sessionId;

        private final String token;
    }

    @Provider
    public static class RequestClientReaderInterceptor implements ReaderInterceptor {

        @Override
        public Object aroundReadFrom(final ReaderInterceptorContext ctx) throws IOException, WebApplicationException {
            if (MediaType.APPLICATION_JSON_TYPE.equals(ctx.getMediaType())) {
                final InternalInputStream is = new InternalInputStream(ctx.getInputStream());
                ctx.setInputStream(is);
            }
            return ctx.proceed();
        }

    }

    /**
     * Skip the JSON prefix used to help prevent JSON Hijacking.
     *
     * @author christophe friederich
     */
    private static class InternalInputStream extends FilterInputStream {

        private volatile boolean skip = false;

        public InternalInputStream(final InputStream in) {
            super(in);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            Objects.checkFromIndexSize(off, len, b.length);
            if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) {
                return -1;
            }
            int count = 0;
            if (!skip) {
                while ((char) c != '{') {
                    c = read();
                    if (c == -1) {
                        return -1;
                    }
                    count++;
                    if (count > 7) {
                        break;
                    }
                }
                skip = true;
            }
            b[off] = (byte) c;
            int i = 1;
            try {
                for (; i < len; i++) {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    b[off + i] = (byte) c;
                }
            } catch (final IOException ee) {
            }
            return i;
        }
    }

}
