package com.pmi.tpd.core.security.provider.ldap;

import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.ldap.server.UnboundIdContainer;

/**
 * @author Luke Taylor
 */

public final class LdapServerIntegration implements InitializingBean, DisposableBean, ApplicationContextAware {

    private final String ldifFile;

    private ApplicationContext context;

    private UnboundIdContainer server;

    private Integer serverPort;

    public LdapServerIntegration(final String ldifFile) {
        this.ldifFile = ldifFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startServer();
    }

    @Override
    public void destroy() throws Exception {
        stopServer();
    }

    public void startServer() throws Exception {
        server = new UnboundIdContainer("dc=company,dc=com", ldifFile);
        server.setApplicationContext(context);
        final int port = getAvailablePort();
        server.setPort(port);
        server.afterPropertiesSet();
        serverPort = port;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

    }

    public void stopServer() throws Exception {
        serverPort = null;
        if (server != null) {
            server.stop();
        }
    }

    public int getServerPort() {
        if (serverPort == null) {
            throw new IllegalStateException("The ApacheDSContainer is not currently running");
        }
        return serverPort;
    }

    private static int getAvailablePort() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (final IOException e) {
                }
            }
        }
    }
}