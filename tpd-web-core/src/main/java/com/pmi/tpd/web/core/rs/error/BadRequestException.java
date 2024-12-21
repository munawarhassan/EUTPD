package com.pmi.tpd.web.core.rs.error;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.web.core.rs.support.ResponseFactory;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class BadRequestException extends ResourceException {

    private static final long serialVersionUID = 9187161493815859599L;

    /**
     * @param message
     *                error message
     */
    public BadRequestException(final String message) {
        this(null, message);
    }

    /**
     * Construct new {@code BadRequestException} with error context and message.
     *
     * @param context
     *                error context
     * @param message
     *                error message
     */
    public BadRequestException(@Nullable final String context, @Nullable final String message) {
        super(ResponseFactory.status(BAD_REQUEST).entity(new Errors(context, message)));
    }

    /**
     * @param messages
     *                 error messages.
     */
    public BadRequestException(@Nonnull final Iterable<String> messages) {
        super(ResponseFactory.status(BAD_REQUEST).entity(asRestErrors(checkNotNull(messages, "messages"))));
    }

    private static Errors asRestErrors(final Iterable<String> messages) {
        final Errors.Builder errors = new Errors.Builder();
        for (final String message : messages) {
            errors.add(new ErrorMessage(message));
        }
        return errors.build();
    }

}
