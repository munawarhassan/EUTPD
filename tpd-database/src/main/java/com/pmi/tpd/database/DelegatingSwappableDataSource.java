package com.pmi.tpd.database;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.springframework.core.InfrastructureProxy;

import com.google.common.io.Closeables;
import com.pmi.tpd.api.util.IDisposable;

/**
 * An implementation of {@link SwappableDataSource} which delegates all methods to another {@code DataSource} instance.
 * That instance may be swapped out at runtime, allowing the database to be "updated" by tasks such as database
 * migration without requiring the new data source to be re-injected throughout the system.
 * <p>
 * Additionally, this class implements {@code Closeable} and adds a Spring {@code PreDestroy} {@link #close()} method
 * which is used to close the delegate {@code DataSource} when the Spring context is shutdown.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DelegatingSwappableDataSource implements Closeable, ISwappableDataSource {

    /** */
    private volatile DataSource delegate;

    public DelegatingSwappableDataSource(final DataSource delegate) {
        this.delegate = delegate;
    }

    public DelegatingSwappableDataSource(final Provider<DataSource> delegate) {
        this.delegate = delegate.get();
    }

    /**
     * Closes the delegate {@code DataSource}, if it implements {@code Closeable} or {@link IDisposable}. Otherwise,
     * does nothing.
     * <p>
     * This method is a workaround for an issue where Spring does not call annotated {@code PreDestroy} methods on
     * objects which are created using {@code BeanFactory.getObject(String, Object...)}.
     */
    @Override
    @PreDestroy
    public void close() {
        if (delegate instanceof IDisposable) {
            ((IDisposable) delegate).dispose();
        } else if (delegate instanceof Closeable) {
            try {
                Closeables.close((Closeable) delegate, false);
            } catch (final IOException e) {
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    public DataSource getWrappedObject() {
        // cache the delegate field to a local variable to guard against the delegate changing between the instanceof
        // check and the rest of the ternary
        final DataSource delegate = this.delegate;

        return delegate instanceof InfrastructureProxy
                ? (DataSource) ((InfrastructureProxy) delegate).getWrappedObject() : delegate;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Nonnull
    @Override
    public DataSource swap(@Nonnull final DataSource target) {
        final DataSource old = delegate;
        delegate = target;

        return old;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }
}
