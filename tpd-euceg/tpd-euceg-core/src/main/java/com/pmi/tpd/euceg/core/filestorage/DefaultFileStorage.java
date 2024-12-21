package com.pmi.tpd.euceg.core.filestorage;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.filestorage.internal.DirectoriesWalkTreeVisitor;
import com.pmi.tpd.euceg.core.filestorage.internal.DirectoriesWalkTreeVisitor.TreeDirectory;
import com.pmi.tpd.euceg.core.filestorage.internal.FileEntry;
import com.pmi.tpd.euceg.core.filestorage.internal.FileStorageDirectory;
import com.pmi.tpd.euceg.core.filestorage.internal.FileStorageFile;
import com.pmi.tpd.euceg.core.filestorage.internal.QFileEntry;
import com.querydsl.codegen.utils.ECJEvaluatorFactory;
import com.querydsl.codegen.utils.EvaluatorFactory;
import com.querydsl.codegen.utils.JDKEvaluatorFactory;
import com.querydsl.collections.CollQuery;
import com.querydsl.collections.CollQueryTemplates;
import com.querydsl.collections.DefaultEvaluatorFactory;
import com.querydsl.collections.DefaultQueryEngine;
import com.querydsl.collections.QueryEngine;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Default storage implementation for creating and managing attachment files.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Slf4j
public class DefaultFileStorage implements IFileStorage {

    /** */
    private final IApplicationConfiguration settings;

    /** */
    private final I18nService i18nService;

    /** */
    private final boolean unicity = true;

    /** */
    private final boolean caseSensitive = true;

    /** */
    private final QueryEngine queryEngine;

    /** */
    private final PathBuilder<FileEntry> builder;

