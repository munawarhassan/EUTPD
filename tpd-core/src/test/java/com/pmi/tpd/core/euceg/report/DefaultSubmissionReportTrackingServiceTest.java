package com.pmi.tpd.core.euceg.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.euceg.IAttachmentService;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.task.ITrackingReportState;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.request.IRequestManager;

public class DefaultSubmissionReportTrackingServiceTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IApplicationConfiguration applicationConfiguration;

    @Mock
    private IEucegTaskExecutorManager taskExecutorManager;

    @Mock
    private IRequestManager requestManager;

    /** */
    @Mock
    private IAttachmentService attachmentService;

    /** */
    @Mock
    private IIndexerOperations indexerOperations;

    /** */
    @Mock
    private IProductSubmissionStore productSubmissionStore;

    private final I18nService i18nService = new SimpleI18nService();

    private DefaultSubmissionReportTrackingService reportTrackingManager;

    public Path folder;

    @BeforeEach
    public void forEach(@TempDir final Path path) throws Exception {
        reportTrackingManager = new DefaultSubmissionReportTrackingService(applicationConfiguration,
                taskExecutorManager, attachmentService, indexerOperations, productSubmissionStore, requestManager,
                i18nService);
        this.folder = path;
        when(applicationConfiguration.getReportDirectory()).thenReturn(path);
    }

    @Test
    public void testTrackingReport() {
        final ITaskMonitor taskMonitor = mock(ITaskMonitor.class);
        when(taskExecutorManager.trackingReport(any(ITrackingReportState.class))).thenReturn(taskMonitor);

        reportTrackingManager.trackingReport(SubmissionReportType.submission, PageUtils.newRequest(0, 20), -1);

    }

    @Test
    public void testDelete() throws IOException {
        final File file = new File(folder.toFile(), "report-tracking-admin-20130122-125455-781Z.xlsx");
        Files.touch(file);
        assertTrue(file.isFile());

        assertTrue(reportTrackingManager.delete(reportTrackingManager.createTrackingReport(file)));

        assertFalse(file.isFile());
        assertFalse(file.exists());
    }

    @Test
    public void testDeleteReturnsFalseWhenDirectory() {
        assertTrue(new File(folder.toFile(), "report-tracking-admin-20130122-125455-781Z.xlsx").mkdir());

        assertFalse(
            reportTrackingManager.delete(mockTrackingReport("report-tracking-admin-20130122-125455-781Z.xlsx")));
    }

    @Test
    public void testDeleteReturnsFalseWhenNotFound() {
        assertFalse(
            reportTrackingManager.delete(mockTrackingReport("report-tracking-admin-20130122-125455-781Z.xlsx")));
    }

    @Test
    public void testDeleteThrowsOnEmptyName() {
        assertThrows(ArgumentValidationException.class, () -> {
            reportTrackingManager.delete(mockTrackingReport(""));
        });
    }

    @Test
    public void testDeleteThrowsOnNullBackup() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportTrackingManager.delete((ITrackingReport) null);
        });
    }

    @Test
    public void testDeleteThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportTrackingManager.delete(mockTrackingReport(null));
        });
    }

    @Test
    public void testDeleteThrowsOnRelativePath() {
        assertThrows(ArgumentValidationException.class, () -> {
            reportTrackingManager.delete(mockTrackingReport("../app-config.properties"));
        });
    }

    @Test
    public void testDeleteThrowsOnWrongExtension() {
        assertThrows(ArgumentValidationException.class, () -> {
            reportTrackingManager.delete(mockTrackingReport("report-tracking-admin-20130122-125455-781.dat"));
        });
    }

    @Test
    public void testDeleteTrimsWhitespace() throws IOException {
        final File file = new File(folder.toFile(), "report-tracking-admin-20130122-125455-781Z.xlsx");
        Files.touch(file);
        assertTrue(file.isFile());

        assertTrue(reportTrackingManager
                .delete(mockTrackingReport("    report-tracking-admin-20130122-125455-781Z.xlsx\t\n   ")));

        assertFalse(file.isFile());
        assertFalse(file.exists());
    }

    @Test
    public void testFindAll() throws Exception {
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-223904-273Z.xlsx"));

        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-225321-347Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130129-172208-374Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130122-125455-781Z.xlsx"));

        Page<ITrackingReport> page = reportTrackingManager.findAll(PageUtils.newRequest(0, 3));
        assertNotNull(page);
        assertTrue(page.hasNext());
        assertPage(page,
            "report-tracking-admin-20130122-125455-781Z.xlsx",
            "report-tracking-admin-20130129-172208-374Z.xlsx",
            "report-tracking-admin-20130121-225321-347Z.xlsx");

        page = reportTrackingManager.findAll(page.nextPageable());
        assertNotNull(page);
        assertFalse(page.hasNext());
        assertPage(page,
            "report-tracking-admin-20130121-223904-273Z.xlsx",
            "report-tracking-admin-20130121-223652-257Z.xlsx");
    }

    @Test
    public void testFindAllIgnoresNonBackupFiles() throws IOException {
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-225321-347Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130129-172208-374Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-ad.min-20130129-172208-374Z.xlsx"));
        Files.touch(new File(folder.toFile(), "export-admin-20130110-103951-764Z.xlsx"));
        Files.touch(new File(folder.toFile(), "export-admin-20130110-130231-902Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-ad.min-20130129-172208-374Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-..-20130129-172208-374Z.xlsx"));

        final Page<ITrackingReport> page = reportTrackingManager.findAll(PageUtils.newRequest(0, 5));
        assertNotNull(page);
        assertFalse(page.hasNext());
        assertPage(page,
            "report-tracking-admin-20130121-223652-257Z.xlsx",
            "report-tracking-admin-20130121-225321-347Z.xlsx",
            "report-tracking-admin-20130129-172208-374Z.xlsx");
    }

    @Test
    public void testFindAllReturnsEmptyPageWhenNoBackupsExist() {
        final Page<ITrackingReport> page = reportTrackingManager.findAll(PageUtils.newRequest(0, 10));
        assertNotNull(page);
        assertEquals(0, page.getNumberOfElements());
        assertFalse(page.hasNext());

        final List<ITrackingReport> values = page.getContent();
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testFindAllReturnsEmptyPageWhenStartExceedsCount() {
        final Page<ITrackingReport> page = reportTrackingManager.findAll(PageUtils.newRequest(5, 10));
        assertNotNull(page);
        assertEquals(0, page.getNumberOfElements());
        assertFalse(page.hasNext());

        final List<ITrackingReport> values = page.getContent();
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testFindAllThrowsOnNullPageRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportTrackingManager.findAll(null);
        });
    }

    @Test
    public void testFindByName() throws IOException {
        final File file = new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx");
        Files.asCharSink(file, Charsets.UTF_8).write("report content");

        final ITrackingReport report = reportTrackingManager
                .findByName("report-tracking-admin-20130121-223652-257Z.xlsx")
                .orElse(null);
        assertNotNull(report);
        assertEquals("report-tracking-admin-20130121-223652-257Z.xlsx", report.getName());
        assertEquals(file.lastModified(), report.getModified());
        assertEquals(file.length(), report.getSize());
    }

    @Test
    public void testFindByNameIgnoresDirectories() {
        assertTrue(new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx").mkdir());

        assertEquals(Optional.empty(),
            reportTrackingManager.findByName("report-tracking-admin-20130121-223652-257Z.xlsx"));
    }

    @Test
    public void testFileWithDotAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(reportTrackingManager.findByName("report-tracking-foo.bar-20130121-223652-257Z.xlsx"));
        });
    }

    @Test
    public void testFileWithForwardSlashesAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(reportTrackingManager.findByName("report-tracking-foo/bar-20130121-223652-257Z.xlsx"));
        });
    }

    @Test
    public void testFileWithBackSlashesAreInvalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(reportTrackingManager.findByName("report-tracking-foo\\bar-20130121-223652-257Z.xlsx"));
        });
    }

    @Test
    public void testConfigProperties1Invalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(reportTrackingManager.findByName("../app-config.properties"));
        });
    }

    @Test
    public void testConfigProperties2Invalid() {
        assertThrows(ArgumentValidationException.class, () -> {
            assertNull(reportTrackingManager.findByName("..\\app-config.properties"));
        });
    }

    @Test
    public void testFindByNameReturnsNullOnNotFound() {
        assertEquals(Optional.empty(),
            reportTrackingManager.findByName("report-tracking-admin-20130121-223652-257Z.xlsx"));
    }

    @Test
    public void testGetByName() throws Exception {
        final File file = new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx");
        Files.asCharSink(file, Charsets.UTF_8).write("report content");

        final ITrackingReport report = reportTrackingManager
                .getByName("report-tracking-admin-20130121-223652-257Z.xlsx");
        assertNotNull(report);
        assertEquals("report-tracking-admin-20130121-223652-257Z.xlsx", report.getName());
        assertEquals(file.lastModified(), report.getModified());
        assertEquals(file.length(), report.getSize());
        assertEquals("report content", report.asByteSource().asCharSource(Charsets.UTF_8).read());
    }

    @Test
    public void testGetByNameThrowsOnNotFound() {
        assertThrows(NoSuchEntityException.class, () -> {
            reportTrackingManager.getByName("report-tracking-admin-20130121-223652-257Z.xlsx");
        });
    }

    @Test
    public void testGetLatest() throws Exception {
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-225321-347Z.xlsx"));
        Files.touch(new File(folder.toFile(), "report-tracking-admin-20130121-223652-257Z.xlsx"));

        final ITrackingReport backup = reportTrackingManager.getLatest();
        assertNotNull(backup);
        assertEquals("report-tracking-admin-20130121-225321-347Z.xlsx", backup.getName());
    }

    @Test
    public void testGetLatestThrowsWhenNoBackupsExist() {
        assertThrows(NoSuchEntityException.class, () -> {
            reportTrackingManager.getLatest();
        });
    }

    @Test
    public void testExtractId() {
        assertEquals("20130121-225321-347",
            reportTrackingManager.extractId("report-tracking-someuser-20130121-225321-347Z.xlsx"));
        assertNull(reportTrackingManager.extractId("wrongnname.txt"));
    }

    @Test
    public void testExtractUsername() {
        assertEquals("someuser",
            reportTrackingManager.extractUsername("report-tracking-someuser-20130121-225321-347Z.xlsx"));
        assertNull(reportTrackingManager.extractUsername("wrongnname.txt"));
    }

    @Test
    public void testExtractReportType() {
        assertEquals("tracking",
            reportTrackingManager.extractReportType("report-tracking-someuser-20130121-225321-347Z.xlsx"));
        assertNull(reportTrackingManager.extractReportType("wrongnname.txt"));
    }

    private ITrackingReport mockTrackingReport(final String name) {
        final ITrackingReport report = mock(ITrackingReport.class, withSettings().lenient());
        when(report.getName()).thenReturn(name);
        if (name != null) {
            when(report.getId()).thenReturn(reportTrackingManager.extractId(name));
            when(report.getUsername()).thenReturn(reportTrackingManager.extractUsername(name));
            when(report.getType()).thenReturn(reportTrackingManager.extractReportType(name));
        }
        return report;
    }

    private static void assertPage(final Page<ITrackingReport> page, final String... names) {
        // Note: For simplicity/speed, this is not testing ordering. testGetLatest will implicitly cover that. All that
        // is tested here is that the right items, and only the right items, are returned.
        assertEquals(names.length, page.getNumberOfElements());

        if (names.length > 0) {
            final Set<String> expected = ImmutableSet.copyOf(names);
            final Set<String> actual = page.getContent()
                    .stream()
                    .map(ITrackingReport::getName)
                    .collect(Collectors.toSet());

            assertTrue(expected.containsAll(actual));
            assertTrue(actual.containsAll(expected));
        }
    }
}
