package com.pmi.tpd.euceg.core.filestorage.internal;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.codehaus.plexus.util.NioFiles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;

/**
 * Default implementation of interface {@link IFileStorageFile}.
 *
 * @author friederich christophe
 * @since 3.0
 */
public class FileStorageFile extends ByteSource implements IFileStorageFile {

    /** */
    private final File file;

    /** */
    private final boolean temporary;

    /** */
    private final String name;

    /** */
    private final Path relativeParentPath;

    /** */
    private final String uuid;

    private String mimeType;

    /**
     * Create {@link FileStorageFile} for the associated to {@code file}.
     *
     * @param file
     *            the file.
     * @param parentPath
     *            the parent path relative to root physical path.
     */
    public FileStorageFile(@Nonnull final File file, @Nonnull final Path parentPath) {
        this(file, parentPath, false);
    }

    /**
     * Create {@link FileStorageFile} for the associated to {@code file}.
     *
     * @param file
     *            the file.
     * @param parentPath
     *            the parent path relative to root physical path.
     * @param temporary
     *            indicating if file is temporary.
     */
    public FileStorageFile(@Nonnull final File file, @Nonnull final Path parentPath, final boolean temporary) {
        state(!checkNotNull(parentPath, "parentPath").isAbsolute(), "should be relative");
        this.file = checkNotNull(file, "file");
        this.relativeParentPath = parentPath;
        this.name = extractName(file.getName());
        this.uuid = extractUUID(file.getName());
        this.temporary = temporary;
        try {
            mimeType = java.nio.file.Files.probeContentType(getRelativePath());
        } catch (final IOException e) {
            // noop
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Optional<String> getMimeType() {
        return Optional.ofNullable(this.mimeType);
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    /** {@inheritDoc} */
    @Override
    public long getModified() {
        try {
            return NioFiles.getLastModified(file);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getPhysicalName() {
        return file.getName();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Path getRelativeParentPath() {
        return this.relativeParentPath;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Path getRelativePath() {
        if (relativeParentPath != null) {
            return relativeParentPath.resolve(getName());
        }
        return Path.of(getName());
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Path getPhysicalRelativePath() {
        if (relativeParentPath != null) {
            return relativeParentPath.resolve(getPhysicalName());
        }
        return Path.of(getPhysicalName());
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getUUID() {
        return uuid;
    }

    /** {@inheritDoc} */
    @Override
    public long getSize() {
        return file.length();
    }

    /** {@inheritDoc} */
    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(file);
    }

    /** {@inheritDoc} */
    @Override
    @JsonIgnore
    public boolean isTemporary() {
        return temporary;
    }

    private String extractUUID(final String filename) {
        return Files.getFileExtension(filename);
    }

    private String extractName(final String filename) {
        return Files.getNameWithoutExtension(filename);
    }
}