    /**
     * Create new instance of {@link DefaultFileStorage}.
     *
     * @param settings
     *                    application configuration to use.
     * @param i18nService
     *                    localization service.
     */
    @Inject
    public DefaultFileStorage(@Nonnull final IApplicationConfiguration settings,
            @Nonnull final I18nService i18nService) {
        this.settings = checkNotNull(settings, "settings");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        final DefaultEvaluatorFactory evaluatorFactory = new ExEvaluatorFactory(CollQueryTemplates.DEFAULT);
        this.queryEngine = new DefaultQueryEngine(evaluatorFactory);
        this.builder = new PathBuilder<>(QFileEntry.fileEntry.getType(), QFileEntry.fileEntry.getMetadata());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageFile createFile(@Nonnull final InputStream input, @Nonnull final String file)
            throws IOException {
        checkNotNull(file, "file");
        final Path pathFile = Paths.get(file);
        final String filename = pathFile.getFileName().toString();
        final Path path = pathFile.getParent();
        if (exists(filename)) {
            throw new FileStorageAlreadyExistsException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.alreadyexistfile", filename));
        }
        final String name = generatePhysicalFilename(filename);

        final Path resolvedPath = resolve(path, name);
        Files.copy(input, resolvedPath);
        return createFileStorageFile(resolvedPath.toFile());
    }

    /** {@inheritDoc} */
    @Override
    public IFileStorageFile updateFilename(@Nonnull final IFileStorageFile file, @Nonnull final String filename) {
        checkNotNull(file, "file");
        checkHasText(filename, "filename");
        final File afile = file.getFile();
        final File newFile = afile.getParentFile().toPath().resolve(filename + '.' + file.getUUID()).toFile();
        if (!newFile.equals(afile)) {
            afile.renameTo(newFile);
        }
        return createFileStorageFile(newFile);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageFile replace(@Nonnull final IFileStorageFile file, @Nonnull final InputStream input)
            throws IOException {
        checkNotNull(input, "input");
        delete(checkNotNull(file, "file"));
        final Path afile = settings.getAttachmentsDirectory().resolve(file.getPhysicalRelativePath());

        Files.copy(input, afile, StandardCopyOption.REPLACE_EXISTING);
        return this.getByName(file.getRelativePath().toString());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageFile moveTo(@Nonnull final IFileStorageFile file, @Nonnull final String newParentPath)
            throws IOException {
        checkNotNull(file, "file");
        checkNotNull(newParentPath, "newParentPath");

        if (!file.getFile().exists()) {
            throw new FileStorageNotFoundException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.nosuchfile", file.getName()));
        }

        // same parent path
        if (newParentPath.equals(file.getRelativeParentPath().toString())) {
            return file;
        }

        final Path relativeNewPath = Path.of(newParentPath);
        final Path resolvedNewPath = resolve(relativeNewPath, file.getPhysicalName());
        Files.move(file.getFile().toPath(), resolvedNewPath);
        return this.getByName(relativeNewPath.resolve(file.getName()).toString());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageDirectory moveTo(@Nonnull final IFileStorageDirectory directory,
        @Nonnull final String newParentPath) throws IOException {
        checkNotNull(directory, "directory");
        checkNotNull(newParentPath, "newParentPath");

        if (!this.existsDirectory(directory.getRelativePath().toString())) {
            throw new FileStorageNotFoundException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.nosuchdirectory", directory.getName()));
        }

        // same parent path
        if (newParentPath.equals(directory.getRelativeParentPath().toString())) {
            return directory;
        }

        final Path resolvedPath = resolve(Path.of(newParentPath), null);
        org.apache.commons.io.FileUtils.moveDirectoryToDirectory(directory.getFile(), resolvedPath.toFile(), true);
        return createFileStorageDirectory(resolvedPath.resolve(directory.getName()));
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public IFileStorageFile findByName(@Nonnull final String name) {
        checkNotNull(name, "name");
        List<String> files = null;
        final Path rootPath = settings.getAttachmentsDirectory();
        final Path path = Path.of(name);

        final String searchPath = buildSearchPath(path.getParent());
        try {
            files = getFileNames(rootPath
                    .toFile(),
                searchPath + path.getFileName().toString() + ".*",
                null,
                true,
                caseSensitive);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        if (files.isEmpty()) {
            return null;
        }
        final File file = new File(Iterables.getOnlyElement(files));

        return createFileStorageFile(file);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageFile getByName(@Nonnull final String name) {
        checkNotNull(name, "name");
        final IFileStorageFile file = findByName(name);
        if (file == null) {
            throw new FileStorageNotFoundException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.nosuchfile", name));
        }
        return file;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageFile getByUuid(@Nonnull final String uuid) {
        checkNotNull(uuid, "uuid");
        final IFileStorageFile file = findByUuid(uuid);
        if (file == null) {
            throw new FileStorageNotFoundException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.nosuchfile", uuid));
        }
        return file;
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(@Nonnull final IFileStorageFile attachment) {
        final File file = settings.getAttachmentsDirectory()
                .resolve(checkNotNull(attachment, "attachment").getPhysicalRelativePath())
                .toFile();

        return file.isFile() && file.delete();
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(@Nonnull final String filename) {
        final IFileStorageFile file = findByName(filename);
        return file != null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByUuid(@Nonnull final String uuid) {
        final IFileStorageFile file = findByUuid(uuid);
        return file != null;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Page<IFileStorageElement> findAll(@Nonnull final Pageable pageRequest) {
        return findAll(pageRequest, true, null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Page<IFileStorageElement> findAll(@Nonnull final Pageable pageRequest,
        final boolean getDirectory,
        @Nullable final String pathSearchLocation) {
        checkNotNull(pageRequest, "pageRequest");
        final List<FileEntry> files = getListFiles(pathSearchLocation, true, getDirectory).stream()
                .map(f -> FileEntry.builder().name(f).build())
                .collect(Collectors.toList());
        final QFileEntry entity = QFileEntry.fileEntry;
        final Predicate predicates = DslPagingHelper.createPredicates(pageRequest, entity, (Predicate[]) null);

        final CollQuery<FileEntry> query = DslPagingHelper
                .applyPagination(pageRequest, builder, query().from(entity, files).where(predicates));
        final QueryResults<FileEntry> results = query.fetchResults();

        final long totalElements = results.getTotal();
        return PageUtils.createPage(results.getResults(), pageRequest, totalElements)
                .map(f -> this.createElement(new File(f.getName())));
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public List<IFileStorageDirectory> getDirectories(@Nonnull final String directory) {
        checkNotNull(directory, "directory");
        List<String> files;
        final Path rootPath = settings.getAttachmentsDirectory();

        try {
            files = getFileAndDirectoryNames(rootPath.resolve(directory).toFile(), "*", null, true, true, false, true);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return files.stream()
                .map(Path::of)
                .map(path -> new FileStorageDirectory(path, relativize(path.getParent())))
                .collect(Collectors.toList());

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IFileStorageDirectory getDirectory(@Nonnull final String path) {
        final Path rootPath = settings.getAttachmentsDirectory();
        final Path directoryPath = rootPath.resolve(path);
        if (!directoryPath.toFile().exists()) {
            throw new FileStorageNotFoundException(
                    i18nService.createKeyedMessage("app.euceg.filestorage.nosuchdirectory", path));
        }
        return createFileStorageDirectory(directoryPath);
    }

    @Override
    public ITreeDirectory walkTreeDirectories() {

        final Path rootPath = settings.getAttachmentsDirectory();
        final TreeDirectory rootDirectories = new TreeDirectory(relativize(rootPath), null);
        try {

            Files.walkFileTree(rootPath, new DirectoriesWalkTreeVisitor(rootDirectories, this::relativize));
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return rootDirectories;

    }

    /** {@inheritDoc} */
    @Override
    public IFileStorageDirectory createDirectory(@Nonnull final String parentPath, @Nonnull final String directory) {
        Assert.state(REGEX_VALIDATION_PATH.matcher(checkNotNull(parentPath, "parentPath")).matches(),
            "should have valid path");
        Assert.state(REGEX_VALIDATION_DIRECTORY.matcher(checkNotNull(directory, "directory")).matches(),
            "should have valid directory name");
        final Path parent = Path.of(parentPath);
        Assert.state(!parent.isAbsolute(), "path should be relative");
        final Path rootPath = settings.getAttachmentsDirectory();
        final Path path = rootPath.resolve(parent).resolve(directory);
        final File f = path.toFile();
        if (!f.exists() && f.mkdirs()) {
            return createFileStorageDirectory(path);
        }
        throw new FileStorageAlreadyExistsException(
                i18nService.createKeyedMessage("app.euceg.filestorage.alreadyexistdirectory", directory));
    }

    /** {@inheritDoc} */
    @Override
    public IFileStorageDirectory createDirectory(@Nonnull final String path) {
        final Path apath = Path.of(path);
        final Path parent = apath.getParent();
        return createDirectory(parent == null ? "" : parent.toString(), apath.getFileName().toString());
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteDirectory(final String name) {
        final Path rootPath = settings.getAttachmentsDirectory();
        final Path path = rootPath.resolve(name);
        final File f = path.toFile();
        if (f.exists() && f.isDirectory()) {
            return f.delete();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsDirectory(@Nonnull final String path) {
        checkNotNull(path, "path");
        final Path rootPath = settings.getAttachmentsDirectory();
        return rootPath.resolve(path).toFile().exists();
    }

    @Override
    public IFileStorageDirectory renameDirectory(@Nonnull final IFileStorageDirectory directory,
        @Nonnull final String newName) {
        checkNotNull(directory, "directory");
        Assert.state(REGEX_VALIDATION_DIRECTORY.matcher(checkNotNull(newName, "newName")).matches(),
            "should have a valid name");
        final File file = directory.getFile();
        final Path resolvedPath = file.toPath();
        final Path newPath = resolvedPath.resolveSibling(newName);
        file.renameTo(newPath.toFile());
        return createFileStorageDirectory(newPath);
    }

    /**
     * Find the {@link IAttachment} with the specific {@code uuid}.
     *
     * @param uuid
     *             the uuid to find.
     * @return Returns a new instance of {@link IAttachment} corresponding to specific {@code uuid}, otherwise
     *         {@code null}.
     */
    @Nullable
    public IFileStorageFile findByUuid(@Nonnull final String uuid) {
        checkNotNull(uuid, "uuid");
        List<String> files = null;
        final Path rootPath = settings.getAttachmentsDirectory();
        try {
            files = getFileNames(rootPath.toFile(), "**/*." + uuid, null, true, caseSensitive);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        if (files.isEmpty()) {
            return null;
        }
        final File file = new File(Iterables.getOnlyElement(files));

        return createFileStorageFile(file);

    }

    /**
     * Save the {@code stream} in temporary file with {@code filename}.
     *
     * @param stream
     *                 the stream to store.
     * @param filename
     *                 the file name to use.
     * @return Returns new instance of {@link IAttachment} corresponding to {@code stream} and {@code filename}.
     * @throws FileNotFoundException
     *                               if the file exists but is a directory rather than a regular file, does not exist
     *                               but cannot be created, or cannot be opened for any other reason.
     * @throws IOException
     *                               if an I/O error occurs.
     */
    public IFileStorageFile saveTemporary(@Nonnull final InputStream stream, @Nonnull final String filename)
            throws FileNotFoundException, IOException {
        checkNotNull(stream, "stream");
        checkNotNull(filename, "filename");
        final Path rootPath = settings.getAttachmentsDirectory();
        final Path file = rootPath.resolve(generatePhysicalFilename(checkHasText(filename, "filename")));
        Files.copy(stream, file);
        return createFileStorageFile(file.toFile());
    }

    protected CollQuery<FileEntry> query() {
        return new CollQuery<>(queryEngine);
    }

    private static String generatePhysicalFilename(@Nonnull final String filename) {
        checkNotNull(filename, "filename");
        return filename + "." + Eucegs.uuid();
    }

    private List<String> getListFiles(@Nullable final String pathSearchLocation,
        final boolean getFile,
        final boolean getDirectory) {
        List<String> files;
        final Path rootPath = settings.getAttachmentsDirectory();
        try {
            final String searchPath = buildSearchPath(pathSearchLocation != null ? Path.of(pathSearchLocation) : null);
            files = getFileAndDirectoryNames(rootPath
                    .toFile(),
                searchPath + "*",
                null,
                true,
                caseSensitive,
                getFile,
                getDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return files;
    }

    /**
     * Return a list of files as String depending options.
     * <p>
     * <b>Note:</b> copy function of {@link FileUtils#getFileNames(File, String, String, boolean, boolean)}. removed ','
     * split character.
     * </p>
     *
     * @param directory
     *                        the directory to scan
     * @param includes
     *                        the includes pattern, comma separated
     * @param excludes
     *                        the excludes pattern, comma separated
     * @param includeBasedir
     *                        true to include the base dir in each String of file
     * @param isCaseSensitive
     *                        true if case sensitive
     * @return a list of files as String
     * @throws IOException
     *                     if any
     */
    private static List<String> getFileNames(final File directory,
        final String includes,
        final String excludes,
        final boolean includeBasedir,
        final boolean isCaseSensitive) throws IOException {
        return getFileAndDirectoryNames(directory, includes, excludes, includeBasedir, isCaseSensitive, true, false);
    }

    /**
     * Return a list of files as String depending options.
     * <p>
     * <b>Note:</b> copy function of
     * {@link FileUtils#getFileAndDirectoryNames(File, String, String, boolean, boolean, boolean, boolean)}. removed ','
     * split character.
     * </p>
     *
     * @param directory
     *                        the directory to scan
     * @param includes
     *                        the includes pattern, comma separated
     * @param excludes
     *                        the excludes pattern, comma separated
     * @param includeBasedir
     *                        true to include the base dir in each String of file
     * @param isCaseSensitive
     *                        true if case sensitive
     * @param getFiles
     *                        true if get files
     * @param getDirectories
     *                        true if get directories
     * @return a list of files as String
     * @throws IOException
     *                     if any
     */
    private static List<String> getFileAndDirectoryNames(final File directory,
        final String includes,
        final String excludes,
        final boolean includeBasedir,
        final boolean isCaseSensitive,
        final boolean getFiles,
        final boolean getDirectories) throws IOException {
        final DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(directory);

        if (includes != null) {
            scanner.setIncludes(new String[] { includes });
        }

        if (excludes != null) {
            scanner.setExcludes(new String[] { excludes });
        }

        scanner.setCaseSensitive(isCaseSensitive);

        scanner.scan();

        final List<String> list = new ArrayList<>();

        if (getFiles) {
            final String[] files = scanner.getIncludedFiles();

            for (final String file : files) {
                if (includeBasedir) {
                    list.add(directory + File.separator + file);
                } else {
                    list.add(file);
                }
            }
        }

        if (getDirectories) {
            final String[] directories = scanner.getIncludedDirectories();

            for (final String directory1 : directories) {
                if (includeBasedir) {
                    list.add(directory + File.separator + directory1);
                } else {
                    list.add(directory1);
                }
            }
        }

        return list;
    }

    private IFileStorageElement createElement(final File file) {
        if (file.isDirectory()) {
            return createFileStorageDirectory(file.toPath());
        } else {
            return createFileStorageFile(file);
        }
    }

    private IFileStorageFile createFileStorageFile(final File file) {
        return new FileStorageFile(file, relativize(file.toPath().getParent()));
    }

    private FileStorageDirectory createFileStorageDirectory(final Path path) {
        return new FileStorageDirectory(path, relativize(path.getParent()));
    }

    private String buildSearchPath(final Path pathSearchLocation) {
        String searchPath = unicity ? "**/" : "";
        if (unicity) {
            if (pathSearchLocation != null) {
                searchPath = pathSearchLocation.toString();
            }
            if (!searchPath.endsWith("/")) {
                searchPath += "/";
            }
        }
        return searchPath;
    }

    @Nonnull
    private Path relativize(@Nullable final Path relativePath) {
        final Path rootPath = settings.getAttachmentsDirectory();
        Path locatinPath = rootPath;
        if (relativePath != null) {
            locatinPath = rootPath.resolve(relativePath);
        }
        final Path relatived = rootPath.relativize(locatinPath);
        return relatived;
    }

    private Path resolve(@Nullable final Path relativePath, @Nullable final String filename) {
        final Path rootPath = settings.getAttachmentsDirectory();
        Path locationPath = rootPath;
        if (relativePath != null) {
            locationPath = rootPath.resolve(relativePath);
        }
        if (!locationPath.toFile().exists()) {
            locationPath.toFile().mkdirs();
        }
        if (filename != null) {
            return locationPath.resolve(filename);
        }
        return locationPath;
    }

    private static class ExEvaluatorFactory extends DefaultEvaluatorFactory {

        public static CompilerOptions getDefaultEcjCompilerOptions() {
            final String javaSpecVersion = "1.9";
            final Map<String, String> settings = new HashMap<>();
            settings.put(CompilerOptions.OPTION_Source, javaSpecVersion);
            settings.put(CompilerOptions.OPTION_TargetPlatform, javaSpecVersion);
            settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
            return new CompilerOptions(settings);
        }

        public ExEvaluatorFactory(final CollQueryTemplates templates) {
            this(templates, Thread.currentThread().getContextClassLoader() != null
                    ? Thread.currentThread().getContextClassLoader() : DefaultEvaluatorFactory.class.getClassLoader());
        }

        protected ExEvaluatorFactory(final CollQueryTemplates templates, final ClassLoader classLoader) {
            super(templates, createEvaluatorFactory(classLoader));

        }

        private static EvaluatorFactory createEvaluatorFactory(final ClassLoader classLoader) {
            final JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
            EvaluatorFactory factory = null;
            if (classLoader instanceof URLClassLoader && systemJavaCompiler != null) {
                factory = new JDKEvaluatorFactory((URLClassLoader) classLoader, systemJavaCompiler);
            } else {
                // for OSGi and JRE compatibility
                factory = new ECJEvaluatorFactory(classLoader, getDefaultEcjCompilerOptions());
            }
            return factory;
        }

    }

}
