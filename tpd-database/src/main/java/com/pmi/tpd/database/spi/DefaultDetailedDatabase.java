package com.pmi.tpd.database.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.versioning.Version;
import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DbType;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultDetailedDatabase implements IDetailedDatabase {

    /** */
    private final IDatabase database;

    /** */
    private final DatabaseSupportLevel supportLevel;

    /** */
    private final DbType type;

    /**
     * Create new instance of {@link DefaultDetailedDatabase}.
     *
     * @param database
     *                     database to use
     * @param supportLevel
     *                     supported level information
     * @param type
     *                     characteristics of database.
     */
    public DefaultDetailedDatabase(final IDatabase database, final DatabaseSupportLevel supportLevel,
            final DbType type) {
        this.database = checkNotNull(database, "database");
        this.supportLevel = checkNotNull(supportLevel, "supportLevel");
        this.type = type;
    }

    @Override
    public int getMajorVersion() {
        return database.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return database.getMinorVersion();
    }

    @Override
    @Nonnull
    public String getName() {
        return database.getName();
    }

    @Override
    public int getPatchVersion() {
        return database.getPatchVersion();
    }

    @Nonnull
    @Override
    public DatabaseSupportLevel getSupportLevel() {
        return supportLevel;
    }

    @Override
    public DbType getType() {
        return type;
    }

    @Override
    @Nonnull
    public Version getVersion() {
        return database.getVersion();
    }

    @Override
    public boolean isClusterable() {
        return type != null && type.isClusterable();
    }

    @Override
    public boolean isInternal() {
        return DefaultDatabaseSupplier.NAME_DERBY.equals(getName());
    }
}
