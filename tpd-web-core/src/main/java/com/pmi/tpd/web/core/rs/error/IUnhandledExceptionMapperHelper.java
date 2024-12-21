package com.pmi.tpd.web.core.rs.error;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * A simple wrapper around plugin dependencies for {@link UnhandledExceptionMapper} for protecting backwards
 * compatibility in plugins that need to extend it.
 *
 * @since 2.0
 */
public interface IUnhandledExceptionMapperHelper {

    /**
     * @return
     */
    @Nonnull
    IAuthenticationContext getAuthenticationContext();

    /**
     * @return
     */
    @Nonnull
    I18nService getI18nService();

    /**
     * @return the {@link IRequestManager}, to allow the exception mapper to retrieve information about the current
     *         request
     */
    @Nonnull
    IRequestManager getRequestManager();
}
