package com.pmi.tpd.euceg.backend.core;

import javax.annotation.Nonnull;

import org.eu.ceg.AppResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.euceg.api.BackendNotStartedException;
import com.pmi.tpd.euceg.backend.core.spi.ISenderMessageHandler;

/**
 * @author christophe friederich
 * @since 2.5
 */
public interface IBackendManager extends IStartable, IShutdown, SmartLifecycle, DisposableBean {

    /**
     * @return
     */
    ISenderMessageHandler<AppResponse> getMessageHandler();

    /**
     * @param handler
     */
    void setMessageHandler(@Nonnull final ISenderMessageHandler<AppResponse> handler);

    /**
     * @param autoStartup
     */
    void setAutoStartup(final boolean autoStartup);

    /**
     * Checks whether the server is running and the transport connection if {@code checkOnlyServer} is {@code false}.
     *
     * @param checkOnlyServer
     *                        checks the server run only
     * @throws Exception
     *                   if the healthcheck has failed.
     */
    void healthCheck(boolean checkOnlyServer) throws Exception;

    /**
     * @param healthCheckUrl
     * @throws Exception
     */
    void healthCheck(@Nonnull String healthCheckUrl) throws Exception;

    /**
     * Send a payload with specific messageId.
     *
     * @param messageId
     *                  the message identifier to use.
     * @param payload
     *                  the payload to use.
     * @throws BackendNotStartedException
     *                                    if the server is unavailable or service is not enable.
     */
    void sendPayload(@Nonnull String messageId, @Nonnull Object payload) throws BackendNotStartedException;

}