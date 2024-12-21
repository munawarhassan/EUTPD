package com.pmi.tpd.core.backup.impl;

import static org.mockito.ArgumentMatchers.same;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Page;

import com.google.common.base.Charsets;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackup;
import com.pmi.tpd.core.backup.IBackupFeature;
import com.pmi.tpd.core.backup.task.BackupTask;
import com.pmi.tpd.core.backup.task.IBackupTaskFactory;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultBackupServiceTest extends MockitoTestCase {

    public Path folder;

    @Mock
    private IExecutorService clusterExecutorService;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private ILiquibaseAccessor liquibaseDao;

    @Mock
    private IMaintenanceService maintenanceService;

    @Mock
    private IBackupTaskFactory backupTaskFactory;

    @InjectMocks
    private DefaultBackupService service;

    @Mock(lenient = true)
    private IApplicationConfiguration settings;

    @Mock
    private ITaskMaintenanceMonitor taskMonitor;

    @BeforeEach
    public void forEach(@TempDir final Path path) throws Exception {
        this.folder = path;
        when(settings.getBackupDirectory()).thenReturn(path);
    }

    @Test
    public void testBackup() {
        final BackupTask task = mock(BackupTask.class);
        when(backupTaskFactory.backupTask()).thenReturn(task);
        when(maintenanceService.start(any(IRunnableTask.class), any(MaintenanceType.class))).thenReturn(taskMonitor);

        service.backup();

        verify(maintenanceService).start(same(task), eq(MaintenanceType.BACKUP));
        verify(backupTaskFactory).backupTask();

    }

    @Test
    public void testBackupThrowsWhenMaintenanceInProgress() {
        assertThrows(BackupException.class, () -> {
            final BackupTask task = mock(BackupTask.class);
            when(backupTaskFactory.backupTask()).thenReturn(task);

            doThrow(IllegalStateException.class).when(maintenanceService).start(same(task), eq(MaintenanceType.BACKUP));

            try {
                service.backup();
            } finally {
                verify(maintenanceService).start(same(task), eq(MaintenanceType.BACKUP));
                verify(backupTaskFactory).backupTask();
            }
        });
    }

    @Test
    public void testDelete() throws IOException {
        final File file = new File(folder.toFile(), "backup-admin-20130122-125455-781Z.zip");
        Files.touch(file);
        assertTrue(file.isFile());

        assertTrue(service.delete(mockBackup("backup-admin-20130122-125455-781Z.zip")));

        assertFalse(file.isFile());
        assertFalse(file.exists());
    }

    @Test
    public void testDeleteReturnsFalseWhenDirectory() {
        assertTrue(new File(folder.toFile(), "backup-admin-20130122-125455-781Z.zip").mkdir());

        assertFalse(service.delete(mockBackup("backup-admin-20130122-125455-781Z.zip")));
    }

    @Test
    public void testDeleteReturnsFalseWhenNotFound() {
        assertFalse(service.delete(mockBackup("backup-admin-20130122-125455-781Z.zip")));
    }

    @Test
    public void testDeleteThrowsOnEmptyName() {
        assertThrows(ArgumentValidationException.class, () -> {
            service.delete(mockBackup(""));
        });
    }

    @Test
    public void testDeleteThrowsOnMigrationZip() {
        assertThrows(ArgumentValidationException.class, () -> {
            service.delete(mockBackup("migration-admin-20121128-163943-117Z.zip"));
        });
    }

    @Test
    public void testDeleteThrowsOnNullBackup() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.delete(null);
        });
    }

    @Test
    public void testDeleteThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.delete(mockBackup(null));
        });
    }

    @Test
    public void testDeleteThrowsOnRelativePath() {
        assertThrows(ArgumentValidationException.class, () -> {
            service.delete(mockBackup("../app-config.properties"));
        });
    }

    @Test
    public void testDeleteThrowsOnWrongExtension() {
        assertThrows(ArgumentValidationException.class, () -> {
            service.delete(mockBackup("backup-admin-20130122-125455-781.dat"));
        });
    }

    @Test
    public void testDeleteTrimsWhitespace() throws IOException {
        final File file = new File(folder.toFile(), "backup-admin-20130122-125455-781Z.zip");
        Files.touch(file);
        assertTrue(file.isFile());

        assertTrue(service.delete(mockBackup("    backup-admin-20130122-125455-781Z.zip\t\n   ")));

        assertFalse(file.isFile());
        assertFalse(file.exists());
    }

    @Test
    public void testFindAll() throws Exception {
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-223904-273Z.zip"));

        Files.touch(new File(folder.toFile(), "backup-admin-20130121-225321-347Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130129-172208-374Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130122-125455-781Z.zip"));

        Page<IBackup> page = service.findAll(PageUtils.newRequest(0, 3));
        assertNotNull(page);
        assertTrue(page.hasNext());
        assertPage(page,
            "backup-admin-20130122-125455-781Z.zip",
            "backup-admin-20130129-172208-374Z.zip",
            "backup-admin-20130121-225321-347Z.zip");

        page = service.findAll(page.nextPageable());
        assertNotNull(page);
        assertFalse(page.hasNext());
        assertPage(page, "backup-admin-20130121-223904-273Z.zip", "backup-admin-20130121-223652-257Z.zip");
    }

    @Test
    public void testFindAllIgnoresNonBackupFiles() throws IOException {
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-225321-347Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130129-172208-374Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-ad.min-20130129-172208-374Z.zip"));
        Files.touch(new File(folder.toFile(), "migration-admin-20130110-103951-764Z.zip"));
        Files.touch(new File(folder.toFile(), "migration-admin-20130110-130231-902Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-ad.min-20130129-172208-374Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-..-20130129-172208-374Z.zip"));

        final Page<IBackup> page = service.findAll(PageUtils.newRequest(0, 5));
        assertNotNull(page);
        assertFalse(page.hasNext());
        assertPage(page,
            "backup-admin-20130121-223652-257Z.zip",
            "backup-admin-20130121-225321-347Z.zip",
            "backup-admin-20130129-172208-374Z.zip");
    }

    @Test
    public void testFindAllReturnsEmptyPageWhenNoBackupsExist() {
        final Page<IBackup> page = service.findAll(PageUtils.newRequest(0, 10));
        assertNotNull(page);
        assertEquals(0, page.getNumberOfElements());
        assertFalse(page.hasNext());

        final List<IBackup> values = page.getContent();
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testFindAllReturnsEmptyPageWhenStartExceedsCount() {
        final Page<IBackup> page = service.findAll(PageUtils.newRequest(5, 10));
        assertNotNull(page);
        assertEquals(0, page.getNumberOfElements());
        assertFalse(page.hasNext());

        final List<IBackup> values = page.getContent();
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testFindAllThrowsOnNullPageRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.findAll(null);
        });
    }

    @Test
    public void testFindByName() throws IOException {
        final File file = new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip");
        Files.asCharSink(file, Charsets.UTF_8).write("Backup content");

        final IBackup backup = service.findByName("backup-admin-20130121-223652-257Z.zip");
        assertNotNull(backup);
        assertEquals("backup-admin-20130121-223652-257Z.zip", backup.getName());
        assertEquals(file.lastModified(), backup.getModified());
        assertEquals(file.length(), backup.getSize());
    }

    @Test
    public void testFindByNameIgnoresDirectories() {
        assertTrue(new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip").mkdir());

        assertNull(service.findByName("backup-admin-20130121-223652-257Z.zip"));
    }

    @Test
    public void testFileWithDotAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(service.findByName("backup-foo.bar-20130121-223652-257Z.zip"));
        });
    }

    @Test
    public void testFileWithForwardSlashesAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(service.findByName("backup-foo/bar-20130121-223652-257Z.zip"));
        });
    }

    @Test
    public void testFileWithBackSlashesAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(service.findByName("backup-foo\\bar-20130121-223652-257Z.zip"));
        });
    }

    @Test
    public void testConfigProperties1Invalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(service.findByName("../app-config.properties"));
        });
    }

    @Test
    public void testConfigProperties2Invalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(service.findByName("..\\app-config.properties"));
        });
    }

    @Test
    public void testFindByNameReturnsNullOnNotFound() {
        assertNull(service.findByName("backup-admin-20130121-223652-257Z.zip"));
    }

    @Test
    public void testGetByName() throws Exception {
        final File file = new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip");
        Files.asCharSink(file, Charsets.UTF_8).write("Backup content");

        final IBackup backup = service.getByName("backup-admin-20130121-223652-257Z.zip");
        assertNotNull(backup);
        assertEquals("backup-admin-20130121-223652-257Z.zip", backup.getName());
        assertEquals(file.lastModified(), backup.getModified());
        assertEquals(file.length(), backup.getSize());
        assertEquals("Backup content", backup.asCharSource(Charsets.UTF_8).read());
    }

    @Test
    public void testGetByNameThrowsOnNotFound() {
        assertThrows(NoSuchEntityException.class, () -> {
            service.getByName("backup-admin-20130121-223652-257Z.zip");
        });
    }

    @Test
    public void testGetLatest() throws Exception {
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-225321-347Z.zip"));
        Files.touch(new File(folder.toFile(), "backup-admin-20130121-223652-257Z.zip"));

        final IBackup backup = service.getLatest();
        assertNotNull(backup);
        assertEquals("backup-admin-20130121-225321-347Z.zip", backup.getName());
    }

    @Test
    public void testGetLatestThrowsWhenNoBackupsExist() {
        assertThrows(NoSuchEntityException.class, () -> {
            service.getLatest();
        });
    }

    @Test
    public void testGetFeatures() {

        when(liquibaseDao.findCustomChanges()).thenReturn(ImmutableSet.<Class<?>> of(getClass()));

        final List<IBackupFeature> backupFeatures = service.getFeatures();

        assertThat(backupFeatures, containsFeatureLike("database", null, BackupFeatureMode.RESTORE));
        assertThat(backupFeatures, containsFeatureLike("core", "backup-support", BackupFeatureMode.BACKUP));
        assertThat(backupFeatures, containsFeatureLike("web", "json", BackupFeatureMode.BACKUP));
        assertThat(backupFeatures, containsFeatureLike("forks", "alternates", BackupFeatureMode.BOTH));
        assertEquals(4, backupFeatures.size(), "Incorrect amount of features returned");
    }

    private static Matcher<List<IBackupFeature>> containsFeatureLike(final String group,
        final String name,
        final BackupFeatureMode mode) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object item) {
                // noinspection unchecked
                @SuppressWarnings("unchecked")
                final List<IBackupFeature> backupFeatures = (List<IBackupFeature>) item;
                return Collections2
                        .filter(backupFeatures,
                            input -> (group == null || input.getGroup().equals(group))
                                    && (name == null || input.getName().equals(name))
                                    && (mode == null || input.getMode() == mode))
                        .iterator()
                        .hasNext();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("lacks the feature " + group + ":" + name + ":" + mode);
            }
        };
    }

    private static void assertPage(final Page<IBackup> page, final String... names) {
        // Note: For simplicity/speed, this is not testing ordering. testGetLatest will implicitly cover that. All that
        // is tested here is that the right items, and only the right items, are returned.
        assertEquals(names.length, page.getNumberOfElements());

        if (names.length > 0) {
            final Set<String> expected = ImmutableSet.copyOf(names);
            final Set<String> actual = ImmutableSet
                    .copyOf(Iterables.transform(page.getContent(), @Nullable IBackup::getName));

            assertTrue(expected.containsAll(actual));
            assertTrue(actual.containsAll(expected));
        }
    }

    @Test
    public void testDeleteThrowsOnNestedPage() {
        assertThrows(ArgumentValidationException.class, () -> {
            service.delete(mockBackup("plugin/some-output.file"));
        });
    }

    private static IBackup mockBackup(final String name) {
        final IBackup backup = mock(IBackup.class);
        when(backup.getName()).thenReturn(name);

        return backup;
    }
}
