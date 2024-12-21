package com.pmi.tpd.database.config;

import static com.pmi.tpd.database.config.DefaultDataSourceConfiguration.JTDS_DRIVER;
import static com.pmi.tpd.database.config.DefaultDataSourceConfiguration.JTDS_PROTOCOL;
import static com.pmi.tpd.database.config.DefaultDataSourceConfiguration.JTDS_URL;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.database.DbType;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.hibernate.UnsupportedJdbcUrlException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultDataSourceConfigurationTest extends MockitoTestCase {

    @Test
    public void testConstructorReplacesGeneratedJtdsUrlWithMicrosoft() {
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration(JTDS_DRIVER, "user",
                "password", JTDS_PROTOCOL + "://example.com:6098;databaseName=app;");
        assertEquals(DbType.MSSQL.getDriverClassName(), configuration.getDriverClassName());
        assertEquals(DbType.MSSQL.getProtocol() + "://example.com:6098;databaseName=app;", configuration.getUrl());
        assertEquals("password", configuration.getPassword());
        assertEquals("user", configuration.getUser());
    }

    @Test
    public void testConstructorReplacesAlternativeJtdsUrlWithMicrosoft() {
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration(JTDS_DRIVER, "user",
                "password", JTDS_PROTOCOL + "://example.com:6098/app");
        assertEquals(DbType.MSSQL.getDriverClassName(), configuration.getDriverClassName());
        assertEquals(DbType.MSSQL.getProtocol() + "://example.com:6098;databaseName=app;", configuration.getUrl());
        assertEquals("password", configuration.getPassword());
        assertEquals("user", configuration.getUser());
    }

    @Test
    public void testUpdateReplacesGeneratedJtdsUrlWithMicrosoft() {
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration("driver", "user",
                "password", "url");

        final IDataSourceConfiguration update = mock(IDataSourceConfiguration.class);
        when(update.getDriverClassName()).thenReturn(JTDS_DRIVER);
        when(update.getUrl()).thenReturn(JTDS_PROTOCOL + "://example.com:6098;databaseName=app;");
        when(update.getUser()).thenReturn("user");
        when(update.getPassword()).thenReturn("password");

        configuration.update(update);

        assertEquals(DbType.MSSQL.getDriverClassName(), configuration.getDriverClassName());
        assertEquals(DbType.MSSQL.getProtocol() + "://example.com:6098;databaseName=app;", configuration.getUrl());
        assertEquals("password", configuration.getPassword());
        assertEquals("user", configuration.getUser());
    }

    @Test
    public void testUpdateReplacesAlternativeJtdsUrlWithMicrosoft() {
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration("driver", "user",
                "password", "url");

        final IDataSourceConfiguration update = mock(IDataSourceConfiguration.class);
        when(update.getDriverClassName()).thenReturn(JTDS_DRIVER);
        when(update.getUrl()).thenReturn(JTDS_PROTOCOL + "://example.com:6098/app");
        when(update.getUser()).thenReturn("user");
        when(update.getPassword()).thenReturn("password");

        configuration.update(update);

        assertEquals(DbType.MSSQL.getDriverClassName(), configuration.getDriverClassName());
        assertEquals(DbType.MSSQL.getProtocol() + "://example.com:6098;databaseName=app;", configuration.getUrl());
        assertEquals("password", configuration.getPassword());
        assertEquals("user", configuration.getUser());
    }

    @Test
    public void testConstructorThrowsOnRedundantDatabaseNames() {
        assertThrows(UnsupportedJdbcUrlException.class, () -> {
            new DefaultDataSourceConfiguration(JTDS_DRIVER, "user", "password",
                    JTDS_PROTOCOL + "://example.com:6098/app;databaseName=app");
        });
    }

    @Test
    public void testConstructorThrowsOnUnmatchedJtdsUrls() {
        assertThrows(UnsupportedJdbcUrlException.class, () -> {
            new DefaultDataSourceConfiguration(JTDS_DRIVER, "user", "password", "jdbc:postgresql://localhost:5432/app");
        });
    }

    @Test
    public void testConstructorThrowsOnUnsupportedJtdsUrls() {
        assertThrows(UnsupportedJdbcUrlException.class, () -> {
            new DefaultDataSourceConfiguration(JTDS_DRIVER, "user", "password",
                    JTDS_PROTOCOL + "://example.com:6098;databaseName=app;domain=NA;");
        });
    }

    @Test
    public void testCopy() {
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration("driver", "user",
                "password", "url");

        final IDataSourceConfiguration copy = configuration.copy();
        assertEquals("driver", copy.getDriverClassName());
        assertEquals("user", copy.getUser());
        assertEquals("password", copy.getPassword());
        assertEquals("url", copy.getUrl());
    }

    @Test
    public void testJtdsUrlPattern() {
        assertMatch(JTDS_PROTOCOL
                + "://localhost:1433;databaseName=app;",
            "localhost",
            "1433",
            null,
            ";databaseName=app;");
        assertMatch(JTDS_PROTOCOL
                + "://localhost:1433;databaseName=app",
            "localhost",
            "1433",
            null,
            ";databaseName=app");
        assertMatch(JTDS_PROTOCOL + "://localhost;databaseName=app;", "localhost", null, null, ";databaseName=app;");
        assertMatch(JTDS_PROTOCOL + "://localhost;databaseName=app", "localhost", null, null, ";databaseName=app");
        assertMatch(JTDS_PROTOCOL + "://localhost:1433/app;", "localhost", "1433", "app", ";");
        assertMatch(JTDS_PROTOCOL + "://localhost:1433/app", "localhost", "1433", "app", null);
        assertMatch(JTDS_PROTOCOL + "://localhost/app;", "localhost", null, "app", ";");
        assertMatch(JTDS_PROTOCOL + "://localhost/app", "localhost", null, "app", null);
        assertMatch(JTDS_PROTOCOL + "://localhost:1433", "localhost", "1433", null, null);
        assertMatch(JTDS_PROTOCOL + "://localhost", "localhost", null, null, null);
        assertNoMatch(JTDS_PROTOCOL + "://localhost:;databaseName=app");
        assertNoMatch(JTDS_PROTOCOL + "://localhost:/app");
        assertNoMatch(JTDS_PROTOCOL + "://");
        assertNoMatch("jdbc:postgresql://localhost:5432/app");
    }

    private static void assertMatch(final String value,
        final String host,
        final String port,
        final String databaseName,
        final String parameters) {
        final Matcher matcher = JTDS_URL.matcher(value);
        assertTrue(matcher.matches(), value + " did not match the URL pattern where a match was expected");
        assertEquals(host, matcher.group(1), "Unexpected host extracted for [" + value + "]");
        assertEquals(port, matcher.group(2), "Unexpected port extracted for [" + value + "]");
        assertEquals(databaseName, matcher.group(3), "Unexpected database name extracted for [" + value + "]");
        assertEquals(parameters, matcher.group(4), "Unexpected parameters extracted for [" + value + "]");
    }

    private static void assertNoMatch(final String value) {
        final Matcher matcher = JTDS_URL.matcher(value);
        if (matcher.matches()) {
            fail(value + " matched the URL pattern where a match was not expected.");
        }
    }
}
