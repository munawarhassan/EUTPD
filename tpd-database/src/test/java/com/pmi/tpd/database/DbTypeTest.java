package com.pmi.tpd.database;

import static com.pmi.tpd.database.DbType.MSSQL;
import static com.pmi.tpd.database.DbType.MYSQL;
import static com.pmi.tpd.database.DbType.ORACLE;
import static com.pmi.tpd.database.DbType.POSTGRES;
import static com.pmi.tpd.database.DbType.forDriver;
import static com.pmi.tpd.database.DbType.forKey;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the operation of the {@link DbType} enumeration.
 */
public class DbTypeTest {
    // ==================================================================================================================
    // Tests of the forKey method
    // ==================================================================================================================

    /**
     * Tests the positive path (key found) through the {@code forKey} method.
     */
    @Test
    public void forOracleKey() {
        assertEquals(of(ORACLE), forKey("oracle"));
    }

    /**
     * Tests the negative path (key not found) through the {@code forKey} method.
     */
    @Test
    public void forBollocksKey() {
        assertEquals(empty(), forKey("bollocks"));
    }

    // ==================================================================================================================
    // Tests of the forDriver method
    // ==================================================================================================================

    @Test
    public void forOracleDriver() {
        assertEquals(of(ORACLE), forDriver("oracle.jdbc.driver.OracleDriver"));
    }

    // jTDS was removed in 2.1, and should no longer return a valid DbType
    @Test
    public void forMsSqlJtdsDriver() {
        assertEquals(empty(), forDriver("net.sourceforge.jtds.jdbc.Driver"));
    }

    @Test
    public void testMsSqlMicrosoftDriver() {
        assertEquals(of(MSSQL), forDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
    }

    /**
     * Negative path (DB type not found) through the forDriver method
     */
    @Test
    public void forBollocksDriver() {
        assertEquals(empty(), forDriver("com.example.jdbc.NoSuchDriver"));
    }

    // ==================================================================================================================
    // Tests concerning JDBC URL generation
    // ==================================================================================================================

    @Test
    public void msSqlMicrosoftUrl() {
        assertEquals("jdbc:sqlserver://example.com:6098;databaseName=app;",
            MSSQL.generateUrl("example.com", "app", 6098));
    }

    @Test
    public void mySqlUrl() {
        assertEquals(
            "jdbc:mysql://example.com:6098/app?characterEncoding=utf8&useUnicode=true&sessionVariables=storage_engine%3DInnoDB",
            MYSQL.generateUrl("example.com", "app", 6098));
    }

    @Test
    public void generateOracleUrl() {
        assertEquals("jdbc:oracle:thin:@//example.com:6098/app", ORACLE.generateUrl("example.com", "app", 6098));
    }

    @Test
    public void generatePostgresUrl() {
        assertEquals("jdbc:postgresql://example.com:6098/app", POSTGRES.generateUrl("example.com", "app", 6098));
    }
}
