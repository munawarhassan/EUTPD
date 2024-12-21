package com.pmi.tpd.web.core.rs.error;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class DefaultUnhandledExceptionMapperHelper implements IUnhandledExceptionMapperHelper {

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final I18nService i18nService;

    /** */
    private final IRequestManager requestManager;

    @Inject
    public DefaultUnhandledExceptionMapperHelper(final IAuthenticationContext authenticationContext,
            final I18nService i18nService, final IRequestManager requestManager) {
        this.authenticationContext = authenticationContext;
        this.i18nService = i18nService;
        this.requestManager = requestManager;
    }

    @Nonnull
    @Override
    public IAuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    @Nonnull
    @Override
    public I18nService getI18nService() {
        return i18nService;
    }

    @Nonnull
    @Override
    public IRequestManager getRequestManager() {
        return requestManager;
    }
}
