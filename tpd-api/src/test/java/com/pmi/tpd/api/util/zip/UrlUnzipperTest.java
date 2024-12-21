package com.pmi.tpd.api.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.testing.junit5.TestCase;

public class UrlUnzipperTest extends TestCase {

    private static final long MILLISECOND = 10000;

    private File destdir;

    private File zip;

    private File sourcedir;

    private File source1;

    @BeforeEach
    public void forEach(@TempDir final Path tempDir) throws IOException {
        final File basedir = tempDir.toFile();
        destdir = new File(basedir, "dest");
        destdir.mkdir();
        zip = new File(basedir, "test.zip");
        sourcedir = new File(basedir, "source");
        sourcedir.mkdir();
        source1 = new File(sourcedir, "source1.jar");
        Files.asCharSink(source1, Charsets.UTF_8).write("source1");
        source1.setLastModified(source1.lastModified() - 100000);
    }

    @Test
    public void conditionalUnzip() throws IOException, InterruptedException {
        zip(sourcedir, zip);
        UrlUnzipper unzipper = new UrlUnzipper(zip.toURI().toURL(), destdir);
        unzipper.conditionalUnzip();

        assertEquals(1, destdir.listFiles().length);
        File dest1 = destdir.listFiles()[0];
        assertEquals(source1.lastModified() / MILLISECOND, dest1.lastModified() / MILLISECOND);
        assertEquals("source1", Files.asCharSource(dest1, Charsets.UTF_8).read());

        Files.asCharSink(source1, Charsets.UTF_8).write("source1-modified");
        zip(sourcedir, zip);
        unzipper = new UrlUnzipper(zip.toURI().toURL(), destdir);
        unzipper.conditionalUnzip();

        assertEquals(1, destdir.listFiles().length);
        dest1 = destdir.listFiles()[0];
        assertEquals(source1.lastModified() / MILLISECOND, dest1.lastModified() / MILLISECOND);
        assertEquals("source1-modified", Files.asCharSource(dest1, Charsets.UTF_8).read());

    }

    @Test
    public void conditionalUnzipWithNoUnzipIfNoFileMod() throws IOException, InterruptedException {
        zip(sourcedir, zip);
        UrlUnzipper unzipper = new UrlUnzipper(zip.toURI().toURL(), destdir);
        unzipper.conditionalUnzip();

        assertEquals(1, destdir.listFiles().length);
        File dest1 = destdir.listFiles()[0];
        assertEquals(source1.lastModified() / MILLISECOND, dest1.lastModified() / MILLISECOND);
        assertEquals("source1", Files.asCharSource(dest1, Charsets.UTF_8).read());

        final long ts = source1.lastModified();
        Files.asCharSink(source1, Charsets.UTF_8).write("source1-modified");
        source1.setLastModified(ts);
        zip(sourcedir, zip);
        unzipper = new UrlUnzipper(zip.toURI().toURL(), destdir);
        unzipper.conditionalUnzip();

        assertEquals(1, destdir.listFiles().length);
        dest1 = destdir.listFiles()[0];
        assertEquals(source1.lastModified() / MILLISECOND, dest1.lastModified() / MILLISECOND);
        assertEquals("source1", Files.asCharSource(dest1, Charsets.UTF_8).read());

    }

    private void zip(final File basedir, final File destfile) throws IOException {
        final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(destfile));
        for (final File child : basedir.listFiles()) {
            final ZipEntry entry = new ZipEntry(child.getName());
            entry.setTime(child.lastModified());
            zout.putNextEntry(entry);
            final FileInputStream input = new FileInputStream(child);
            java.nio.file.Files.copy(child.toPath(), zout);
            input.close();

            // not sure why this is necessary...
            child.setLastModified(entry.getTime());
        }
        zout.close();
    }
}
