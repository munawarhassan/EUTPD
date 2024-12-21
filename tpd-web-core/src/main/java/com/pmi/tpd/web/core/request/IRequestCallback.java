package com.pmi.tpd.web.core.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.web.client.RequestCallback;

import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Callback provided to callers of {@link IRequestManager#doAsRequest(RequestCallback, IRequestInfoProvider)}. The
 * callback provides all information in the {@link IRequestInfoProvider} plus a generated requestId.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@FunctionalInterface
public interface IRequestCallback<T, E extends Exception> {

    /**
     * Callback method that is called by {@link IRequestManager#doAsRequest(RequestCallback, IRequestInfoProvider)}.
     *
     * @param requestContext
     *                       information about the current request, including a generated request id.
     * @return the value that should be returned by
     *         {@link IRequestManager#doAsRequest(RequestCallback, IRequestInfoProvider)}
     * @throws E
     */
    @Nullable
    T withRequest(@Nonnull IRequestContext requestContext) throws E;
}
