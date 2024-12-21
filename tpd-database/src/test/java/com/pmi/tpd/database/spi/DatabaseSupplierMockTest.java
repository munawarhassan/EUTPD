package com.pmi.tpd.database.spi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.UnsupportedDatabaseException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DatabaseSupplierMockTest extends MockitoTestCase {

    @Mock
    private Connection connection;

    @Mock(lenient = true)
    private DataSource dataSource;

    @Mock
    private DatabaseMetaData metaData;

    @InjectMocks
    private DefaultDatabaseSupplier supplier;

    @Test
    public void testGetForUnknownDatabaseReturnsUnsupported() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);

        when(dataSource.getConnection()).thenReturn(connection);

        // These values are drawn from H2's JdbcDatabaseMetaData class
        when(metaData.getDatabaseProductName()).thenReturn("H2");
        when(metaData.getDatabaseProductVersion()).thenReturn("1.3.171 (2013-03-17)");
        when(metaData.getDatabaseMajorVersion()).thenReturn(1);
        when(metaData.getDatabaseMinorVersion()).thenReturn(3);

        final IDetailedDatabase database = supplier.get();
        assertNotNull(database);
        assertEquals(1, database.getMajorVersion());
        assertEquals(3, database.getMinorVersion());
        assertEquals("H2", database.getName());
        assertEquals(171, database.getPatchVersion());
        assertEquals("1.3.171", database.getVersion().toString());
        assertSame(DatabaseSupportLevel.UNSUPPORTED, database.getSupportLevel());
    }

    @Test
    public void testGetUnwrapsInitializationExceptions() throws SQLException {
        assertThrows(CannotGetJdbcConnectionException.class, () -> {
            doThrow(SQLException.class).when(dataSource).getConnection();

            try {
                supplier.get();
            } finally {
                verify(dataSource).getConnection();
            }
        });
    }

    @Test
    public void testGetForConnectionHandlesExceptionFromMetadata() throws SQLException {
        assertThrows(DataRetrievalFailureException.class, () -> {
            doThrow(SQLException.class).when(connection).getMetaData();

            when(dataSource.getConnection()).thenReturn(connection);

            try {
                supplier.getForConnection(connection);
            } finally {
                verify(connection).getMetaData();
            }
        });
    }

    @Test
    public void testParseVersionHandlesMultiLineOracleVersions() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);

        when(dataSource.getConnection()).thenReturn(connection);

        when(metaData.getDatabaseProductName()).thenReturn("Oracle");
        when(metaData.getDatabaseProductVersion())
                .thenReturn("Oracle Database 11g Enterprise Edition Release 11.2.0.3.0 - 64bit Production\n"
                        + "With the Partitioning, Automatic Storage Management and Oracle Label Security options");
        when(metaData.getDatabaseMajorVersion()).thenReturn(11);
        when(metaData.getDatabaseMinorVersion()).thenReturn(2);

        final IDetailedDatabase database = supplier.get();
        assertNotNull(database);
        assertEquals(11, database.getMajorVersion());
        assertEquals(2, database.getMinorVersion());
        assertEquals("Oracle", database.getName());
        assertEquals(0, database.getPatchVersion());
        assertEquals("11.2.0.3.0", database.getVersion().toString());
        assertSame(DatabaseSupportLevel.SUPPORTED, database.getSupportLevel());
    }

    @Test
    public void testParseVersionHandlesUnexpectedOracleVersions() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);

        when(dataSource.getConnection()).thenReturn(connection);

        when(metaData.getDatabaseProductName()).thenReturn("Oracle");
        when(metaData.getDatabaseProductVersion()).thenReturn("12.0.1");
        when(metaData.getDatabaseMajorVersion()).thenReturn(11); // Something like this could never happen, but this
        when(metaData.getDatabaseMinorVersion()).thenReturn(2); // makes validation much easier

        final IDetailedDatabase database = supplier.get();
        assertNotNull(database);
        assertEquals(11, database.getMajorVersion()); // Verifies that the version string is _ignored_
        assertEquals(2, database.getMinorVersion());
        assertEquals("Oracle", database.getName());
        assertEquals(0, database.getPatchVersion());
        assertEquals("11.2.0", database.getVersion().toString()); // Version pads to 3 places
        assertSame(DatabaseSupportLevel.SUPPORTED, database.getSupportLevel());
    }

    @Test
    public void testValidateAllowsDatabaseWithUnknownSupport() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);

        when(dataSource.getConnection()).thenReturn(connection);

        when(metaData.getDatabaseProductName()).thenReturn("Oracle");
        when(metaData.getDatabaseProductVersion())
                .thenReturn("Oracle Database 10g Express Edition Release 10.2.0.2.0 - Production");
        when(metaData.getDatabaseMajorVersion()).thenReturn(10);
        when(metaData.getDatabaseMinorVersion()).thenReturn(2);

        supplier.validate();

        verify(connection).getMetaData();
        verify(connection).close();
        verify(dataSource).getConnection();
    }

    @Test
    public void testValidateHonorsIgnoreUnsupported() {
        supplier.setIgnoreUnsupported(true);
        supplier.validate();

        verifyZeroInteractions(dataSource);
    }

    @Test
    public void testValidateThrowsOnUnsupportedDatabase() throws SQLException {
        assertThrows(UnsupportedDatabaseException.class, () -> {
            when(connection.getMetaData()).thenReturn(metaData);

            when(dataSource.getConnection()).thenReturn(connection);

            when(metaData.getDatabaseProductName()).thenReturn("MySQL");
            when(metaData.getDatabaseProductVersion()).thenReturn("5.6.11");
            when(metaData.getDatabaseMajorVersion()).thenReturn(5);
            when(metaData.getDatabaseMinorVersion()).thenReturn(6);

            try {
                supplier.validate();
            } finally {
                verify(connection).getMetaData();
                verify(connection).close();
                verify(dataSource).getConnection();
            }
        });

    }
}
