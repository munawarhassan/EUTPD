package com.pmi.tpd.database;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.database.spi.IDatabase;

/**
 * Thrown when the system is connected to a database which is {@link DatabaseSupportLevel#UNSUPPORTED unsupported}, to
 * prevent the system from coming up and attempting to create/migrate the schema or use Hibernate.
 *
 * @since 1.3
 * @author Christophe Friederich
 */
public class UnsupportedDatabaseException extends UnsupportedOperationException {

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private final IDatabase database;

    /**
     * Create new instance of {@link UnsupportedDatabaseException} with the specified detail message and database
     * information.
     *
     * @param message
     *                 mutable copy of {@link IMutableDataSourceConfiguration}.
     * @param database
     *                 a database information used.
     */
    public UnsupportedDatabaseException(@Nonnull final String message, @Nonnull final IDatabase database) {
        super(checkNotNull(message, "message"));

        this.database = checkNotNull(database, "database");
    }

    /**
     * Retrieves information about the connected database, for use in error messages.
     *
     * @return the connected database
     */
    @Nonnull
    public IDatabase getDatabase() {
        return database;
    }
}
