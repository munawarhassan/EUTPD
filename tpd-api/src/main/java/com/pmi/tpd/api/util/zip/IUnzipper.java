package com.pmi.tpd.api.util.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * <p>
 * IUnzipper interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUnzipper {

    /**
     * <p>
     * unzip.
     * </p>
     *
     * @throws java.io.IOException
     *             if any.
     */
    void unzip() throws IOException;

    /**
     * <p>
     * conditionalUnzip.
     * </p>
     *
     * @throws java.io.IOException
     *             if any.
     */
    void conditionalUnzip() throws IOException;

    /**
     * <p>
     * unzipFileInArchive.
     * </p>
     *
     * @param fileName
     *            a {@link java.lang.String} object.
     * @throws java.io.IOException
     *             if any.
     * @throws java.io.FileNotFoundException
     *             if any.
     * @return a {@link java.io.File} object.
     */
    File unzipFileInArchive(String fileName) throws IOException, FileNotFoundException;

    /**
     * <p>
     * entries.
     * </p>
     *
     * @throws java.io.IOException
     *             if any.
     * @return an array of {@link java.util.zip.ZipEntry} objects.
     */
    ZipEntry[] entries() throws IOException;
}
