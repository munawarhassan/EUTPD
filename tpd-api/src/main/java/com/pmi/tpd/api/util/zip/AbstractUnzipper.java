package com.pmi.tpd.api.util.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.util.FileUtils;

/**
 * <p>
 * Abstract AbstractUnzipper class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class AbstractUnzipper implements IUnzipper {

    /** */
    protected static final Logger LOGGER = LoggerFactory.getLogger(FileUnzipper.class);

    /** */
    protected File destDir;

    /**
     * <p>
     * saveEntry.
     * </p>
     *
     * @param is
     *            a {@link java.io.InputStream} object.
     * @param entry
     *            a {@link java.util.zip.ZipEntry} object.
     * @throws java.io.IOException
     *             if any.
     * @return a {@link java.io.File} object.
     */
    protected File saveEntry(final InputStream is, final ZipEntry entry) throws IOException {
        final File file = new File(destDir, entry.getName());

        if (entry.isDirectory()) {
            file.mkdirs();
        } else {
            final File dir = new File(file.getParent());
            dir.mkdirs();

            Files.copy(is, file.toPath());
        }
        file.setLastModified(entry.getTime());

        return file;
    }

    /**
     * <p>
     * entries.
     * </p>
     *
     * @param zis
     *            a {@link java.util.zip.ZipInputStream} object.
     * @throws java.io.IOException
     *             if any.
     * @return an array of {@link java.util.zip.ZipEntry} objects.
     */
    protected ZipEntry[] entries(final ZipInputStream zis) throws IOException {
        final List<ZipEntry> entries = Lists.newArrayList();
        try {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                entries.add(zipEntry);
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        } finally {
            Closeables.closeQuietly(zis);
        }

        return entries.toArray(new ZipEntry[entries.size()]);
    }

    /** {@inheritDoc} */
    @Override
    public void conditionalUnzip() throws IOException {
        final Map<String, Long> zipContentsAndLastModified = new HashMap<>();

        final ZipEntry[] zipEntries = entries();
        for (final ZipEntry zipEntrie : zipEntries) {
            zipContentsAndLastModified.put(zipEntrie.getName(), zipEntrie.getTime());
        }

        // If the jar contents of the directory does not match the contents of the zip
        // The we will nuke the bundled plugins directory and re-extract.
        final Map<String, Long> targetDirContents = getContentsOfTargetDir(destDir);
        if (!targetDirContents.equals(zipContentsAndLastModified)) {
            FileUtils.deleteDirectory(destDir.toPath());
            unzip();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Target directory contents match zip contents. Do nothing.");
            }
        }
    }

    private Map<String, Long> getContentsOfTargetDir(final File dir) throws IOException {

        if (!dir.isDirectory()) {
            return Collections.emptyMap();
        }

        final Map<String, Long> targetDirContents = new HashMap<>();
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File child : files) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Examining entry in zip: " + child);
                }
                targetDirContents.put(child.getName(), child.lastModified());
            }
        }

        return targetDirContents;
    }
}
