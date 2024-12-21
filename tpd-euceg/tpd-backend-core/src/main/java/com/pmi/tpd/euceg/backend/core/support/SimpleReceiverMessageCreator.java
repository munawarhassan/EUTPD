package com.pmi.tpd.euceg.backend.core.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.io.CharStreams;
import com.pmi.tpd.euceg.backend.core.IReceiverMessageCreator;

public class SimpleReceiverMessageCreator implements IReceiverMessageCreator<String> {

    private ISenderMessageResolver<String, String> resolver;

    public SimpleReceiverMessageCreator() {
    }

    public SimpleReceiverMessageCreator(@Nullable final ISenderMessageResolver<String, String> resolver) {
        this.resolver = resolver;
    }

    @Override
    public String createIncommingPayload(final @Nonnull DataSource source) {
        try (Reader r = new InputStreamReader(source.getInputStream())) {
            return CharStreams.toString(r);
        } catch (final IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public @Nonnull DataSource createResponsePayload(final @Nonnull String incomingPayload,
        @Nonnull final String conversationId,
        final @Nullable Path workingDirectory) {
        String payload = "response";
        if (this.resolver != null) {
            payload = this.resolver.apply(incomingPayload);
        }
        return new ByteArrayDataSource(payload.getBytes(), "text/plain");
    }

}
