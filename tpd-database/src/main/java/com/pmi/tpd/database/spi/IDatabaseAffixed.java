package com.pmi.tpd.database.spi;

/**
 * Implemented by components which are affixed to the current database. If the database changes (for example, in
 * response to a successful database migration), components are notified via {@link #release} that they need to refresh
 * their internal state to pick up the new database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabaseAffixed {

    /**
     * Indicates the component should release its currently-affixed database in preparation for its replacement.
     * <p>
     * <b>Warning</b>: Implementations should <i>not</i> attempt to fetch the new database details on the thread that
     * invokes this method; there is no guarantee the new database is in place and accessible yet. This is
     * <i>intentional</i>. The desired behaviour is for any thread which calls into the implementing component to block,
     * if the database is not available (latched), until it becomes available.
     */
    void release();
}
