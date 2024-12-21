package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.security.access.AccessDeniedException;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * To avoid bleeding of data for anonymous users treat {@link AccessDeniedException} as unauthorized.
 *
 * @since 2.0
 */
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

    /** */
    private final I18nService i18nService;

    /**
     * @param authenticationContext
     * @param i18nService
     */
    public AccessDeniedExceptionMapper(final I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public Response toResponse(final AccessDeniedException exception) {
        return ResponseFactory.status(Response.Status.UNAUTHORIZED)
                // TODO This needs to be the same as ExceptionRewriteAdvice
                .entity(new ErrorMessage(i18nService.getMessage("app.service.accessdenied")))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();

    }
}
