package com.pmi.tpd.database.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import com.jolbox.bonecp.hooks.ConnectionHook;

/**
 * This {@link ConnectionHook} ensures that connections being returned to the pool have their auto-commit flag set to
 * true, and resets it to true if the client has failed to do so. By night it also has another identity - it stalks
 * leased connections and exposes the current working set via the {@link LeasedConnectionTracker} interface. This was
 * introduced as liquibase sets the auto-commit flag to false in {@link AbstractDatabase#setConnection} and fails to
 * restore it to true before returning the connection to the pool. This causes intermittent problems in other clients
 * that rely on auto-commit behaviour. Notably this caused problems in the ActiveObjectsTableCreator, which will fail to
 * commit DDL operations on databases which support transactional DDL, e.g. postgres, when auto-commit is disabled. TO DO
 * upgrade to a version of liquibase with this bug fixed, and change this class to raise an exception if a client
 * returns a connection with auto-commit disabled
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class CleanupConnectionHook extends AbstractConnectionHook implements ILeasedConnectionTracker {

    /** */
    private final Set<ConnectionHandle> leased = Collections
            .newSetFromMap(Maps.<ConnectionHandle, Boolean> newConcurrentMap());

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupConnectionHook.class);

    @Override
    public void onCheckIn(final ConnectionHandle handle) {
        leased.remove(handle);

        if (handle.isPossiblyBroken()) {
            return;
        }

        try {
            final Connection connection = handle.getInternalConnection();
            if (!connection.isClosed() && !connection.getAutoCommit()) {
                LOGGER.debug("A connection was checked into the connection pool with auto-commit disabled. "
                        + "Re-enabling auto-commit before returning to the pool.");
                connection.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            LOGGER.warn("Could not check or set the auto-commit status for the connection being checked in.", e);
        }
    }

    @Override
    public void onCheckOut(final ConnectionHandle connection) {
        leased.add(connection);
    }

    @Override
    public Set<ConnectionHandle> getLeased() {
        return leased;
    }
}
