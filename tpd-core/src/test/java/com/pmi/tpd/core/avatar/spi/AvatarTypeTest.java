package com.pmi.tpd.core.avatar.spi;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.testing.junit5.TestCase;

public class AvatarTypeTest extends TestCase {

    private static final Pattern PATTERN_USER_PATH = Pattern.compile("avatars/user/([\\d]+)\\.png");

    @Test
    public void testLoadDefaultForUser() throws Exception {
        for (final int size : AvatarType.DEFAULT_SIZES) {
            final IAvatarSupplier supplier = AvatarType.USER.loadDefault("1", size);
            assertNotNull(supplier, "Loading a default user avatar should never return null");
            assertEquals(AvatarType.USER.getContentType(),
                supplier.getContentType(),
                "User avatar has wrong content type");

            final ClassPathResource resource = ((ResourceAvatarSupplier) supplier).getResource();
            final Matcher matcher = PATTERN_USER_PATH.matcher(resource.getPath());
            assertTrue(matcher.matches(), "The user avatar's name does not match the expected pattern");
            assertEquals(size, Integer.parseInt(matcher.group(1)), "The user avatar size is not what was requested");

            try (InputStream stream = supplier.open()) {
                assertNotNull(stream, "The user avatar did not provide an InputStream");
            }
        }
    }

    @Test
    public void testLoadFixedDefault() throws Exception {
        for (final AvatarType type : AvatarType.values()) {
            final IAvatarSupplier supplier = type.loadFixedDefault(64);
            assertNotNull(supplier, type.name() + " has no fixed default avatar");
            assertEquals(type.getContentType(),
                supplier.getContentType(),
                "The fixed default avatar for " + type.name() + " has the wrong content type");

            try (InputStream stream = supplier.open()) {
                assertNotNull(stream,
                    "The fixed default avatar for " + type.name() + " did not provide an InputStream");
            }
        }
    }
}
