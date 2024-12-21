package com.pmi.tpd.database.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.LatchMode;

/**
 * Describes a latch over the system database. This latch includes both the {@code DataSource} and the Hibernate
 * {@code SessionFactory}. While the latch is held, attempts to open new database connections or start Hibernate
 * sessions will block. Note that all other operations against the {@code SessionFactory} will also block, not just
 * creating new sessions.
 * <p>
 * <b>Warning</b>: A <i>best effort</i> is made to allow existing connections/sessions to continue processing normally,
 * but it is possible they will also be blocked in certain circumstances. When that happens, the latch will be unable to
 * {@link #drain(long, java.util.concurrent.TimeUnit) drain}.
 *
 * @see IDatabaseManager#acquireLatch(LatchMode)
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabaseLatch extends ILatch {

    /**
     * Switches the system database ({@code DatabaseConfiguration}, {@code DataSource} and {@code SessionFactory}) over
     * to the provided {@code target}, and then {@link #unlatch() unlatches} the system {@code DataSource} and
     * {@code SessionFactory}.
     *
     * @param target
     *               the new database to unlatch on
     */
    void unlatchTo(@Nonnull IDatabaseHandle target);
}
