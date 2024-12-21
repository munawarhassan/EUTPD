package com.pmi.tpd.core.versioning;

/**
 * <p>
 * BuildVersion interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface BuildVersion {

    /**
     * <p>
     * getBuildNumber.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getBuildNumber();

    /**
     * <p>
     * getVersion.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getVersion();
}
