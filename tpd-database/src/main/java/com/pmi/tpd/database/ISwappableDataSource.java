package com.pmi.tpd.database;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.springframework.core.InfrastructureProxy;

/**
 * An extension to JDBC's standard {@code DataSource} which allows swapping out the instance to use when servicing
 * requests.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISwappableDataSource extends DataSource, InfrastructureProxy {

    /**
     * Overrides the return type of {@code InfrastructureProxy.getWrappedObject()} to be {@code DataSource} to simplify
     * callers.
     *
     * @return the wrapped {@code DataSource}
     */
    @Override
    DataSource getWrappedObject();

    /**
     * Swaps in provided target {@code DataSource} for processing method invocations, returning the old target.
     *
     * @param target
     *               the new data source to use
     * @return the previous data source
     */
    @Nonnull
    DataSource swap(@Nonnull DataSource target);
}
