package com.pmi.tpd.web.logback;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.api.ApplicationConstants.Directories;
import com.pmi.tpd.testing.junit5.TestCase;

import ch.qos.logback.core.Context;

public class LogDirectoryPropertyDefinerTest extends TestCase {

    public Path temporaryFolder;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        temporaryFolder = path;
    }

    @Test
    public void testGetPropertyValue() throws Exception {
        final File home = temporaryFolder.resolve("home").toFile();
        home.mkdir();

        final Context context = mock(Context.class);
        when(context.getProperty(eq("home.dir"))).thenReturn(home.getAbsolutePath());

        final LogDirectoryPropertyDefiner definer = new LogDirectoryPropertyDefiner();
        definer.setContext(context);
        assertEquals(new File(home, Directories.LOG_DIRECTORY).getAbsolutePath(), definer.getPropertyValue());
    }

    @Test
    public void testGetPropertyValueOccludedByFile() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            final File home = temporaryFolder.resolve("home").toFile();
            home.mkdir();
            Files.asCharSink(new File(home, Directories.LOG_DIRECTORY), Charsets.UTF_8).write("t");

            final Context context = mock(Context.class);
            when(context.getProperty(eq("home.dir"))).thenReturn(home.getAbsolutePath());

            final LogDirectoryPropertyDefiner definer = new LogDirectoryPropertyDefiner();
            definer.setContext(context);
            definer.getPropertyValue();
        });

    }

    @Test
    public void testGetPropertyValueWithNoHome() {
        assertThrows(IllegalStateException.class, () -> {
            final Context context = mock(Context.class);

            final LogDirectoryPropertyDefiner definer = new LogDirectoryPropertyDefiner();
            definer.setContext(context);
            definer.getPropertyValue();
        });

    }
}
