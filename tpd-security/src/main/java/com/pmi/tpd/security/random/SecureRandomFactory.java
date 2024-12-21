package com.pmi.tpd.security.random;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory which returns properly initialised instances of {@link SecureRandom}.
 * <p/>
 * Clients should not access this class directly, but instead use {@link ISecureRandomService} or
 * {@link ISecureTokenGenerator} for their random data generation.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SecureRandomFactory {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureRandomFactory.class);

    private SecureRandomFactory() {
        // prevent construction
    }

    /**
     * Creates and fully initialises a new {@link SecureRandom} instance.
     * <p/>
     * The instance is created via {@link SecureRandom#SecureRandom()}, which uses the default algorithm provided by the
     * JVM.
     * <p/>
     * The initialisation involves forcing the self-seeding of the instance by calling
     * {@link SecureRandom#nextBytes(byte[])}.
     *
     * @return self-seeded {@link SecureRandom} instance.
     */
    public static SecureRandom newInstance() {
        LOGGER.debug("Starting creation of new SecureRandom");
        final long start = System.currentTimeMillis();

        final SecureRandom random = new SecureRandom();

        // force self-seeding
        random.nextBytes(new byte[1]);

        final long end = System.currentTimeMillis();
        LOGGER.debug("Finished creation new SecureRandom in {} ms", end - start);

        return random;
    }
}
