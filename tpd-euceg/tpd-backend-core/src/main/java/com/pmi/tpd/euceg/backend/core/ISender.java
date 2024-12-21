package com.pmi.tpd.euceg.backend.core;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;

/**
 * @param <REQUEST>
 */
public interface ISender<REQUEST> extends IStartable, IShutdown, SmartLifecycle, DisposableBean {

    /**
     * @param autoStartup
     */
    void setAutoStartup(boolean autoStartup);

    /**
     * @throws Exception
     */
    void healthCheck() throws Exception;

    /**
     * @param messageId
     * @param payload
     * @param workingDirectory
     * @throws IOException
     * @throws BackendException
     */
    void send(@Nonnull final String messageId, @Nonnull final REQUEST payload, @Nullable Path workingDirectory)
            throws IOException, BackendException;

    /**
     * @param messageId
     * @param payload
     * @throws IOException
     * @throws BackendException
     */
    void send(@Nonnull final String messageId, @Nonnull final REQUEST payload) throws IOException, BackendException;

}
