package com.pmi.tpd.security.random;

import org.bouncycastle.util.encoders.Hex;

/**
 * Implementation of {@link ISecureTokenGenerator} which uses the {@link DefaultSecureRandomService} for random byte
 * generation.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DefaultSecureTokenGenerator implements ISecureTokenGenerator {

    /** */
    private static final ISecureTokenGenerator INSTANCE = new DefaultSecureTokenGenerator(
            DefaultSecureRandomService.getInstance());

    /** */
    private static final int TOKEN_LENGTH_BYTES = 20;

    /** */
    private final ISecureRandomService randomService;

    DefaultSecureTokenGenerator(final ISecureRandomService randomService) {
        this.randomService = randomService;
    }

    /**
     * @return shared {@link DefaultSecureTokenGenerator} instance.
     */
    public static ISecureTokenGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a hexadecimal {@link String} representation of 20 random bytes, produced by
     * {@link DefaultSecureRandomService#nextBytes(byte[])}. The generated {@link String} is 40 characters in length and
     * is composed of characters in the range '0'-'9' and 'a'-'f'. The length (20 bytes / 160 bits) was selected as it
     * is the same as the size of the internal state of the SHA1PRNG.
     *
     * @return returns a hexadecimal encoded representation of 20 random bytes.
     */
    @Override
    public String generateToken() {
        final byte[] bytes = new byte[TOKEN_LENGTH_BYTES];

        randomService.nextBytes(bytes);

        // can replace this with Hex.encodeHexString(bytes) when we upgrade to commons-codec 1.4.
        return new String(Hex.encode(bytes));
    }
}
