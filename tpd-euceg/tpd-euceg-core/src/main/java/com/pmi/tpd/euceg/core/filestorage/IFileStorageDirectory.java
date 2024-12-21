package com.pmi.tpd.euceg.core.filestorage;

/**
 * Represents a directory in {@link IFileStorage}.
 *
 * @author christophe friederich
 * @since 3.0
 */
public interface IFileStorageDirectory extends IFileStorageElement {

    /**
     * Gets the indicating whether the directory is empty.
     *
     * @return Returns {@code true} whether the directory is empty, otherwise {@code false}.
     */
    public boolean isEmpty();
}
