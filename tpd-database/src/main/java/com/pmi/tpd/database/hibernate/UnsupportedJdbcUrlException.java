package com.pmi.tpd.database.hibernate;

/**
 * Thrown when a custom jTDS JDBC URL is detected during startup.
 * <p>
 * jTDS is no longer used to connect to SQL Server installations, as it is not compatible with recent releases of
 * Hibernate. It has been replaced by Microsoft's own JDBC driver. However, some customers use customised JDBC URLs to
 * leverage SQL Server features like domain authentication. The URL parameters for those features differ between the
 * drivers and cannot (easily) be automatically "fixed". This exception is thrown instead, and then handled specially by
 * Event to display a (hopefully) useful error message.
 *
 * @since 1.3
 * @author Christophe Friederich
 */
public class UnsupportedJdbcUrlException extends UnsupportedOperationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create new instance of {@link UnsupportedJdbcUrlException} with the specified detail message.
     *
     * @param message
     *            a detail message.
     */
    public UnsupportedJdbcUrlException(final String message) {
        super(message);
    }
}
