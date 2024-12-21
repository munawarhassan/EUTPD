package com.pmi.tpd.core.database;

import static com.pmi.tpd.database.DatabaseConstants.PROP_JDBC_DRIVER;
import static com.pmi.tpd.database.DatabaseConstants.PROP_JDBC_PASSWORD;
import static com.pmi.tpd.database.DatabaseConstants.PROP_JDBC_URL;
import static com.pmi.tpd.database.DatabaseConstants.PROP_JDBC_USER;
import static com.pmi.tpd.testing.hamcrest.FileMatchers.exists;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.config.spi.ConfigurationService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.transaction.support.TransactionSynchronization;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.config.FileOperationException;
import com.pmi.tpd.database.config.RemovePropertiesAmendment;
import com.pmi.tpd.database.config.RemoveSetupConfigurationRequest;
import com.pmi.tpd.database.config.SimpleDataSourceConfiguration;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link DatabaseConnectionConfiguration} class.
 */
public class DatabaseConnectionConfigurationTest extends MockitoTestCase {

    private static final String ORIGINAL_JDBC_DRIVER = "org.db.driver.OriginalJdbcDriver";

    private static final String ORIGINAL_JDBC_URL = "jdbc:db://localhost:7990/original";

    private static final String ORIGINAL_JDBC_USERNAME = "original_username";

    private static final String ORIGINAL_JDBC_PASSWORD = "0r1g1n4l_s3cr3t";

    private static final String REPLACEMENT_JDBC_DRIVER = "org.db.driver.ReplacementJdbcDriver";

    private static final String REPLACEMENT_JDBC_URL = "jdbc:db://localhost:7990/replacement";

    private static final String REPLACEMENT_JDBC_USERNAME = "replacement_username";

    private static final String REPLACEMENT_JDBC_PASSWORD = "r3pl4c3m3nt_s3cr3t";

    private static final String SYS_ADMIN_PSW_LINE = "setup.sysadmin.password=adminPsw";

    private static final String SYS_ADMIN_USERNAME_LINE = "setup.sysadmin.username=admin";

    private static final String SYS_ADMIN_DISPLAY_NAME_LINE = "setup.sysadmin.displayName=Admin dispName";

    private static final String SYS_ADMIN_EMAIL_LINE = "setup.sysadmin.emailAddress=fschroder@company.com";

    public Path tempFolder;

    @Mock
    private IClock clock;

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    @Mock(lenient = true)
    private ITransactionSynchronizer synchronizer;

    private I18nService i18nService;

    private IDataSourceConfiguration dataSourceConfig;

    private IDataSourceConfiguration replacementDataSourceConfig;

    private DatabaseConnectionConfiguration serviceUnderTest;

    private File configPropertiesFile;

    private File configPropertiesBakFile;

    /**
     * Set up involves creating a temporary directory beneath the system's temporary directory (as defined by the
     * java.io.tmpdir system property). The mock application settings will return that directory as the home directory.
     * <p>
     * The mock data source configuration is given some test data to return.
     */
    @BeforeEach
    public void setUp(@TempDir final Path path) throws IOException {
        this.tempFolder = path;
        final File homeDir = path.resolve("home").toFile();
        homeDir.mkdir();
        configPropertiesFile = new File(homeDir, ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME);
        configPropertiesBakFile = new File(homeDir, ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME + ".bak");

        when(clock.now()).thenReturn(new DateTime(2004, 12, 25, 12, 0, 0, 0, DateTimeZone.UTC));

        dataSourceConfig = new SimpleDataSourceConfiguration(ORIGINAL_JDBC_DRIVER, ORIGINAL_JDBC_URL,
                ORIGINAL_JDBC_USERNAME, ORIGINAL_JDBC_PASSWORD);

        replacementDataSourceConfig = new SimpleDataSourceConfiguration(REPLACEMENT_JDBC_DRIVER, REPLACEMENT_JDBC_URL,
                REPLACEMENT_JDBC_USERNAME, REPLACEMENT_JDBC_PASSWORD);

        final IUser user = User.builder()
                .username("jbloggs")
                .displayName("Joe Bloggs")
                .email("joe.bloggs@company.com")
                .build();
        when(authenticationContext.getCurrentUser()).thenReturn(Optional.of(user));
        when(synchronizer.register(any(TransactionSynchronization.class))).thenReturn(false);

        i18nService = new SimpleI18nService();

        final IApplicationConfiguration settings = mock(IApplicationConfiguration.class);
        when(settings.getSharedHomeDirectory()).thenReturn(homeDir.toPath());

        serviceUnderTest = new DatabaseConnectionConfiguration(settings, authenticationContext, clock, i18nService,
                synchronizer);
    }

