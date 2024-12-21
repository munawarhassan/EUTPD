package com.pmi.tpd.euceg.backend.core.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.io.CharStreams;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;

public class SimpleSenderMessageCreator implements ISenderMessageCreator<String, String> {

    @Override
    public @Nonnull DataSource createRequestPayload(final @Nonnull String payload,
        final @Nullable Path workingDirectory) throws IOException, BackendException {
        return new ByteArrayDataSource(payload.getBytes(), "plain/text");
    }

    @Override
    public String createPayloadResponse(final @Nonnull DataSource source) {
        try (Reader r = new InputStreamReader(source.getInputStream())) {
            return CharStreams.toString(r);
        } catch (final IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

}
