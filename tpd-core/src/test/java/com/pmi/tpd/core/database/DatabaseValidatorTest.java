package com.pmi.tpd.core.database;

import static org.mockito.ArgumentMatchers.same;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.versioning.Version;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.backup.IMigrationTarget;
import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DatabaseValidationException;
import com.pmi.tpd.database.spi.DefaultDetailedDatabase;
import com.pmi.tpd.database.spi.IDatabaseSupplier;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DatabaseValidatorTest extends MockitoTestCase {

    @Mock
    private IClusterService clusterService;

    @Mock
    private Connection connection;

    @Mock(lenient = true)
    private DefaultDetailedDatabase database;

    @Mock
    private DataSource dataSource;

    @Mock(lenient = true)
    private IDatabaseSupplier databaseSupplier;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private IMigrationTarget migrationTarget;

    @Mock
    private IDatabaseTables databaseTables;

    private DatabaseValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(databaseSupplier.getForConnection(same(connection))).thenReturn(database);

        validator = new DatabaseValidator(clusterService, databaseSupplier, i18nService, databaseTables) {

            @Override
            protected IMigrationTarget createMigrationTarget(final Connection connection) {
                return migrationTarget;
            }
        };
    }

    @Test
    public void testValidate() throws SQLException {
        when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
        when(migrationTarget.hasNoClashingTables()).thenReturn(true);
        when(migrationTarget.hasRequiredSchemaPermissions()).thenReturn(true);
        when(migrationTarget.hasRequiredTemporaryTablePermission()).thenReturn(true);
        when(migrationTarget.isCaseSensitive()).thenReturn(true);
        when(migrationTarget.isUtf8()).thenReturn(true);

        validator.validate(dataSource);

        verify(clusterService).isAvailable();
        verify(database).getSupportLevel();
        verify(database, never()).isClusterable();
        verify(migrationTarget).hasNoClashingTables();
        verify(migrationTarget).hasRequiredSchemaPermissions();
        verify(migrationTarget).hasRequiredTemporaryTablePermission();
        verify(migrationTarget).isCaseSensitive();
        verify(migrationTarget).isUtf8();
        verify(connection).close();
    }

    @Test
    public void testValidateIfDataCenterAndClusterable() throws SQLException {
        when(clusterService.isAvailable()).thenReturn(true);
        when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
        when(database.isClusterable()).thenReturn(true);
        when(migrationTarget.hasNoClashingTables()).thenReturn(true);
        when(migrationTarget.hasRequiredSchemaPermissions()).thenReturn(true);
        when(migrationTarget.hasRequiredTemporaryTablePermission()).thenReturn(true);
        when(migrationTarget.isCaseSensitive()).thenReturn(true);
        when(migrationTarget.isUtf8()).thenReturn(true);

        validator.validate(dataSource);

        verify(clusterService).isAvailable();
        verify(database).getSupportLevel();
        verify(database).isClusterable();
        verify(migrationTarget).hasNoClashingTables();
        verify(migrationTarget).hasRequiredSchemaPermissions();
        verify(migrationTarget).hasRequiredTemporaryTablePermission();
        verify(migrationTarget).isCaseSensitive();
        verify(migrationTarget).isUtf8();
        verify(connection).close();
    }

    @Test
    public void testValidateIfDataCenterAndNotClusterable() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
            when(clusterService.isAvailable()).thenReturn(true);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(connection).close();
            }
        });
    }

    @Test
    public void testValidateIfHasClashingTables() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
            when(migrationTarget.isUtf8()).thenReturn(true);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(migrationTarget).isUtf8();
                verify(migrationTarget).hasNoClashingTables();
                verify(connection).close();
            }
        });
    }

    @Test
    public void testValidateIfLacksPermissions() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
            when(migrationTarget.hasNoClashingTables()).thenReturn(true);
            when(migrationTarget.isUtf8()).thenReturn(true);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(migrationTarget).hasNoClashingTables();
                verify(migrationTarget).hasRequiredSchemaPermissions();
                verify(migrationTarget).isUtf8();
                verify(connection).close();
            }
        });
    }

    @Test
    public void testValidateIfLacksTemporaryTablePermissions() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
            when(migrationTarget.hasNoClashingTables()).thenReturn(true);
            when(migrationTarget.hasRequiredSchemaPermissions()).thenReturn(true);
            when(migrationTarget.isUtf8()).thenReturn(true);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(migrationTarget).hasNoClashingTables();
                verify(migrationTarget).hasRequiredSchemaPermissions();
                verify(migrationTarget).hasRequiredTemporaryTablePermission();
                verify(migrationTarget).isUtf8();
                verify(connection).close();
            }
        });
    }

    @Test
    public void testValidateIfNotCaseSensitive() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
            when(migrationTarget.hasNoClashingTables()).thenReturn(true);
            when(migrationTarget.hasRequiredSchemaPermissions()).thenReturn(true);
            when(migrationTarget.hasRequiredTemporaryTablePermission()).thenReturn(true);
            when(migrationTarget.isUtf8()).thenReturn(true);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(migrationTarget).hasNoClashingTables();
                verify(migrationTarget).hasRequiredSchemaPermissions();
                verify(migrationTarget).hasRequiredTemporaryTablePermission();
                verify(migrationTarget).isCaseSensitive();
                verify(migrationTarget).isUtf8();
                verify(connection).close();
            }
        });
    }

    @Test
    public void testValidateIfNotUtf8() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);

            try {
                validator.validate(dataSource);
            } finally {
                verify(clusterService).isAvailable();
                verify(migrationTarget).isUtf8();
                verify(connection).close();
            }
        });

    }

    @Test
    public void testValidateIfUnsupportedDatabase() throws SQLException {
        assertThrows(DatabaseValidationException.class, () -> {
            when(database.getName()).thenReturn("MySQL");
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.UNSUPPORTED);
            when(database.getVersion()).thenReturn(new Version(5, 6, 10));

            try {
                validator.validate(dataSource);
            } finally {
                verify(database).getSupportLevel();
                verify(databaseSupplier).getForConnection(same(connection));
                verify(dataSource).getConnection();
            }
        });
    }

    @Test
    public void testValidatePropagatesCannotGetJdbcConnectionException() throws SQLException {
        assertThrows(CannotGetJdbcConnectionException.class, () -> {
            when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);

            reset(dataSource);
            doThrow(CannotGetJdbcConnectionException.class).when(dataSource).getConnection();

            try {
                validator.validate(dataSource);
            } finally {
                verify(dataSource).getConnection();
            }
        });
    }

    // Verifies that validate(DataSource) doesn't NullPointerException if the ClusterService is not set
    @Test
    public void testValidateWithoutClusterService() throws SQLException {
        when(database.getSupportLevel()).thenReturn(DatabaseSupportLevel.SUPPORTED);
        when(migrationTarget.hasNoClashingTables()).thenReturn(true);
        when(migrationTarget.hasRequiredSchemaPermissions()).thenReturn(true);
        when(migrationTarget.hasRequiredTemporaryTablePermission()).thenReturn(true);
        when(migrationTarget.isCaseSensitive()).thenReturn(true);
        when(migrationTarget.isUtf8()).thenReturn(true);

        new DatabaseValidator(databaseSupplier, i18nService, databaseTables) {

            @Override
            protected IMigrationTarget createMigrationTarget(final Connection connection) {
                return migrationTarget;
            }
        }.validate(dataSource);

        verify(database).getSupportLevel();
        verify(database, never()).isClusterable();
        verify(migrationTarget).hasNoClashingTables();
        verify(migrationTarget).hasRequiredSchemaPermissions();
        verify(migrationTarget).hasRequiredTemporaryTablePermission();
        verify(migrationTarget).isCaseSensitive();
        verify(migrationTarget).isUtf8();
        verify(connection).close();
    }
}
