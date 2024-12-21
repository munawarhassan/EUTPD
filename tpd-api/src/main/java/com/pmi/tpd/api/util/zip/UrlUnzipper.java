package com.pmi.tpd.api.util.zip;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.Closeables;


/**
 * <p>
 * UrlUnzipper class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UrlUnzipper extends AbstractUnzipper {

    /** */
    private final URL zipUrl;

    /**
     * <p>
     * Constructor for UrlUnzipper.
     * </p>
     *
     * @param zipUrl
     *            a {@link java.net.URL} object.
     * @param destDir
     *            a {@link java.io.File} object.
     */
    public UrlUnzipper(final URL zipUrl, final File destDir) {
        this.zipUrl = zipUrl;
        this.destDir = destDir;
    }

    /** {@inheritDoc} */
    @Override
    public void unzip() throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipUrl.openStream());

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                saveEntry(zis, zipEntry);
            }
        } finally {
            Closeables.closeQuietly(zis);
        }
    }

    /** {@inheritDoc} */
    @Override
    public File unzipFileInArchive(final String fileName) throws IOException {
        throw new UnsupportedOperationException("Feature not implemented.");
    }

    /** {@inheritDoc} */
    @Override
    public ZipEntry[] entries() throws IOException {
        return entries(new ZipInputStream(zipUrl.openStream()));
    }
}