    @Test
    public void testWritesToNewFile() throws IOException {
        assertThat(configPropertiesFile, not(exists()));

        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        final Iterable<String> lines = readFile(configPropertiesFile);
        assertEquals(7, Iterables.size(lines));
        assertEquals("#>*******************************************************", Iterables.get(lines, 0));
        assertEquals("#> Updated by Joe Bloggs on 2004-12-25T12:00:00.000Z", Iterables.get(lines, 1));
        assertEquals("#>*******************************************************", Iterables.get(lines, 2));

        assertHasOriginalProperties(lines);
    }

    /**
     * Tests that the JDBC properties in an existing configuration properties file are amended on a subsequent call to
     * the {@link ConfigurationService#saveDataSourceConfiguration} method.
     */
    @Test
    public void testAmendJdbcProperties() throws IOException {
        // create a config file with known-value JDBC properties
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        // save a different data source configuration
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig);

        // we expect to see that the JDBC properties now have the values
        // from the second data source configuration
        final List<String> lines = readFile(configPropertiesFile);
        assertHasReplacementProperties(lines);

        // we expect that there are not any un-commented-out versions of those old property values
        assertThat(lines, not(hasItem(String.format("%s=%s", PROP_JDBC_DRIVER, ORIGINAL_JDBC_DRIVER))));
        assertThat(lines, not(hasItem(String.format("%s=%s", PROP_JDBC_URL, ORIGINAL_JDBC_URL))));
        assertThat(lines, not(hasItem(String.format("%s=%s", PROP_JDBC_USER, ORIGINAL_JDBC_USERNAME))));
        assertThat(lines, not(hasItem(String.format("%s=%s", PROP_JDBC_PASSWORD, ORIGINAL_JDBC_PASSWORD))));

