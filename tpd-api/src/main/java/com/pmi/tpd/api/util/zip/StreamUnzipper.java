package com.pmi.tpd.api.util.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

/**
 * Stream based ZIP extractor.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class StreamUnzipper extends AbstractUnzipper {

    /** */
    private final ZipInputStream zis;

    /**
     * Construct a stream unzipper.
     *
     * @param zipStream
     *            Inputstream to use for ZIP archive reading
     * @param destDir
     *            Directory to unpack stream contents
     */
    public StreamUnzipper(final InputStream zipStream, final File destDir) {
        if (zipStream == null) {
            throw new IllegalArgumentException("zip stream cannot be null");
        }
        this.zis = new ZipInputStream(zipStream);
        this.destDir = destDir;
    }

    /** {@inheritDoc} */
    @Override
    public void unzip() throws IOException {
        ZipEntry zipEntry = zis.getNextEntry();
        try {
            while (zipEntry != null) {
                saveEntry(zis, zipEntry);
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        } finally {
            Closeables.close(zis, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public File unzipFileInArchive(final String fileName) throws IOException {
        File result = null;

        try {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();

                // os-dependent zips contain a leading back slash "\" character. we want to strip this off first
                if (!Strings.isNullOrEmpty(entryName) && entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }

                if (fileName.equals(entryName)) {
                    result = saveEntry(zis, zipEntry);
                    break;
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        } finally {
            Closeables.closeQuietly(zis);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public ZipEntry[] entries() throws IOException {
        return entries(zis);
    }
}
