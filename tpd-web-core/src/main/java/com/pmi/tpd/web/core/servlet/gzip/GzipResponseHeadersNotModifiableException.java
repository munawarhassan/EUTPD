package com.pmi.tpd.web.core.servlet.gzip;

import javax.servlet.ServletException;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class GzipResponseHeadersNotModifiableException extends ServletException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            a {@code String} specifying the text of the exception message
     */
    public GzipResponseHeadersNotModifiableException(final String message) {
        super(message);
    }
}
