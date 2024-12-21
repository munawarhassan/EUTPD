package com.pmi.tpd.api.util;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Utility class for generating random Strings.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class RandomUtil {

    /** */
    private static final int DEF_COUNT = 20;

    private RandomUtil() {
    }

    /**
     * Generates a password.
     *
     * @return the generated password
     */
    @Nonnull
    public static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
    }

    /**
     * Generate a uuid
     *
     * @return a random uuid
     */
    @Nonnull
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}
