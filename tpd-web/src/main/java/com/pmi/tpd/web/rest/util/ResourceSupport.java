package com.pmi.tpd.web.rest.util;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.exception.DataStoreException;
import com.pmi.tpd.core.exception.NoSuchUserException;

public abstract class ResourceSupport {

    protected final I18nService i18nService;

    protected final Logger log;

    protected ResourceSupport(final I18nService i18nService) {
        this.i18nService = i18nService;
        log = LoggerFactory.getLogger(getClass());
    }

    /**
     * Throws a {@link NoSuchUserException} with the provided {@link ApplicationUser#getSlug() slug}.
     *
     * @param slug
     *            the slug for which no {@link ApplicationUser ApplicationUser} could be found
     * @return nothing; this method <i>always</i> throws a {@link NoSuchUserException}
     * @since 2.4
     */
    protected NoSuchUserException newNoSuchUserBySlugException(final String slug) {
        // This is a bit of a lie; no good options
        throw new NoSuchUserException(i18nService.createKeyedMessage("app.web.user.slug.notfound", slug), slug);
    }

    protected void rejectException(final Errors errors, final ServiceException e) {
        errors.reject(e.getMessageKey(), e.getLocalizedMessage());
        if (e instanceof DataStoreException) {
            log.error("Exception occurred", e);
        } else {
            log.debug("Exception occurred", e);
        }
    }

    protected void rejectException(final Errors errors, final ConstraintViolationException e) {
        for (final ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.rejectValue(violation.getPropertyPath().toString(),
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                violation.getMessage());
        }
    }
}