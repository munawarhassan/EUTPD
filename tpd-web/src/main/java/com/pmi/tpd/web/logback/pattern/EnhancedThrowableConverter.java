package com.pmi.tpd.web.logback.pattern;

import java.sql.SQLException;
import java.util.Iterator;

import ch.qos.logback.classic.pattern.PublicExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * A customisation of the standard Logback {@code ExtendedThrowableProxyConverter} that traverses and logs chained
 * {@link SQLException SQLExceptions}.
 * <p/>
 * TODO further enhance this class to omit certain frames related to filter chains, AOP proxies and other stack entries
 * which only serve to make debugging issues harder rather than easier.
 */
public class EnhancedThrowableConverter extends PublicExtendedThrowableProxyConverter {

    /**
     * Whether redundant frames should be trimmed from stacktraces (enabled by default).
     */
    private static final String PROP_FULL_STACK_TRACES = "app.log.fullStackTraces";

    /** */
    private final boolean logFullStackTrace;

    /**
     *
     */
    public EnhancedThrowableConverter() {
        this.logFullStackTrace = Boolean.getBoolean(PROP_FULL_STACK_TRACES);
    }

    /**
     * Similar to {@link ch.qos.logback.classic.pattern.ThrowableProxyConverter#throwableProxyToString} but with special
     * handling of proxies wrapping {@link SQLException}s.
     *
     * @param tp
     *            a logback throwable proxy
     * @return a pretty printed string representation of the supplied proxy
     */
    @Override
    protected String throwableProxyToString(final IThrowableProxy tp) {
        final StringBuilder buf = new StringBuilder(32);
        IThrowableProxy currentThrowable = logFullStackTrace ? tp : new FilteredThrowableProxy(tp);
        while (currentThrowable != null) {
            final SQLException sqlException = extractChainedSQLException(currentThrowable);
            if (sqlException != null) {
                subjoinSqlException(buf, sqlException);
            } else {
                subjoinThrowableProxy(buf, currentThrowable);
            }

            currentThrowable = currentThrowable.getCause();
        }
        return buf.toString();
    }

    /**
     * Mimics {@link #subjoinThrowableProxy}, but iterates over. {@link SQLException} appending any chained exceptions
     * to the StringBuilder
     *
     * @param buf
     *            the log message so far
     * @param sqlException
     *            a (potentially chained) SQLException
     */
    void subjoinSqlException(final StringBuilder buf, final SQLException sqlException) {
        for (final Iterator<Throwable> iterator = sqlException.iterator(); iterator.hasNext();) {
            final Throwable chainedException = iterator.next();
            final ThrowableProxy tp = new ThrowableProxy(chainedException);
            subjoinThrowableProxy(buf, tp);
            if (iterator.hasNext()) {
                buf.append("Next Exception: ");
            }
        }
    }

    /**
     * @param tp
     *            a logback throwable proxy
     * @return an {@link SQLException} if the supplied proxy wraps a chained SQLException, otherwise null
     */
    private SQLException extractChainedSQLException(final IThrowableProxy tp) {
        final Throwable t = unwrapException(tp);
        if (t instanceof SQLException) {
            final SQLException sqlException = (SQLException) t;
            // only return an SQLException if it is chained. Solitary
            // SQLExceptions will be treated normally.
            if (sqlException.getNextException() != null) {
                return sqlException;
            }
        }
        return null;
    }

    private Throwable unwrapException(final IThrowableProxy tp) {
        if (tp instanceof FilteredThrowableProxy) {
            return unwrapException(((FilteredThrowableProxy) tp).getThrowableProxy());
        } else if (tp instanceof ThrowableProxy) {
            return ((ThrowableProxy) tp).getThrowable();
        } else {
            addWarn(String.format("Unexpected implementation of %s: %s",
                IThrowableProxy.class.getSimpleName(),
                tp.getClass().getName()));
        }

        return null;
    }

}
