package com.pmi.tpd.core.liquibase.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.io.Flushables;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.core.liquibase.AbstractLiquibaseTest;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;

/**
 * A base class for building migration tests, where Liquibase is used to export
 * and then restore a database instance populated with data.
 * <p>
 * Test subclasses are responsible for implementing the following contract
 * methods:
 * <ul>
 * <li>{@link #populateData()}: generate the initial sample data for the
 * test,</li>
 * <li>{@link #compareRestoredData()}: compare the database after it has been
 * restored from the backup.</li>
 * </ul>
 * See the description of each method for more details.
 * </p>
 *
 * @see SimpleMigrationTest for an example
 */
public abstract class AbstractLiquibaseMigrationTest extends AbstractLiquibaseTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractLiquibaseMigrationTest.class);

    // target directory for the backup generated during the test
    static final String TARGET_DIR = "target"; // i.e. "dao-impl/target" when run from Maven

    // prefix and suffix for the backup file
    static final String BACKUP_PREFIX = "backup-";

    static final String BACKUP_SUFFIX = ".xml";

    // user that will added as the author of the Liquibase changesets in the backup
    static final String BACKUP_USER = "backup.user";

    public Path tempFolder;

    @Mock
    private ICancelState cancelState;

    @Inject
    private DataSource dataSource;

    @Autowired
    private SessionFactory sessionFactory;

    @Value("${database.liquibase.commit.block.size:10000}")
    private long commitBlockSize;

    @Mock
    private ILiquibaseBackupMonitor backupMonitor;

    @Mock
    private ILiquibaseRestoreMonitor restoreMonitor;

    private DefaultLiquibaseMigrationDao migrationDao;

    // ------- contract for the subclass ------- //

    /**
     * Populates the initial test data.
     */
    protected abstract void populateData();

    /**
     * Inspects the data in the database after it is restored.
     * <p>
     * The test should verify the database match the data generated in
     * {@link #populateData}. Any divergence should be reported by a JUnit assertion
     * failure.
     * </p>
     */
    protected abstract void compareRestoredData();

    /**
     * Whether to remove the generated backup once the test has completed.
     */
    protected boolean removeBackupAfterTest = true;

    // ------- test execution ------- //

    @BeforeEach
    public void forEach(@TempDir Path path) {
        this.tempFolder = path;
    }

    @BeforeEach
    public void configureXmlUnit() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @BeforeEach
    public void setupMigrationDao() {
        migrationDao = new DefaultLiquibaseMigrationDao(new DefaultLiquibaseXmlWriterFactory());
    }

    @Test
    public void testMigration() throws Exception {
        final File file = createTargetFile();

        // add sample projects, repos, etc.
        populateData();
        flushData();

        // export the database as a XML backup
        backupTo(file);

        // restore the database from the backup
        prepareForRestore();
        restoreFrom(file);

        // compare the restored data match the data generated in the first step
        compareRestoredData();

        // optionally remove the backup
        if (removeBackupAfterTest && !file.delete()) {
            file.deleteOnExit();
        }
    }

    private File createTargetFile() throws IOException {
        final File directory = new File(TARGET_DIR).getAbsoluteFile();
        assertTrue(directory.isDirectory() || directory.mkdirs(), "Could not create target directory: " + directory);

        final String dbName = liquibaseAccessor.getDatabaseType().toLowerCase();
        return File.createTempFile(BACKUP_PREFIX + dbName + "-", BACKUP_SUFFIX, directory).getAbsoluteFile();
    }

    private void backupTo(final File file) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            log.info("Generating database backup to: {}", file.getPath());
            migrationDao.backup(liquibaseAccessor, out, BACKUP_USER, backupMonitor, cancelState);
            Flushables.flush(out, true);
        }
    }

    private void restoreFrom(final File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            migrationDao.restore(liquibaseAccessor, in, tempFolder.toFile(), restoreMonitor, cancelState);
        }
    }

    private void flushData() {
        sessionFactory.getCurrentSession().flush(); // make sure any pending operation was performed
    }

    private void prepareForRestore() throws SQLException {
        // clear data from Hibernate L1 cache
        sessionFactory.getCurrentSession().clear();

        // roll back any changes made in the current tx - this results in empty tables,
        // except for...
        liquibaseAccessor.rollback();

        // the id_sequence values are updated in separate transactions, so they need to
        // deleted separately
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from id_sequence")) {
                statement.execute();
            }
        }
    }
}
