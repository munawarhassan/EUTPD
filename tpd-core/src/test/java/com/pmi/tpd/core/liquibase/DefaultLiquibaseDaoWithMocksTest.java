package com.pmi.tpd.core.liquibase;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.core.liquibase.upgrade.CustomChangePackage;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.lockservice.LockService;

/**
 * Complement of {@link DefaultLiquibaseDaoTest} for methods that are difficult to test without mocks.
 *
 * @see DefaultLiquibaseDaoTest
 */
public class DefaultLiquibaseDaoWithMocksTest extends MockitoTestCase {

    private DefaultLiquibaseAccessor accessor;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private LockService lockService;

    @Mock
    private Consumer<ILiquibaseAccessor> effect;

    private final IDatabaseTables databaseTables = new DefaultDatabaseTables();

    @Mock
    private ISchemaCreator schemaCreator;

    @BeforeEach
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getMetaData().getURL()).thenReturn("");
        when(connection.getMetaData().getSQLKeywords()).thenReturn("");

        when(databaseMetaData.getDatabaseProductName()).thenReturn("Apache Derby");

        accessor = new DefaultLiquibaseAccessor(schemaCreator, databaseTables, dataSource, 20L,
                CustomChangePackage.class.getPackageName()) {

            @Override
            public LockService getLockService(final Database database) {
                return lockService;
            }
        };
    }

    @Test
    public void testWithLock() throws Exception {
        final InOrder inOrder = inOrder(lockService, effect);
        accessor.withLock(effect);
        inOrder.verify(lockService).waitForLock();
        inOrder.verify(effect).accept(any(ILiquibaseAccessor.class));
        inOrder.verify(lockService).releaseLock();
    }

    @Test
    public void testClose() throws Exception {
        accessor.close();
        verify(connection).close();
    }

    @Test
    public void testBeginAndEndChangeset() throws Exception {
        accessor.beginChangeSet();
        verify(connection, times(1)).setAutoCommit(eq(false));

        accessor.endChangeSet();
        verify(connection).commit();
    }

    @Test
    public void testRollback() throws Exception {
        accessor.rollback();
        verify(connection).rollback();
    }

    @Test
    public void testBooleanConversionNotPerformedOnNonOracle() throws Exception {
        assertEquals(BigDecimal.ONE,
            accessor.transformValue(mock(MSSQLDatabase.class), BigDecimal.ONE, Types.DECIMAL, 1));
        assertEquals(BigDecimal.ONE,
            accessor.transformValue(mock(HsqlDatabase.class), BigDecimal.ONE, Types.DECIMAL, 1));
        assertEquals(BigDecimal.ONE,
            accessor.transformValue(mock(PostgresDatabase.class), BigDecimal.ONE, Types.DECIMAL, 1));
        assertEquals(BigDecimal.ONE,
            accessor.transformValue(mock(MySQLDatabase.class), BigDecimal.ONE, Types.DECIMAL, 1));
    }

    @Test
    public void testOneWithNonBoolWidthUnconvertedOnOracle() throws Exception {
        assertEquals(BigDecimal.ONE,
            accessor.transformValue(mock(OracleDatabase.class), BigDecimal.ONE, Types.DECIMAL, 2));
    }

    @Test
    public void testOtherBigDecimalsAreNotConvertedToBooleanOnOracle() throws Exception {
        final OracleDatabase db = mock(OracleDatabase.class);

        for (int i = -9; i < 10; ++i) {
            final BigDecimal bd = BigDecimal.valueOf(i);
            final Object o = accessor.transformValue(db, bd, Types.DECIMAL, 1);
            if (i == 0) {
                // 0, with precision 1, should be converted to false
                assertEquals(Boolean.FALSE, o);
            } else if (i == 1) {
                // 1, with precision 1, should be converted to true
                assertEquals(Boolean.TRUE, o);
            } else {
                // Any other number should be left unchanged
                assertEquals(bd, o);
            }
        }
    }

    @Test
    public void testIntegersAreNotConvertedToBooleanOnOracle() throws Exception {
        Object o = accessor.transformValue(mock(OracleDatabase.class), 1, Types.INTEGER, 1);
        assertThat(o, is(not(instanceOf(Boolean.class))));
        o = accessor.transformValue(mock(OracleDatabase.class), 1, Types.SMALLINT, 1);
        assertThat(o, is(not(instanceOf(Boolean.class))));
        o = accessor.transformValue(mock(OracleDatabase.class), 1, Types.BIGINT, 1);
        assertThat(o, is(not(instanceOf(Boolean.class))));
    }
}
