package com.pmi.tpd.database;

/**
 * Enumerates possible database support levels.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum DatabaseSupportLevel {

    /**
     * The database is <i>currently</i> supported, but support will be removed in a future release. At that time, the
     * database will transition to {@link #UNSUPPORTED unsupported}.
     */
    DEPRECATED,
    /**
     * The database is actively supported and tested. All functionality should work correctly.
     */
    SUPPORTED,
    /**
     * The database is not actively tested. Some functionality may not work as expected, and upgrading to new versions
     * of the system may completely break support altogether.
     */
    UNKNOWN,
    /**
     * The database is <i>actively unsupported</i>, meaning it has been tested and has known issues.
     */
    UNSUPPORTED
}
