package com.pmi.tpd.core.liquibase;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.pmi.tpd.core.JpaConfig;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.testing.AbstractJunitTest;

@Configuration
@ExtendWith(SpringExtension.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@ActiveProfiles({ "integration-test" })
@TestPropertySource("classpath:jpa-config.properties")
@ContextConfiguration(classes = { JpaConfig.class, LiquibaseMigrationTargetIT.class })
public class LiquibaseMigrationTargetIT extends AbstractJunitTest {

    @Inject
    private DataSource dataSource;

    /**
     * Run the pre-condition checks defined by {@link MigrationTarget} against the configured database.
     * <p>
     * NOTE: With the exception of {@link MigrationTarget#hasNoClashingTables()}, this doesn't check the negative cases
     * where a database is misconfigured, only that a correctly configured database.
     */
    @Test
    public void testDatabaseSatisfiesPreconditions() throws Exception {
        try (LiquibaseMigrationTarget migrationTarget = new LiquibaseMigrationTarget(dataSource.getConnection(),
                new DefaultDatabaseTables())) {
            assertFalse(migrationTarget.hasNoClashingTables(),
                "Configured database with created schema *should* have clashing tables!");
            assertTrue(migrationTarget.isUtf8(), "Configured database isn't UTF-8");
            assertTrue(migrationTarget.hasRequiredSchemaPermissions(),
                "Configured database user doesn't have the required schema permissions");
            assertTrue(migrationTarget.hasRequiredTemporaryTablePermission(),
                "Configured database user doesn't have the required temporary permissions");
            assertTrue(migrationTarget.isCaseSensitive(), "Configured database isn't case sensitive");
        }
    }
}
