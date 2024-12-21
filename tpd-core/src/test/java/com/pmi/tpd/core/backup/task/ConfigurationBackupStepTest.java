package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper.assertProgress;
import static org.mockito.ArgumentMatchers.anyBoolean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

public class ConfigurationBackupStepTest extends MockitoTestCase {

    private static final String CONTENT = "# example config content";

    private static final String DRIVER_CLASS_NAME = "driver.class";

    private static final String PASSWORD = "password";

    private static final String URL = "url";

    private static final String USER = "user";

    public Path tmpDir;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock(lenient = true)
    private IDataSourceConfiguration configuration;

    @Mock(lenient = true)
    private IBackupState state;

    private ConfigurationBackupStep step;

    private ByteArrayOutputStream output;

    private ZipOutputStream zipStream;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        this.tmpDir = path;
        output = new ByteArrayOutputStream();
        zipStream = spy(new ZipOutputStream(output));

        final IDatabaseHandle handle = mock(IDatabaseHandle.class, withSettings().lenient());

        when(state.getBackupZipStream()).thenReturn(zipStream);
        when(state.getSourceDatabase()).thenReturn(handle);
        when(handle.getConfiguration()).thenReturn(configuration);

        when(configuration.getDriverClassName()).thenReturn(DRIVER_CLASS_NAME);
        when(configuration.getPassword()).thenReturn(PASSWORD);
        when(configuration.getUrl()).thenReturn(URL);
        when(configuration.getUser()).thenReturn(USER);

        final IApplicationConfiguration applicationSettings = mock(IApplicationConfiguration.class,
            withSettings().lenient());
        when(applicationSettings.getSharedHomeDirectory()).thenReturn(tmpDir);

        step = new ConfigurationBackupStep(state, applicationSettings, i18nService);
    }

    @Test
    public void testRunIoException() throws IOException {
        assertThrows(BackupException.class, () -> {
            doThrow(new IOException("")).when(zipStream).putNextEntry(any(ZipEntry.class));
            step.run();
        });

    }

    @Test
    public void testRunNoConfigFilePresent() throws Exception {
        assertProgress("app.backup.configuration", 0, step.getProgress());

        step.run();

        final String content = getContent();

        assertTrue(content.startsWith(ConfigurationBackupStep.BANNER + "\n"));
        assertTrue(content.contains("jdbc.driverClassName=" + DRIVER_CLASS_NAME + "\n"));
        assertTrue(content.contains("jdbc.url=" + URL + "\n"));
        assertTrue(content.contains("jdbc.username=" + USER + "\n"));
        assertTrue(content.contains("jdbc.password=" + PASSWORD + "\n"));

        assertProgress("app.backup.configuration", 100, step.getProgress());

        verify(zipStream).putNextEntry(any(ZipEntry.class), anyBoolean());
        verify(zipStream, atLeastOnce()).closeEntry();
    }

    @Test
    public void testRunWithConfigFile() throws Exception {
        createConfigFile();

        assertProgress("app.backup.configuration", 0, step.getProgress());
        step.run();

        assertEquals(CONTENT, getContent());
        assertProgress("app.backup.configuration", 100, step.getProgress());

        verify(zipStream).putNextEntry(any(ZipEntry.class), anyBoolean());
        verify(zipStream, atLeastOnce()).closeEntry();
    }

    private void createConfigFile() {
        try {
            Files.asCharSink(new File(tmpDir.toFile(), ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME),
                Charsets.UTF_8).write(CONTENT);;
        } catch (final IOException e) {
            fail("Error while writing test config file " + e);
        }
    }

    private String getContent() throws IOException {
        final ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(output.toByteArray()));
        try {
            inputStream.getNextEntry();
            return CharStreams.toString(new InputStreamReader(inputStream));
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }
}
