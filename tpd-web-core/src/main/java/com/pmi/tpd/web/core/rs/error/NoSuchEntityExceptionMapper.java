package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * To avoid bleeding of data for anonymous users treat {@link NoSuchEntityException} as unauthorized.
 *
 * @since 2.0
 */
public class NoSuchEntityExceptionMapper implements ExceptionMapper<NoSuchEntityException> {

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final I18nService i18nService;

    /**
     * @param authenticationContext
     * @param i18nService
     */
    public NoSuchEntityExceptionMapper(final IAuthenticationContext authenticationContext,
            final I18nService i18nService) {
        this.authenticationContext = authenticationContext;
        this.i18nService = i18nService;
    }

    @Override
    public Response toResponse(final NoSuchEntityException exception) {
        if (!authenticationContext.isAuthenticated()) {
            return ResponseFactory.status(Response.Status.UNAUTHORIZED)
                    // TODO This needs to be the same as ExceptionRewriteAdvice
                    .entity(new ErrorMessage(i18nService.getMessage("app.service.accessdenied")))
                    .type(RestUtils.APPLICATION_JSON_UTF8)
                    .build();
        }
        // Fall through to the default handler
        return null;
    }
}
