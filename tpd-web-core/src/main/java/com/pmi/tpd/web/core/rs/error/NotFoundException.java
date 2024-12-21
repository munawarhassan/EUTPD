package com.pmi.tpd.web.core.rs.error;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.annotation.Nullable;

import com.pmi.tpd.web.core.rs.support.ResponseFactory;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class NotFoundException extends ResourceException {

    private static final long serialVersionUID = 6374167568769913992L;

    public NotFoundException(final String message) {
        this(null, message);
    }

    /**
     * Construct new {@code NotFoundException} with error context and message.
     *
     * @param context
     *                error context
     * @param message
     *                error message
     */
    public NotFoundException(@Nullable final String context, @Nullable final String message) {
        super(ResponseFactory.status(NOT_FOUND).entity(new Errors(context, message)));
    }
}
