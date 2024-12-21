package com.pmi.tpd.euceg.backend.core.spi;

import java.util.Set;

import javax.annotation.Nonnull;

public interface IPendingMessageProvider {

    /**
     * Gets all pending messageIds independently of submitters.
     *
     * @return Return a new list of {@link String} representing a messageId.
     */
    @Nonnull
    Set<String> getPendingMessageIds();

    /**
     * Gets indicating whether this instance is owner of this message
     * 
     * @param messageId
     *                  message to check
     * @return Returns {@code true} whether this instance is owner of this message otherwise {@code false}.
     */
    boolean isOwner(@Nonnull String messageId);
}
