package com.pmi.tpd.web.core.request;

import javax.annotation.Nonnull;

/**
 * Interface for providing request details to {@link IRequestManager}. These details are used to set up logging and
 * profiling.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRequestInfoProvider extends IRequestMetadata {

    /**
     * @return the underlying 'raw' request object
     */
    @Nonnull
    Object getRawRequest();

    /**
     * @return the underlying 'raw' response object
     */
    @Nonnull
    Object getRawResponse();

}
