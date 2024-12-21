package com.pmi.tpd.core.liquibase;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;

import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.core.liquibase.upgrade.CustomChangePackage;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;

public abstract class AbstractLiquibaseTest extends BaseDaoTestIT {

    @Inject
    private ISchemaCreator schemaCreator;

    @Inject
    private DataSource dataSource;

    protected DefaultLiquibaseAccessor liquibaseAccessor;

    @BeforeEach
    public void setupLiquibaseAccessor() {
        liquibaseAccessor = new DefaultLiquibaseAccessor(schemaCreator, new DefaultDatabaseTables(), dataSource, 1000,
                CustomChangePackage.class.getPackageName()) {

            @Override
            public void close() {
                // the parent method will call the close() on the java.sql.Connection, whose effect are
                // implementation-specific if there is an active transaction (see the javadoc of Connection.close()).
                // Thus, to ensure the transaction is not committed, and since we can also not rollback the transaction
                // as we want to check what would be the database contents if it _was_ committed, we just ignore the
                // order and do nothing.
            }

            @Override
            public void withLock(final Consumer<ILiquibaseAccessor> effect) {
                // applies the effect directly, since during the execution of the test, we are effectively the only one
                // querying for the lock anyways, and since the invocation of LockService.releaseLock() in the parent
                // method will otherwise rollback the current transaction.
                effect.accept(this);
            }

            @Override
            public void commit() {
                // prevents any commit of the Liquibase changesets as they are applied
            }
        };
    }

}
