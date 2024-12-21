/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import com.pmi.tpd.api.util.Assert;

/**
 * Delegate a provided {@link DataSource}. this class allows a lazy reference on a datasource. This can be useful to
 * connect a health check to a service having a datasource not yet initialized during the application startup.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class DelegatingProviderDataSource implements DataSource {

    /** datasource provider. */
    private final Provider<DataSource> targetDataSource;

    /**
     * Create a new {@link DelegatingProviderDataSource}.
     *
     * @param targetDataSource
     *                         the target DataSource
     */
    @Inject
    public DelegatingProviderDataSource(@Nonnull final Provider<DataSource> targetDataSource) {
        Assert.checkNotNull(targetDataSource, "targetDataSource");
        this.targetDataSource = targetDataSource;
    }

    /**
     * Gets the associate {@link DataSource}.
     *
     * @return Returns the associate {@link DataSource}.
     */
    public DataSource getTargetDataSource() {
        return this.targetDataSource.get();

    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException {
        return getTargetDataSource().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getTargetDataSource().getConnection(username, password);
    }

    /** {@inheritDoc} */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getTargetDataSource().getLogWriter();
    }

    /** {@inheritDoc} */
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        getTargetDataSource().setLogWriter(out);
    }

    /** {@inheritDoc} */
    @Override
    public int getLoginTimeout() throws SQLException {
        return getTargetDataSource().getLoginTimeout();
    }

    /** {@inheritDoc} */
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        getTargetDataSource().setLoginTimeout(seconds);
    }

    // ---------------------------------------------------------------------
    // Implementation of JDBC 4.0's Wrapper interface
    // ---------------------------------------------------------------------
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return getTargetDataSource().unwrap(iface);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this) || getTargetDataSource().isWrapperFor(iface);
    }

    // ---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    // ---------------------------------------------------------------------
    /** {@inheritDoc} */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

}
