package com.pmi.tpd.core.db.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.spi.DefaultDatabaseSupplier;
import com.pmi.tpd.database.spi.IDetailedDatabase;

@Configuration
@ContextConfiguration(classes = { DatabaseSupplierIT.class })
public class DatabaseSupplierIT extends BaseDaoTestIT {

    private static final Set<DatabaseSupportLevel> TESTED_LEVELS = ImmutableSet.of(DatabaseSupportLevel.DEPRECATED,
        DatabaseSupportLevel.SUPPORTED);

    @Inject
    private DataSource dataSource;

    private DefaultDatabaseSupplier supplier;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        supplier = new DefaultDatabaseSupplier(dataSource);
    }

    @Test
    public void testGet() {
        assertTested(supplier.get());
    }

    @Test
    public void testGetForConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertTested(supplier.getForConnection(connection));
        }
    }

    @Test
    public void testValidate() {
        // If the database support level is explicitly UNSUPPORTED, this will throw an
        // exception. Otherwise, it will
        // return normally and the test will pass
        supplier.validate();
    }

    private static void assertTested(final IDetailedDatabase database) {
        assertNotNull(database, "The DetailedDatabase returned is null");
        assertTrue(TESTED_LEVELS.contains(database.getSupportLevel()),
            () -> database.getName() + " " + database.getVersion() + " is marked as " + database.getSupportLevel()
                    + ". All tested databases should have a tested support level.");
    }
}
