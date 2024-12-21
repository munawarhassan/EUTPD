package com.pmi.tpd.core.avatar.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.http.MediaType;

import com.google.common.io.Files;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.util.FileUtils;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.core.avatar.AbstractAvatarSupplier;
import com.pmi.tpd.core.avatar.AvatarStoreException;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.core.avatar.UnsupportedAvatarException;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.ResourceAvatarSupplier;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DiskAvatarRepositoryTest extends MockitoTestCase {

    public Path folder;

    private DiskAvatarRepository repository;

    @Mock
    private IApplicationConfiguration applicationConfiguration;

    @Mock
    private IGlobalApplicationProperties applicationProperties;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        this.folder = path;
        when(applicationConfiguration.getDataDirectory()).thenReturn(path);

        repository = new DiskAvatarRepository(new SimpleI18nService(), applicationConfiguration, applicationProperties);
        repository.setMaxDimension(1024);
        repository.setMaxSize(1024 * 1024); // 1MB
    }

    @Test
    public void testDelete() throws Exception {
        final File typeDir = FileUtils
                .construct(folder.toFile(), DiskAvatarRepository.AVATARS_PATH, AvatarType.USER.getDirectoryName());
        final File instanceDir = FileUtils.mkdir(typeDir, "1");
        assertTrue(instanceDir.isDirectory(), "The instance directory could not be created");

        Files.touch(new File(instanceDir, "48.png"));
        Files.touch(new File(instanceDir, "64.png"));
        Files.touch(new File(instanceDir, "96.png"));
        Files.touch(new File(instanceDir, "128.png"));
        Files.touch(new File(instanceDir, "256.png"));
        assertEquals(5, instanceDir.listFiles().length);

        repository.delete(AvatarType.USER, 1L);
        assertFalse(instanceDir.exists(), "The entire instance directory, and its contents, should have been removed");
    }

    @Test
    public void testDeleteNonexistentInstance() {
        final File avatarsDir = FileUtils.construct(folder.toFile(), DiskAvatarRepository.AVATARS_PATH);
        final File typeDir = FileUtils.mkdir(avatarsDir, AvatarType.USER.getDirectoryName());
        assertTrue(typeDir.isDirectory(), "The type directory could not be created");

        repository.delete(AvatarType.USER, 1L);
        assertTrue(typeDir.isDirectory(), "The type directory should not have been removed");
    }

    @Test
    public void testDeleteNonexistentType() {
        final File typeDir = FileUtils
                .construct(folder.toFile(), DiskAvatarRepository.AVATARS_PATH, AvatarType.USER.getDirectoryName());
        assertFalse(typeDir.exists(), "The type directory should not already exist or it invalidates this test");

        repository.delete(AvatarType.USER, 1L);
        assertFalse(typeDir.exists(), "The type directory should not have been created");
    }

    @Test
    public void testDeleteWithNullId() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            repository.delete(AvatarType.USER, null);
        });
    }

    @Test
    public void testDeleteWithNullType() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            repository.delete(null, 1L);
        });
    }

    @Test
    public void testLoadDefault() {
        for (final AvatarType type : AvatarType.values()) {
            assertNotNull(repository.loadDefault(type, -1), "No default was returned for " + type);
        }
    }

    @Test
    public void testStoreAndLoad() throws Exception {
        final File original = fileFor(AvatarType.USER, 1, "original.png");
        assertFalse(original.isFile(), "The original avatar file already exists; this test is invalid!");

        // First: Store a new avatar
        store("avatars/user/256.png");
        assertStored(original);

        // Second: Retrieve the _original_ avatar. This should not apply any resizing
        ICacheableAvatarSupplier supplier = repository.load(AvatarType.USER, 1L, DiskAvatarRepository.ORIGINAL_SIZE);
        assertNotNull(supplier);
        assertEquals(MediaType.IMAGE_PNG_VALUE, supplier.getContentType());
        assertEquals(original.lastModified(), supplier.getTimestamp());

        InputStream stream = supplier.open();
        assertNotNull(stream);
        stream.close();

        // Third: Retrieve the avatar in a specific size, to verify that
        final File sized = fileFor(AvatarType.USER, 1, "128.png");
        assertFalse(sized.isFile(), "The sized avatar file already exists; this test is invalid!");

        supplier = repository.load(AvatarType.USER, 1L, 128);
        assertNotNull(supplier);
        assertEquals(MediaType.IMAGE_PNG_VALUE, supplier.getContentType());
        assertEquals(sized.lastModified(), supplier.getTimestamp());
        assertTrue( // Allow equality to try and prevent the test
            supplier.getTimestamp() >= original.lastModified(),
            "The sized avatar should be newer than the original"); // from being flakey on fast systems

        stream = supplier.open();
        assertNotNull(stream);

        // In addition to verifying that the supplier works as expected, verify the
        // resizing worked
        final BufferedImage avatar = ImageIO.read(stream);
        assertEquals(128, avatar.getHeight(), "The sized avatar does not have the requested height");
        assertEquals(128, avatar.getWidth(), "The sized avatar does not have the requested width");

        assertEquals(original.lastModified(), repository.getVersionId(AvatarType.USER, 1L));
        stream.close();
    }

    @Test
    public void testStoreThrowsWhenAvatarIsOversized() throws Exception {
        assertThrows(AvatarStoreException.class, () -> {
            // Bypass the setter; it applies a minimum value that makes this test harder to
            // write
            new DirectFieldAccessor(repository).setPropertyValue("maxSize", 1024L);

            store("avatars/project/blue/256.png");
        });

    }

    @Test
    public void testStoreThrowsWhenSupplierReturnsNullStream() throws Exception {
        assertThrows(AvatarStoreException.class, () -> {
            final IAvatarSupplier supplier = mock(IAvatarSupplier.class);

            try {
                repository.store(AvatarType.USER, 1L, supplier);
            } finally {
                verify(supplier).open();
            }
        });
    }

    @Test
    public void testLoadNonExistentFileHasFallback() throws Exception {
        final ICacheableAvatarSupplier supplier = repository
                .load(AvatarType.USER, 1L, DiskAvatarRepository.ORIGINAL_SIZE);
        assertNotNull(supplier);
        assertEquals(com.google.common.net.MediaType.PNG.toString(), supplier.getContentType());
        assertEquals(-1, supplier.getTimestamp()); // no file -> fallback -> -1 for eternal file

        final InputStream stream = supplier.open(); // without fallback, this fails
        assertNotNull(stream);
        stream.close();
    }

    @Test
    public void testStoreThrowsWhenSupplierThrows() throws Exception {
        assertThrows(AvatarStoreException.class, () -> {
            final IAvatarSupplier supplier = mock(IAvatarSupplier.class);
            doThrow(IOException.class).when(supplier).open();

            try {
                repository.store(AvatarType.USER, 1L, supplier);
            } finally {
                verify(supplier).open();
            }
        });
    }

    @Test
    public void testStoreWithNonImage() {
        assertThrows(UnsupportedAvatarException.class, () -> {
            store("logback-test.xml");
        });
    }

    @Test
    public void testStoreWithNonImageAndFalseContentType() {
        assertThrows(UnsupportedAvatarException.class, () -> {
            store(MediaType.IMAGE_JPEG_VALUE, "logback-test.xml");
        });
    }

    @Test
    public void testStoreWithNullId() {
        assertThrows(NullPointerException.class, () -> {
            repository.store(AvatarType.USER, null, new AbstractAvatarSupplier() {

                @Nonnull
                @Override
                public InputStream open() throws IOException {
                    throw new IllegalStateException("Should never be called!");
                }
            });
        });
    }

    @Test
    public void testStoreWithNullSupplier() {
        assertThrows(NullPointerException.class, () -> {
            repository.store(AvatarType.USER, 1L, null);
        });
    }

    @Test
    public void testStoreWithNullType() {
        assertThrows(NullPointerException.class, () -> {
            repository.store(null, 1L, new AbstractAvatarSupplier() {

                @Nonnull
                @Override
                public InputStream open() throws IOException {
                    throw new IllegalStateException("Should never be called!");
                }
            });
        });
    }

    @Test
    public void testStoreWithOversizedImage() throws IOException {
        assertThrows(UnsupportedAvatarException.class, () -> {
            store(getPackagePath() + "/oversized.png");
        });
    }

    private File assertStored(final File file) {
        assertTrue(file.exists(), "Avatar file does not exist");
        assertTrue(file.isFile(), "Avatar file is not a file");

        return file;
    }

    private File fileFor(final AvatarType type, final Object id, final String name) {
        return FileUtils.construct(folder
                .toFile(),
            DiskAvatarRepository.AVATARS_PATH,
            type.getDirectoryName(),
            String.valueOf(id),
            name);
    }

    private void store(final String path) {
        store(MediaType.IMAGE_PNG_VALUE, path);
    }

    private void store(final String contentType, final String path) {
        final IAvatarSupplier supplier = new ResourceAvatarSupplier(contentType, path);

        repository.store(AvatarType.USER, 1L, supplier);
    }
}