        // we expect that the original properties have been commented out
        assertThat(lines, hasItem(String.format("# %s=%s", PROP_JDBC_DRIVER, ORIGINAL_JDBC_DRIVER)));
        assertThat(lines, hasItem(String.format("# %s=%s", PROP_JDBC_URL, ORIGINAL_JDBC_URL)));
        assertThat(lines, hasItem(String.format("# %s=%s", PROP_JDBC_USER, ORIGINAL_JDBC_USERNAME)));
        assertThat(lines, hasItem(String.format("# %s=%s", PROP_JDBC_PASSWORD, ORIGINAL_JDBC_PASSWORD)));
    }

    /**
     * Tests that the configuration service adds to an existing config file any JDBC properties that are missing.
     */
    @Test
    public void testProvidesMissingJdbcProperties() throws IOException {
        // Put some JDBC properties into the config properties file
        // ** OOPS! We've forgotten to put in the jdbc.user property
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write(String.format("%s=%s\n", PROP_JDBC_DRIVER, ORIGINAL_JDBC_DRIVER));
        sink.write(String.format("%s=%s\n", PROP_JDBC_URL, ORIGINAL_JDBC_URL));
        sink.write(String.format("%s=%s\n", PROP_JDBC_PASSWORD, ORIGINAL_JDBC_PASSWORD));

        // go ahead with an amendment to the config properties
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig);

        // Was the missing jdbc.user property added?
        final List<String> lines = readFile(configPropertiesFile);
        assertThat(lines, hasItem(PROP_JDBC_USER + "=" + REPLACEMENT_JDBC_USERNAME));
    }

    @Test
    public void testInsertsCommentInExistingFile() throws IOException {
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write("fred.foo=One bottle of rum\n");
        sink.write("fred.bar=Two bottles of rum\n");
        sink.write("fred.tzo=Three bottles of rum\n");

        final String message = "Migration of database from HSQL to MySQL";
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig, of(message));

        final List<String> lines = readFile(configPropertiesFile);
        assertEquals("#>*******************************************************", Iterables.get(lines, 3));
        assertEquals("#> Migration of database from HSQL to MySQL", Iterables.get(lines, 4));
        assertEquals("#> Updated by Joe Bloggs on 2004-12-25T12:00:00.000Z", Iterables.get(lines, 5));
        assertEquals("#>*******************************************************", Iterables.get(lines, 6));

        // Make sure lines unrelated to JDBC are still there
        assertThat(lines, hasItem("fred.foo=One bottle of rum"));
        assertThat(lines, hasItem("fred.bar=Two bottles of rum"));
        assertThat(lines, hasItem("fred.tzo=Three bottles of rum"));
    }

    @Test
    public void testInsertsCommentInExistingFileWithoutUser() throws IOException {
        reset(authenticationContext); // Don't return a user
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write("fred.foo=One bottle of rum\n");
        sink.write("fred.bar=Two bottles of rum\n");
        sink.write("fred.tzo=Three bottles of rum\n");

        final String message = "Migration of database from HSQL to MySQL";
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig, of(message));

        final List<String> lines = readFile(configPropertiesFile);
        assertEquals("#>*******************************************************", Iterables.get(lines, 3));
        assertEquals("#> Migration of database from HSQL to MySQL", Iterables.get(lines, 4));
        assertEquals("#> Updated on 2004-12-25T12:00:00.000Z", Iterables.get(lines, 5));
        assertEquals("#>*******************************************************", Iterables.get(lines, 6));

        // Make sure lines unrelated to JDBC are still there
        assertThat(lines, hasItem("fred.foo=One bottle of rum"));
        assertThat(lines, hasItem("fred.bar=Two bottles of rum"));
        assertThat(lines, hasItem("fred.tzo=Three bottles of rum"));
    }

    /**
     * Tests that a comment inserted by a previous operation of the configuration service is replaced.
     */
    @Test
    public void testReplacesCommentWithUser() throws IOException {
        // create a config file with known-value JDBC properties
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        // Save a different data source config.
        // This should update the comment at the start of the file
        final String message = "Migration of database from HSQL to MySQL";
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig, of(message));

        final List<String> lines = readFile(configPropertiesFile);
        assertEquals("#>*******************************************************", Iterables.get(lines, 0));
        assertEquals("#> Migration of database from HSQL to MySQL", Iterables.get(lines, 1));
        assertEquals("#> Updated by Joe Bloggs on 2004-12-25T12:00:00.000Z", Iterables.get(lines, 2));
        assertEquals("#>*******************************************************", Iterables.get(lines, 3));

        // The comment lines in the file should include:
        // a) four comment lines as above
        // b) four commented-out jdbc properties.
        // That's a total of 8 comment lines.
        final Collection<String> commentLines = Collections2.filter(lines, new StartsWith("#"));
        assertThat(commentLines, hasSize(8));
    }

    @Test
    public void testReplaceCommentWithoutUser() throws IOException {
        reset(authenticationContext); // Don't return a user

        // create a config file with known-value JDBC properties
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        // Save a different data source config.
        // This should update the comment at the start of the file
        final String message = "Migration of database from HSQL to MySQL";
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig, of(message));

        final List<String> lines = readFile(configPropertiesFile);
        assertEquals("#>*******************************************************", Iterables.get(lines, 0));
        assertEquals("#> Migration of database from HSQL to MySQL", Iterables.get(lines, 1));
        assertEquals("#> Updated on 2004-12-25T12:00:00.000Z", Iterables.get(lines, 2));
        assertEquals("#>*******************************************************", Iterables.get(lines, 3));
    }

    @Test
    public void testCommentsSetupProperties() throws Exception {
        // Put sys admin properties into the config properties file
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write(SYS_ADMIN_PSW_LINE + "\n");
        sink.write(SYS_ADMIN_USERNAME_LINE + "\n");
        sink.write(SYS_ADMIN_DISPLAY_NAME_LINE + "\n");
        sink.write(SYS_ADMIN_EMAIL_LINE + "\n");

        final RemoveSetupConfigurationRequest configurationRequest = new RemoveSetupConfigurationRequest.Builder()
                .removeSysAdmin()
                .build();

        serviceUnderTest.removeSetupProperties(configurationRequest);

        // we expect that the old property values have been commented out
        final List<String> lines = readFile(configPropertiesFile);
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_PSW_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_USERNAME_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_DISPLAY_NAME_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_EMAIL_LINE, clock)));
    }

    @Test
    public void testCommentsSetupPropertiesWithCommitTransactionSynchronization() throws Exception {
        // Put sys admin properties into the config properties file
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write(SYS_ADMIN_PSW_LINE + "\n");
        sink.write(SYS_ADMIN_USERNAME_LINE + "\n");
        sink.write(SYS_ADMIN_DISPLAY_NAME_LINE + "\n");
        sink.write(SYS_ADMIN_EMAIL_LINE + "\n");

        final RemoveSetupConfigurationRequest configurationRequest = new RemoveSetupConfigurationRequest.Builder()
                .removeSysAdmin()
                .build();

        when(synchronizer.register(any(TransactionSynchronization.class))).thenReturn(true);
        serviceUnderTest.removeSetupProperties(configurationRequest);

        final ArgumentCaptor<TransactionSynchronization> synchronizationCaptor = ArgumentCaptor
                .forClass(TransactionSynchronization.class);
        verify(synchronizer).register(synchronizationCaptor.capture());

        final List<TransactionSynchronization> synchronizations = synchronizationCaptor.getAllValues();
        for (final TransactionSynchronization synchronization : synchronizations) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }

        // we expect that the old property values have been commented out
        final List<String> lines = readFile(configPropertiesFile);
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_PSW_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_USERNAME_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_DISPLAY_NAME_LINE, clock)));
        assertThat(lines, hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_EMAIL_LINE, clock)));

        assertTrue(configPropertiesBakFile.exists());
    }

    @Test
    public void testCommentsSetupPropertiesWithRollbackTransactionSynchronization() throws Exception {
        // Put sys admin properties into the config properties file
        final CharSink sink = Files.asCharSink(configPropertiesFile, Charsets.UTF_8, FileWriteMode.APPEND);
        sink.write(SYS_ADMIN_PSW_LINE + "\n");
        sink.write(SYS_ADMIN_USERNAME_LINE + "\n");
        sink.write(SYS_ADMIN_DISPLAY_NAME_LINE + "\n");
        sink.write(SYS_ADMIN_EMAIL_LINE + "\n");

        final RemoveSetupConfigurationRequest configurationRequest = new RemoveSetupConfigurationRequest.Builder()
                .removeSysAdmin()
                .build();

        when(synchronizer.register(any(TransactionSynchronization.class))).thenReturn(true);
        serviceUnderTest.removeSetupProperties(configurationRequest);

        assertThat(backupFile(), exists());

        final ArgumentCaptor<TransactionSynchronization> synchronizationCaptor = ArgumentCaptor
                .forClass(TransactionSynchronization.class);
        verify(synchronizer).register(synchronizationCaptor.capture());

        final List<TransactionSynchronization> synchronizations = synchronizationCaptor.getAllValues();
        for (final TransactionSynchronization synchronization : synchronizations) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }

        // we expect that the old property values have NOT been commented out
        final List<String> lines = readFile(configPropertiesFile);
        assertEquals(4, lines.size());
        assertThat(lines, hasItem("setup.sysadmin.password=adminPsw"));
        assertThat(lines, hasItem("setup.sysadmin.username=admin"));
        assertThat(lines, hasItem("setup.sysadmin.displayName=Admin dispName"));
        assertThat(lines, hasItem("setup.sysadmin.emailAddress=fschroder@company.com"));

        assertThat(lines, not(hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_PSW_LINE, clock))));
        assertThat(lines, not(hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_USERNAME_LINE, clock))));
        assertThat(lines, not(hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_DISPLAY_NAME_LINE, clock))));
        assertThat(lines, not(hasItem(RemovePropertiesAmendment.formatComment(SYS_ADMIN_EMAIL_LINE, clock))));

        assertNoDraftConfigurationFile();
        assertThat(backupFile(), not(exists()));
    }

    /**
     * Tests that a copy of an amended config properties file is kept as a backup.
     */
    @Test
    public void testKeepsBackup() {
        // create a file that needs to be backed up
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        // this subsequent save operation should create a backup file
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig);

        // did we get a backup file?
        assertThat(backupFile(), exists());
    }

    @Test
    public void testBackupGenerationFails() throws Exception {
        assertThrows(FileOperationException.class, () -> {
            // create config file
            final File sharedHomeDir = tempFolder.resolve("newfolder").toFile();
            sharedHomeDir.mkdir();
            final IApplicationConfiguration settings = mock(IApplicationConfiguration.class);
            when(settings.getSharedHomeDirectory()).thenReturn(sharedHomeDir.toPath());

            serviceUnderTest = new DatabaseConnectionConfiguration(settings, authenticationContext, clock, i18nService,
                    synchronizer);
            serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

            // tweak service

            serviceUnderTest = new DatabaseConnectionConfiguration(settings, authenticationContext, clock, i18nService,
                    synchronizer) {

                @Override
                File backUpConfigFile(final File original) {
                    throw new FileOperationException(i18nService.createKeyedMessage("some.key"), new IOException());
                }
            };

            final RemoveSetupConfigurationRequest configurationRequest = new RemoveSetupConfigurationRequest.Builder()
                    .removeBaseUrl()
                    .build();

            try {
                serviceUnderTest.removeSetupProperties(configurationRequest);
            } catch (final RuntimeException re) {
                assertNoDraftConfigurationFile();

                assertThat(backupFile(), not(exists()));
                throw re;
            }
        });
    }

    /**
     * Tests that we keep only one backup copy of the config properties file; the previous backup is overwritten.
     */
    @Test
    public void testReplacesEarlierBackup() throws IOException {
        // create a file that needs to be backed up
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig, of("CONFIG1"));

        // this save operation should create a backup file of CONFIG1
        serviceUnderTest.saveDataSourceConfiguration(replacementDataSourceConfig, of("CONFIG2"));

        // this save operation should create a backup file of CONFIG2
        serviceUnderTest.saveDataSourceConfiguration(dataSourceConfig);

        // Does the backup file have the CONFIG2 message specified above?
        final List<String> lines = readFile(backupFile());
        assertThat(Iterables.get(lines, 1), containsString("CONFIG2"));
    }

    // ---------------------------------------------------------------------------
    // Some utility methods and classes that help the tests do their work
    // ---------------------------------------------------------------------------

    private void assertNoDraftConfigurationFile() {
        final File[] files = configPropertiesFile.getParentFile().listFiles();
        if (files != null && files.length > 0) {
            for (final File file : files) {
                assertFalse(file.getName().startsWith("draft"));
            }
        }
    }

    private void assertHasOriginalProperties(final Iterable<String> lines) {
        assertProperties(lines,
            ORIGINAL_JDBC_DRIVER,
            ORIGINAL_JDBC_URL,
            ORIGINAL_JDBC_USERNAME,
            ORIGINAL_JDBC_PASSWORD);
    }

    private void assertHasReplacementProperties(final Iterable<String> lines) {
        assertProperties(lines,
            REPLACEMENT_JDBC_DRIVER,
            REPLACEMENT_JDBC_URL,
            REPLACEMENT_JDBC_USERNAME,
            REPLACEMENT_JDBC_PASSWORD);
    }

    private void assertProperties(final Iterable<String> lines,
        final String driver,
        final String url,
        final String username,
        final String password) {
        assertThat(lines, hasItem(String.format("%s=%s", PROP_JDBC_DRIVER, driver)));
        assertThat(lines, hasItem(String.format("%s=%s", PROP_JDBC_URL, url)));
        assertThat(lines, hasItem(String.format("%s=%s", PROP_JDBC_USER, username)));
        assertThat(lines, hasItem(String.format("%s=%s", PROP_JDBC_PASSWORD, password)));
    }

    private List<String> readFile(final File file) throws IOException {
        return Files.readLines(file, Charsets.UTF_8);
    }

    private File backupFile() {
        return new File(configPropertiesFile.getAbsolutePath() + ".bak");
    }

    /**
     * A predicate that test whether its input strings, after trimming, have a given prefix.
     */
    private static final class StartsWith implements Predicate<String> {

        private final String prefix;

        StartsWith(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean apply(final String input) {
            return input.trim().startsWith(prefix);
        }
    }
}
