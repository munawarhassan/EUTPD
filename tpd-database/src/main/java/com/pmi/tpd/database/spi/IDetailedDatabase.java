package com.pmi.tpd.database.spi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DbType;

/**
 * Augments the {@link IDatabase} information with its calculated {@link DatabaseSupportLevel support level}.
 *
 * @since 1.3
 * @author Christophe Friederich
 */
public interface IDetailedDatabase extends IDatabase {

    /**
     * Retrieves the system's {@link DatabaseSupportLevel level of support} for the database.
     * <p>
     * The level of support is calculated from the <i>connected database</i>. The system has type-level support for
     * multiple databases, but that support may not include the specific version connected.
     *
     * @return the support level for the connected database
     */
    @Nonnull
    DatabaseSupportLevel getSupportLevel();

    /**
     * @return the {@link DbType} for this database, or {@code null} if the database is internal or unknown
     */
    @Nullable
    DbType getType();

    /**
     * Retrieves a flag indicating whether the system can use this database with a Data Center license, which allows
     * clustering multiple servers using the same database.
     *
     * @return {@code true} if this database can be used in a cluster; otherwise, {@code false} for databases which can
     *         only be used by standalone, non-Data Center installations
     */
    boolean isClusterable();

    /**
     * Retrieves a flag indicating whether the system is using the internal database.
     *
     * @return {@code true} if this database is the system's internal database; otherwise, {@code false} if this is an
     *         external database
     */
    boolean isInternal();
}
