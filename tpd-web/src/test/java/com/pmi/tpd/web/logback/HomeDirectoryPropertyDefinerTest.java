package com.pmi.tpd.web.logback;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.testing.junit5.TestCase;

public class HomeDirectoryPropertyDefinerTest extends TestCase {

    public Path temporaryFolder;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        temporaryFolder = path;
    }

    @Test
    public void testGetPropertyValue() throws Exception {
        final File home = temporaryFolder.resolve("home").toFile();
        home.mkdir();

        final HomeDirectoryPropertyDefiner propertyDefiner = newHomeDirectoryPropertyDefiner(home);
        assertEquals(home.getAbsolutePath(), propertyDefiner.getPropertyValue());
    }

    @Test
    public void testGetPropertyValueResolvingHomeToFile() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            final File temp = temporaryFolder.resolve("home").toFile();
            temp.mkdir();
            final File home = new File(temp, "tpd-home");
            Files.asCharSink(home, Charsets.UTF_8).write("t");

            final HomeDirectoryPropertyDefiner propertyDefiner = newHomeDirectoryPropertyDefiner(home);
            propertyDefiner.getPropertyValue();
        });
    }

    private static HomeDirectoryPropertyDefiner newHomeDirectoryPropertyDefiner(final File dir) {
        return new HomeDirectoryPropertyDefiner() {

            @Override
            protected File resolveHomeDirectory() {
                return dir;
            }
        };
    }
}
