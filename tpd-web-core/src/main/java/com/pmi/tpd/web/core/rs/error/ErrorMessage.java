package com.pmi.tpd.web.core.rs.error;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.web.core.rs.annotation.JsonSurrogate;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@JsonSerialize
@JsonSurrogate(ServiceException.class)
public class ErrorMessage {

    /** */
    private final String context;

    /** */
    private final String message;

    /** */
    private final String exceptionName;

    /** */
    private final String throwable;

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param context
     *                a context of error.
     * @param message
     *                the message of error.
     */
    public ErrorMessage(final String context, final String message) {
        this(context, message, null, null);
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param context
     *                      a context of error.
     * @param message
     *                      the message of error.
     * @param exceptionName
     *                      the exception name associated to this error.
     */
    public ErrorMessage(final String context, final String message, final String exceptionName) {
        this(context, message, exceptionName, null);
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param context
     *                      a context of error.
     * @param message
     *                      the message of error.
     * @param exceptionName
     *                      the exception name associated to this error.
     * @param throwable
     *                      the stackstrace of associated error.
     */
    public ErrorMessage(final String context, final String message, final String exceptionName,
            final String throwable) {
        this.context = context;
        this.message = message;
        this.exceptionName = exceptionName;
        this.throwable = throwable;
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param message
     *                the message of error.
     */
    public ErrorMessage(final String message) {
        this(null, message, null, null);
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param e
     *          a service exception.
     */
    public ErrorMessage(final ServiceException e) {
        this(null, e.getLocalizedMessage(), e.getClass().getCanonicalName());
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param context
     *                a context of error.
     * @param e
     *                a service exception.
     */
    public ErrorMessage(final String context, final Exception e) {
        this(context, e.getLocalizedMessage(), e.getClass().getCanonicalName(), ExceptionUtils.getStackTrace(e));
    }

    /**
     * Create new instance of {@link ErrorMessage}.
     *
     * @param context
     *                a context of error.
     * @param message
     *                the message of error.
     * @param e
     *                a service exception.
     */
    public ErrorMessage(final String context, final String message, final Exception e) {
        this(context, message, e.getClass().getCanonicalName(), ExceptionUtils.getStackTrace(e));
    }

    /**
     * @return Returns a {@link String} representing the context of error.
     */
    @Nullable
    public String getContext() {
        return context;
    }

    /**
     * @return Returns a {@link String} representing the message of error.
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * @return Returns a {@link String} representing the exception name associated to this error.
     */
    @Nullable
    public String getExceptionName() {
        return exceptionName;
    }

    /**
     * @return Returns a {@link String} representing the stackstrace of exception associated to this error.
     */
    public String getThrowable() {
        return throwable;
    }

}