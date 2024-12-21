package com.pmi.tpd.security.random;

import java.security.SecureRandom;

/**
 * Implementation of the {@link ISecureRandomService} which delegates to a single, shared instance of
 * {@link SecureRandom}.
 * <p/>
 * <p/>
 * Potential improvements include:
 * <ul>
 * <li>Periodic Re-seeding: currently we are not sure if this is a requirement, so it has not been implemented.
 * <li>Object Pooling: as {@link SecureRandom}s block on calls to produce random data, we may improve throughput by
 * pooling a collection of {@link SecureRandom}s. Contention is not yet been proved to be a problem, so it has not been
 * implemented.
 * </ul>
 * <p/>
 * The current implementation is guaranteed to be thread-safe as it delegates calls to the underlying
 * {@link java.security.SecureRandom} instance.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DefaultSecureRandomService implements ISecureRandomService {

    /** */
    private static final ISecureRandomService INSTANCE = new DefaultSecureRandomService(
            SecureRandomFactory.newInstance());

    /** */
    private final SecureRandom random;

    DefaultSecureRandomService(final SecureRandom random) {
        this.random = random;
    }

    /**
     * @return shared {@link DefaultSecureRandomService} instance which delegates to a single, shared instance of
     *         {@link SecureRandom}.
     */
    public static ISecureRandomService getInstance() {
        return INSTANCE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void nextBytes(final byte[] bytes) {
        random.nextBytes(bytes);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int nextInt() {
        return random.nextInt();
    }

    /**
     * @inheritDoc
     */
    @Override
    public int nextInt(final int n) {
        return random.nextInt(n);
    }

    /**
     * @inheritDoc
     */
    @Override
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * @inheritDoc
     */
    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    /**
     * @inheritDoc
     */
    @Override
    public double nextDouble() {
        return random.nextDouble();
    }
}
