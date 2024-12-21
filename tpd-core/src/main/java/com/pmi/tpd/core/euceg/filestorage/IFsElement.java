package com.pmi.tpd.core.euceg.filestorage;

import java.util.Optional;

import com.pmi.tpd.euceg.core.filestorage.IFileStorageDirectory;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageElement;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;

public interface IFsElement<T> {

    public enum FsType {
        directory,
        file
    }

    boolean isDirectory();

    String getName();

    String getParentPath();

    FsType getType();

    T getMetadata();

    public static <R> Optional<IFsElement<R>> create(final IFileStorageElement element, final R metadata) {
        if (element instanceof IFileStorageFile) {
            return Optional.of(FsFile.createFile((IFileStorageFile) element, metadata));
        } else if (element instanceof IFileStorageDirectory) {
            return Optional.of(FsDirectory.createDirectory((IFileStorageDirectory) element, metadata));
        }
        return Optional.empty();
    }
}
