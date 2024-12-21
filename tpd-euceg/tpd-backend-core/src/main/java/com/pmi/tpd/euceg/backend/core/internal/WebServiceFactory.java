package com.pmi.tpd.euceg.backend.core.internal;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.common.logging.Slf4jLogger;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.https.InsecureTrustManager;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.backend.core.BackendProperties;

public class WebServiceFactory {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceFactory.class);

    /** */
    private static final String PROTOCOL = "TLSv1.3";

    /** */
    private static final String[] CIPHER_SUITES = new String[] { "TLS_AES_128_GCM_SHA256" };

    static {
        System.setProperty("org.apache.cxf.Logger", Slf4jLogger.class.getName());
    }

    /**
     * 
     */
    private @Nonnull final IKeyManagerProvider keyManagerProvider;

    /**
     * 
     */
    private @Nonnull final BackendProperties backendProperties;

    public WebServiceFactory(@Nonnull final IKeyManagerProvider keyManagerProvider,
            @Nonnull final BackendProperties backendProperties) {
        this.keyManagerProvider = Assert.checkNotNull(keyManagerProvider, "keyManagerProvider");
        this.backendProperties = Assert.checkNotNull(backendProperties, "backendProperties");
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <I, R extends Service> I createInterface(final Class<I> interfacews, final Class<R> serviceClass) {
        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(interfacews);
        factory.setAddress(backendProperties.getWebServiceUrl());
        // final LoggingFeature loggingFeature = new LoggingFeature();
        // loggingFeature.setPrettyLogging(true);
        // loggingFeature.setVerbose(true);
        // loggingFeature.setLogMultipart(false);
        // factory.getFeatures().add(loggingFeature);
        final Map<String, Object> props = ImmutableMap.of("mtom-enabled", Boolean.TRUE);
        factory.setProperties(props);

        final Object client = factory.create();

        final HTTPConduit httpConduit = (HTTPConduit) ((Client) client).getConduit();

        if (httpConduit != null) {
            configureCerts(httpConduit, keyManagerProvider, backendProperties);
            if (!Strings.isNullOrEmpty(backendProperties.getUsername())) {
                final AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();
                authorizationPolicy.setUserName(backendProperties.getUsername());
                authorizationPolicy.setPassword(backendProperties.getPassword());
                authorizationPolicy.setAuthorizationType(backendProperties.getWsOptions().getAuthorizationType());

                httpConduit.setAuthorization(authorizationPolicy);
            }
            configHttpClientPolicy(httpConduit);
        }

        ((BindingProvider) client).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        return (I) client;
    }

    private static void configureCerts(@Nonnull final HTTPConduit httpConduit,
        @Nonnull final IKeyManagerProvider keyManagerProvider,
        @Nonnull final BackendProperties backendProperties) {
        if (keyManagerProvider == null || backendProperties.isTlsInsecure()) {
            configureTrustAllCerts(httpConduit);
            return;
        }
        try {
            final TLSClientParameters tlsClientParameters = new TLSClientParameters();
            tlsClientParameters.setSecureSocketProtocol(getDefaultProtocol());
            tlsClientParameters.setCipherSuites(getCipherSuites());

            final SSLContext sslContext = WebServiceFactory.configureTls(keyManagerProvider);
            final SSLSocketFactory ssf = sslContext.getSocketFactory();
            tlsClientParameters.setSSLSocketFactory(ssf);
            tlsClientParameters.setUseHttpsURLConnectionDefaultHostnameVerifier(true);

            httpConduit.setTlsClientParameters(tlsClientParameters);
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
    }

    private static void configureTrustAllCerts(final HTTPConduit httpConduit) {
        TLSClientParameters tslClientParameters = httpConduit.getTlsClientParameters();
        if (tslClientParameters == null) {
            tslClientParameters = new TLSClientParameters();
        }
        tslClientParameters.setDisableCNCheck(true);
        tslClientParameters.setTrustManagers(InsecureTrustManager.getNoOpX509TrustManagers());
        httpConduit.setTlsClientParameters(tslClientParameters);
    }

    private static void configHttpClientPolicy(final HTTPConduit http) {
        final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setAutoRedirect(true);
        httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        httpClientPolicy.setConnectionTimeout(60000);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setReceiveTimeout(60000);
        http.setClient(httpClientPolicy);
    }

    public static String getDefaultProtocol() {
        return PROTOCOL;
    }

    public static List<String> getCipherSuites() {
        return Arrays.asList(CIPHER_SUITES);
    }

    public static SSLContext configureTls(@Nullable final IKeyManagerProvider keyManagerProvider) {
        SSLContext sslContext = null;
        try {
            if (keyManagerProvider == null) {
                sslContext = configureTrustAll();;
            } else {
                sslContext = SSLContext.getInstance(getDefaultProtocol());
                sslContext.init(keyManagerProvider.getKeyManagers(), keyManagerProvider.getTrustManagers(), null);
            }
            return sslContext;
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private static SSLContext configureTrustAll() throws KeyManagementException, NoSuchAlgorithmException {
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, InsecureTrustManager.getNoOpX509TrustManagers(), new SecureRandom());
        return sslContext;
    }
}
