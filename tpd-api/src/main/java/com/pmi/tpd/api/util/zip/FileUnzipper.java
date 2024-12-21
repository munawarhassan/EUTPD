package com.pmi.tpd.api.util.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * <p>
 * FileUnzipper class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class FileUnzipper extends AbstractUnzipper {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUnzipper.class);

    /** */
    private final File zipFile;

    /** */
    private final File destDir;

    /**
     * <p>
     * Constructor for FileUnzipper.
     * </p>
     *
     * @param zipFile
     *            a {@link java.io.File} object.
     * @param destDir
     *            a {@link java.io.File} object.
     */
    public FileUnzipper(final File zipFile, final File destDir) {
        this.zipFile = zipFile;
        this.destDir = destDir;
    }

    /**
     * {@inheritDoc} Unzips all files in the archive.
     */
    @Override
    public void unzip() throws IOException {
        if (zipFile == null || !zipFile.isFile()) {
            return;
        }

        getStreamUnzipper().unzip();
    }

    /** {@inheritDoc} */
    @Override
    public ZipEntry[] entries() throws IOException {
        return getStreamUnzipper().entries();
    }

    /**
     * {@inheritDoc} Specify a specific file inside the archive to extract.
     */
    @Override
    public File unzipFileInArchive(final String fileName) throws IOException {
        File result = null;

        if (zipFile == null || !zipFile.isFile() || Strings.isNullOrEmpty(fileName)) {
            return result;
        }

        result = getStreamUnzipper().unzipFileInArchive(fileName);

        if (result == null) {
            LOGGER.error("The file: " + fileName + " could not be found in the archive: " + zipFile.getAbsolutePath());
        }

        return result;
    }

    private StreamUnzipper getStreamUnzipper() throws FileNotFoundException {
        return new StreamUnzipper(new BufferedInputStream(new FileInputStream(zipFile)), destDir);
    }

}
