package com.pmi.tpd.core.cluster;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.CONNECT;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.DISCONNECT;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_ANY_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.core.cluster.check.SharedHomeAndDatabaseJoinCheck;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

public class SharedHomeAndDatabaseJoinCheckTest extends AbstractClusterJoinCheckTest {

    public Path tmpFolder;

    @Mock(lenient = true)
    private IDatabaseConfigurationService configurationService;

    @Mock
    private IDataSourceConfiguration dataSourceConfiguration;

    @Mock
    private ISecureTokenGenerator secureTokenGenerator;

    @BeforeEach
    public void forEach(@TempDir final Path path) throws Exception {
        this.tmpFolder = path;
        when(dataSourceConfiguration.getUrl()).thenReturn("url");
        when(dataSourceConfiguration.getUser()).thenReturn("user");

        when(secureTokenGenerator.generateToken()).thenReturn("unique-token");
    }

    @Test
    public void testCheckSucceedsWithMatchingSharedHomeAndDatabase() throws Exception {
        final IApplicationConfiguration settings = createApplicationConfiguration(tmpFolder);
        final CheckResult result = executeJoinCheck(
            new SharedHomeAndDatabaseJoinCheck(settings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator),
            new SharedHomeAndDatabaseJoinCheck(settings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator));

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testCheckFailsForUnrelatedCluster() throws Exception {
        final Path folder1 = tmpFolder.resolve("folder1");
        folder1.toFile().mkdir();
        final Path folder2 = tmpFolder.resolve("folder2");
        folder2.toFile().mkdir();
        // different shared homes
        final IApplicationConfiguration connectSettings = createApplicationConfiguration(folder1);
        final IApplicationConfiguration acceptSettings = createApplicationConfiguration(folder2);
        // different dbs
        when(dataSourceConfiguration.getUrl()).thenReturn("db1", "db2");

        final CheckResult result = executeJoinCheck(
            new SharedHomeAndDatabaseJoinCheck(connectSettings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator),
            new SharedHomeAndDatabaseJoinCheck(acceptSettings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator));

        // should disconnect, but not passivate
        assertEquals(DISCONNECT, result.acceptResult.getAction());
        // connect side always succeeds
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testCheckFailsWithMismatchedSharedHome() throws Exception {
        final Path folder1 = tmpFolder.resolve("folder1");
        folder1.toFile().mkdir();
        final Path folder2 = tmpFolder.resolve("folder2");
        folder2.toFile().mkdir();
        final IApplicationConfiguration connectSettings = createApplicationConfiguration(folder1);
        final IApplicationConfiguration acceptSettings = createApplicationConfiguration(folder2);

        final CheckResult result = executeJoinCheck(
            new SharedHomeAndDatabaseJoinCheck(connectSettings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator),
            new SharedHomeAndDatabaseJoinCheck(acceptSettings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator));

        assertEquals(PASSIVATE_ANY_NODE, result.acceptResult.getAction());
        // connect side always succeeds
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testCheckFailsWithMismatchedDatabase() throws Exception {
        when(configurationService.loadDataSourceConfiguration()).thenReturn(dataSourceConfiguration);

        final IDatabaseConfigurationService configurationService2 = mock(IDatabaseConfigurationService.class);
        final IDataSourceConfiguration configuration2 = mock(IDataSourceConfiguration.class, withSettings().lenient());
        when(configuration2.getUrl()).thenReturn("other-url");
        when(configuration2.getUser()).thenReturn("user");
        when(configurationService2.loadDataSourceConfiguration()).thenReturn(configuration2);

        final IApplicationConfiguration settings = createApplicationConfiguration(tmpFolder);
        when(dataSourceConfiguration.getUrl()).thenReturn("db1", "db2");

        final CheckResult result = executeJoinCheck(
            new SharedHomeAndDatabaseJoinCheck(settings, configurationService, dataSourceConfiguration,
                    secureTokenGenerator),
            new SharedHomeAndDatabaseJoinCheck(settings, configurationService2, dataSourceConfiguration,
                    secureTokenGenerator));

        assertEquals(PASSIVATE_THIS_NODE, result.acceptResult.getAction());
        // connect always succeeds
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    private IApplicationConfiguration createApplicationConfiguration(final Path sharedHome) {
        final IApplicationConfiguration applicationSettings = mock(IApplicationConfiguration.class);
        when(applicationSettings.getSharedHomeDirectory()).thenReturn(sharedHome);
        return applicationSettings;
    }
}
