package com.pmi.tpd.database.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.versioning.Version;

/**
 * Describes a connected database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabase {

    /**
     * Retrieves the database's major version. This is the first entry in the {@link #getVersion() version}.
     *
     * @return the database's major version
     */
    int getMajorVersion();

    /**
     * Retrieves the database's minor version. This is the second entry in the {@link #getVersion() version}. If the
     * version has no explicit value, it is defaulted to {@code 0}.
     *
     * @return the database's minor version, defaulting to 0
     */
    int getMinorVersion();

    /**
     * Retrieves the database's name.
     * <p>
     * This value is taken from the connected database, <i>not</i> inferred from the JDBC driver being used to make the
     * connection.
     *
     * @return the database's name
     */
    @Nonnull
    String getName();

    /**
     * Retrieves the database's patch version. This is the third entry in the {@link #getVersion() version}. If the
     * version has no explicit value, it is defaulted to {@code 0}.
     *
     * @return the database's patch version, defaulting to 0
     */
    int getPatchVersion();

    /**
     * Retrieves the database's full version. Different databases provide more or less complete version information. The
     * presence of {@link #getMajorVersion() major} and {@link #getMinorVersion() minor} versions is mandated by the
     * JDBC interface for {@code DatabaseMetaData}, but, when handling this version, it is safest to make no assumptions
     * about how many digits it will include.
     *
     * @return the database's version
     */
    @Nonnull
    Version getVersion();
}
