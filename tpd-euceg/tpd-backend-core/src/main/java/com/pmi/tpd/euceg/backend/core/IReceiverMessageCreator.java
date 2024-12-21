package com.pmi.tpd.euceg.backend.core;

import java.nio.file.Path;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IReceiverMessageCreator<R> {

  @Nullable
  R createIncommingPayload(@Nonnull DataSource source);

  @Nonnull
  DataSource createResponsePayload(@Nonnull R incomingPayload,
      @Nonnull String conversationId,
      @Nullable Path workingDirectory);
}
