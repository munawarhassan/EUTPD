package com.pmi.tpd.euceg.core.filestorage;

import static com.pmi.tpd.api.util.FluentIterable.from;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

@SuppressWarnings("exports")
public class FileStorageTest extends MockitoTestCase {

    @Nonnull
    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    @Nonnull
    private IApplicationConfiguration settings;

    @BeforeEach
    public void forEach(@TempDir final Path tempDir) throws IOException {
        when(settings.getAttachmentsDirectory()).thenReturn(tempDir);
    }

    public static Stream<Arguments> shouldCreateNewAttachmentArguments() {
        return Stream.of(arguments("attachment.txt", "attachment.txt", "attachment.txt", ""),
            arguments("./attachment.txt", "attachment.txt", "attachment.txt", ""),
            arguments("test/another/attachment.txt", "attachment.txt", "attachment.txt", "test/another"));
    }

    @ParameterizedTest
    @MethodSource("shouldCreateNewAttachmentArguments")
    public void shouldCreateNewAttachment(final String path,
        final String filename,
        final String expectedFilename,
        final String expectedLocation) throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            storage.createFile(stream, path);
        }

        assertThat(storage.exists(filename), is(true));
        final IFileStorageFile attachment = storage.getByName(filename);
        assertThat(attachment, notNullValue());
        assertThat(attachment.getName(), is(expectedFilename));
        assertThat(attachment.getRelativeParentPath().toString(), is(normalizePath(expectedLocation)));

        assertThat(storage.getByUuid(attachment.getUUID()), notNullValue(IFileStorageFile.class));
    }

    public static Stream<Arguments> shouldNotCreateDuplicateAttachmentArguments() {
        return Stream.of(arguments("attachment.txt", "attachment.txt"),
            arguments("test/another/attachment.txt", "attachment.txt"),
            arguments("attachment.txt", "test/another/attachment.txt"));
    }

    @ParameterizedTest
    @MethodSource("shouldNotCreateDuplicateAttachmentArguments")
    public void shouldNotCreateDuplicateAttachment(final String firstFile, final String secondFile) throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            storage.createFile(stream, firstFile);
        }

        final FileStorageAlreadyExistsException e = assertThrows(FileStorageAlreadyExistsException.class, () -> {
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, secondFile);
            }
        });
        assertThat(e.getMessageKey(), is("app.euceg.filestorage.alreadyexistfile"));
    }

    @Test
    public void shouldFilenameCaseSensitive() throws IOException {
        final String filenameLower = "attachment.txt";
        final String filename = "Attachment.TXT";
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            storage.createFile(stream, filename);
        }
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            storage.createFile(stream, filenameLower);
        }

        assertThat(storage.exists(filename), is(true));
        assertThat(storage.exists(filenameLower), is(true));

        final IFileStorageFile attachment = storage.getByName(filename);
        assertThat(attachment.getName(), Matchers.is(filename));

        final IFileStorageFile attachmentLower = storage.getByName(filenameLower);
        assertThat(attachmentLower.getName(), Matchers.is(filenameLower));
    }

    @Test
    public void shouldUpdateAttachment() throws IOException {
        final String filename = "attachment.txt";
        final String content = "content";
        final String newContent = "new content";
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        try (InputStream stream = ByteSource.wrap(content.getBytes()).openStream()) {
            storage.createFile(stream, filename);
        }

        IFileStorageFile attachment = storage.getByName(filename);
        try (InputStream stream = attachment.openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), is(content));
        }
        try (InputStream stream = ByteSource.wrap(newContent.getBytes()).openStream()) {
            storage.replace(attachment, stream);
        }
        attachment = storage.getByName(filename);
        try (InputStream stream = attachment.openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), is(newContent));
        }
    }

    @Test
    public void shouldUpdateAttachmentInFolder() throws IOException {
        final String filename = "attachment.txt";
        final String content = "content";
        final String newContent = "new content";
        final String folder = "test";
        final Path path = Path.of(folder, filename);

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        storage.createDirectory(folder);
        try (InputStream stream = ByteSource.wrap(content.getBytes()).openStream()) {
            storage.createFile(stream, path.toString());
        }

        IFileStorageFile attachment = storage.getByName(path.toString());
        try (InputStream stream = attachment.openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), is(content));
        }
        try (InputStream stream = ByteSource.wrap(newContent.getBytes()).openStream()) {
            storage.replace(attachment, stream);
        }
        attachment = storage.getByName(filename);
        try (InputStream stream = attachment.openStream()) {
            assertThat(IOUtils.toString(stream, Charsets.UTF_8), is(newContent));
        }
    }

    @Test
    public void testFileNotExists() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        try {
            storage.getByName("filename");
            fail();
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchfile"));
        }
        try {
            storage.getByUuid(Eucegs.uuid());
            fail();
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchfile"));
        }
    }

    @Test
    public void testFindAll() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        for (int i = 0; i < 10; i++) {
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, "attachment." + i + ".txt");
            }
        }

        // first page
        Page<IFileStorageElement> page = storage.findAll(PageUtils.newRequest(0, 5));
        assertThat(page.getNumberOfElements(), is(5));
        assertThat(page.isFirst(), is(true));
        assertThat(page.getTotalPages(), is(2));

        // the last page
        page = storage.findAll(page.nextPageable());
        assertThat(page.getNumberOfElements(), is(5));
        assertThat(page.isLast(), is(true));

        // test out of scope
        assertThrows(IllegalArgumentException.class, () -> storage.findAll(PageUtils.newRequest(3, 5)));
    }

    @Test
    public void testFindAllWithSearch() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        for (int i = 0; i < 10; i++) {
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, "attachment." + i + ".txt");
            }
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, i + ".attachment.txt");
            }
        }

        // ascending ordering
        Page<IFileStorageElement> page = storage.findAll(PageUtils
                .newRequest(0, 5, Sort.by(Direction.ASC, "name"), new Filters(Filter.contains("name", "2.")), null));
        assertThat(page.getNumberOfElements(), is(2));
        assertThat(page.getContent().stream().map(IFileStorageElement::getName).collect(Collectors.toList()),
            Matchers.contains("2.attachment.txt", "attachment.2.txt"));

        // descending ordering
        page = storage.findAll(PageUtils
                .newRequest(0, 5, Sort.by(Direction.DESC, "name"), new Filters(Filter.contains("name", "2.")), null));
        assertThat(page.getContent().stream().map(IFileStorageElement::getName).collect(Collectors.toList()),
            Matchers.contains("attachment.2.txt", "2.attachment.txt"));

    }

    @Test
    public void testFindByFolder() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        for (int i = 0; i < 10; i++) {
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, "attachment-root." + i + ".txt");
            }
        }

        for (int i = 0; i < 10; i++) {
            try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
                storage.createFile(stream, "test/another/attachment." + i + ".txt");
            }
        }

        // first page
        Page<IFileStorageElement> page = storage.findAll(PageUtils.newRequest(0, 5), true, "test/another");
        assertThat(page.getNumberOfElements(), is(5));
        assertThat(page.isFirst(), is(true));
        assertThat(page.getTotalPages(), is(2));

        // the last page
        page = storage.findAll(page.nextPageable(), true, "test/another");
        assertThat(page.getNumberOfElements(), is(5));
        assertThat(page.isLast(), is(true));

        // test out of scope
        assertThrows(IllegalArgumentException.class, () -> {
            storage.findAll(PageUtils.newRequest(3, 5), true, "test/another");
        });

        page = storage.findAll(PageUtils.newRequest(0, 5), true, "test");
        assertThat(page.getNumberOfElements(), is(1));
        assertThat(page.isFirst(), is(true));
        assertThat(page.getTotalPages(), is(1));

        final IFileStorageElement element = page.getContent().get(0);
        assertThat(element.isDirectory(), is(true));
        assertThat(element.getName(), is("another"));
    }

    @Test
    public void testExistFileInFolder() throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile attachment;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            attachment = storage.createFile(stream, "test/another/attachment.txt");
        }
        assertThat(storage.exists(attachment.getRelativePath().toString()), is(true));
    }

    @Test
    public void testFindByNameInFolder() throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile attachment;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            attachment = storage.createFile(stream, "test/another/attachment.txt");
        }
        assertThat(storage.findByName(attachment.getRelativePath().toString()), notNullValue());
    }

    @Test
    public void testGetDirectories() throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        storage.createDirectory("test");
        storage.createDirectory("test/one");
        storage.createDirectory("test/another");

        assertThat(storage.existsDirectory("test/another"), is(true));
        assertThat(storage.existsDirectory("test/wrong"), is(false));

        List<IFileStorageDirectory> directories = storage.getDirectories("test/one");
        assertThat(directories, notNullValue());
        assertThat(directories.size(), is(0));

        directories = storage.getDirectories("");
        assertThat(directories, notNullValue());
        assertThat(directories.size(), is(1));
        IFileStorageDirectory directory = directories.get(0);
        assertThat(directory.getName(), is("test"));
        assertThat(directory.getRelativeParentPath(), notNullValue());
        assertThat(directory.getRelativeParentPath().toString(), is(""));

        directories = storage.getDirectories("test");
        assertThat(directories, notNullValue());
        assertThat(directories.size(), is(2));

        ;
        directory = directories.get(0);
        assertThat(from(directories).transform(IFileStorageDirectory::getName), containsInAnyOrder("one", "another"));
        assertThat(directory.getRelativeParentPath().toString(), is("test"));

        storage.deleteDirectory("test/another");

        directories = storage.getDirectories("test");
        assertThat(directories, notNullValue());
        assertThat(directories.size(), is(1));
    }

    @Test
    public void testTreeDirectories() {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        storage.createDirectory("test/one");
        storage.createDirectory("test/another");

        final ITreeDirectory tree = storage.walkTreeDirectories();

        assertThat("Parent should be null", tree.getParentPath().isEmpty(), is(true));
        assertThat("Path should be be empty string", tree.getPath().toString(), is(""));
        assertThat("Root should be contains only on child", tree.getChildren().size(), is(1));

        ITreeDirectory child = tree.getChildren().stream().filter(d -> "test".equals(d.getName())).findFirst().get();
        assertThat("Parent should be null", tree.getParentPath().isEmpty(), is(true));
        assertThat("Child should be contains 2 children", child.getChildren().size(), is(2));
        assertThat("The path should be relative and contains 'test'", child.getPath().toString(), is("test"));

        child = child.getChildren().stream().filter(d -> "another".equals(d.getName())).findFirst().get();
        assertThat("Child should contains parent", child.getParentPath().isPresent(), is(true));
        assertThat("The parent should be 'test'", child.getParentPath().get().toString(), is("test"));
        assertThat("Shouldn't contains children", child.getChildren().size(), is(0));
        assertThat("The path should be relative and contains 'test/another'",
            child.getPath().toString(),
            is(normalizePath("test/another")));
    }

    @Test
    public void testDirectoryExists() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        storage.createDirectory("test/folder");

        final IFileStorageDirectory directory = storage.getDirectory("test/folder");

        assertThat(directory, notNullValue());
        assertThat(directory.getName(), is("folder"));
    }

    @Test
    public void testDirectoryNotExists() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        try {
            storage.getDirectory("folder");
            fail();
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchdirectory"));
        }
    }

    @Test
    public void testMoveDirectory() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);
        storage.createDirectory("test/folder");
        storage.createDirectory("another/test2");

        // move in same directory
        IFileStorageDirectory directory = storage.moveTo(storage.getDirectory("test/folder"), "test");
        assertThat("Directory name should be 'folder'", directory.getName(), is("folder"));
        assertThat("Directory path should be 'test/folderr'",
            directory.getRelativePath().toString(),
            is(normalizePath("test/folder")));

        // move directory in another directory
        directory = storage.moveTo(storage.getDirectory("test/folder"), "another/test2");

        assertThat(directory, notNullValue());
        assertThat("Directory should be exist", directory.getFile().exists(), is(true));
        assertThat("Directory name should be 'folder'", directory.getName(), is("folder"));
        assertThat("Directory path should be 'another/test2/folder'",
            directory.getRelativePath().toString(),
            is(normalizePath("another/test2/folder")));

        try {
            storage.getDirectory("test/folder");
            fail("should no longer exist");
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchdirectory"));
        }
    }

    @Test
    public void testMovDirectoryNotExist() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        final IFileStorageDirectory directory = storage.createDirectory("test/folder");

        directory.getFile().delete();

        try {
            storage.moveTo(directory, "another");
            fail("should fail");
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchdirectory"));
        }

    }

    @Test
    public void testMoveFile() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile file = null;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            file = storage.createFile(stream, "test/another/attachment.txt");
        }

        storage.createDirectory("another/test2");

        // move file in same directory
        file = storage.moveTo(file, "test/another");
        assertThat("The relative path has no changed",
            file.getRelativePath().toString(),
            is(normalizePath("test/another/attachment.txt")));

        // move file in another directory
        file = storage.moveTo(file, "another/test2");

        assertThat(file, notNullValue());
        assertThat("The filename should be the same", file.getName(), is("attachment.txt"));
        assertThat("The relative path should be changed",
            file.getRelativePath().toString(),
            is(normalizePath("another/test2/attachment.txt")));

        try {
            storage.getByName("test/another/attachment.txt");
            fail("should no exist anymore");
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchfile"));
        }
    }

    @Test
    public void testMoveFileNotExist() throws IOException {

        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile file = null;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            file = storage.createFile(stream, "attachment.txt");
        }

        file.getFile().delete();

        try {
            storage.moveTo(file, "another/test2");
            fail("should fail");
        } catch (final FileStorageNotFoundException ex) {
            assertThat(ex.getMessageKey(), is("app.euceg.filestorage.nosuchfile"));
        }

    }

    @Test
    public void updateFilename() throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile file = null;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            file = storage.createFile(stream, "test/another/attachment.txt");
        }
        // update with same name
        file = storage.updateFilename(file, "attachment.txt");
        assertThat(file, notNullValue());
        assertThat(file.getName(), is("attachment.txt"));

        file = storage.updateFilename(file, "another.name.txt");
        assertThat(file, notNullValue());
        assertThat(file.getName(), is("another.name.txt"));
    }

    @Test()
    public void shouldDelete() throws IOException {
        final DefaultFileStorage storage = new DefaultFileStorage(settings, i18nService);

        IFileStorageFile file = null;
        try (InputStream stream = ByteSource.wrap("content".getBytes()).openStream()) {
            file = storage.createFile(stream, "test/another/attachment.txt");
        }

        final boolean done = storage.delete(file);

        assertThat(done, is(true));

    }

    private static String normalizePath(final String path) {
        return path.replaceAll("/", Matcher.quoteReplacement(File.separator));
    }
}
