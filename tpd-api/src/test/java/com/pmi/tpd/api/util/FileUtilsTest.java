package com.pmi.tpd.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilsTest {

    public static Path folder;

    @BeforeAll
    public static void start(@TempDir final Path tempDir) {
        folder = tempDir;
    }

    @Test
    public void testCreateTempDirWithInvalidPrefix() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.createTempDir("a", null, folder.toFile());
        });

    }

    @Test
    public void testCreateTempDirWithInvalidParent() throws Exception {
        Assertions.assertThrows(IOException.class, () -> {
            FileUtils.createTempDir("foo", ".tmp", folder.resolve("invalid").toFile());
        });

    }

    @Test
    public void testCreateTempDirTwiceWithSamePrefixCreatesTwoDirectories() throws Exception {
        final File a = FileUtils.createTempDir("foo", "", folder.toFile());
        final File b = FileUtils.createTempDir("foo", "", folder.toFile());
        assertFalse(a.equals(b), "Same file");
    }

    @Test
    public void testConstruct() throws Exception {
        assertEquals(new File("a", "b").getAbsoluteFile(), FileUtils.construct(new File("a"), "b").getAbsoluteFile());
        assertEquals(new File(new File("a", "b"), "c").getAbsoluteFile(),
            FileUtils.construct(new File("a"), "b", "c").getAbsoluteFile());
    }
}
