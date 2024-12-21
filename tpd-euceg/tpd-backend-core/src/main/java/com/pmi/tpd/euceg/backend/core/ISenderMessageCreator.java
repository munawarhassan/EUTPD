package com.pmi.tpd.euceg.backend.core;

import java.io.IOException;
import java.nio.file.Path;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author christophe friederich
 * @param <REQUEST>
 * @param <RESPONSE>
 */
public interface ISenderMessageCreator<REQUEST, RESPONSE> {

    @Nonnull
    DataSource createRequestPayload(@Nonnull REQUEST payload, @Nullable Path workingDirectory)
            throws IOException, BackendException;

    public default @Nonnull DataSource createRequestPayload(@Nonnull final REQUEST payload)
            throws IOException, BackendException {
        return createRequestPayload(payload, null);
    }

    @Nullable
    RESPONSE createPayloadResponse(@Nonnull DataSource source);

}
