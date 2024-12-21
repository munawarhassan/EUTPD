package com.pmi.tpd.database.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.versioning.Version;

/**
 * An implementation of {@link IDatabase} which can be populated using fields from JDBC's {@code DatabaseMetaData}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultJdbcMetadataDatabase implements IDatabase {

    /** */
    private final int majorVersion;

    /** */
    private final int minorVersion;

    /** */
    private final String name;

    /** */
    private final Version version;

    /**
     * Create new instance of {@link DefaultJdbcMetadataDatabase}.
     *
     * @param name
     *                     the name of database product name
     * @param version
     *                     the version of database.
     * @param majorVersion
     *                     the major version.
     * @param minorVersion
     *                     the minor version.
     */
    public DefaultJdbcMetadataDatabase(@Nonnull final String name, @Nonnull final Version version,
            final int majorVersion, final int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.name = checkNotNull(name, "name");
        this.version = checkNotNull(version, "version");
    }

    @Override
    public int getMajorVersion() {
        return majorVersion;
    }

    @Override
    public int getMinorVersion() {
        return minorVersion;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPatchVersion() {
        return Iterables.get(version.getVersion(), 2, 0);
    }

    @Nonnull
    @Override
    public Version getVersion() {
        return version;
    }
}
