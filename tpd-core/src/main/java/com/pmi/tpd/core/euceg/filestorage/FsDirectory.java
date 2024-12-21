package com.pmi.tpd.core.euceg.filestorage;

import com.pmi.tpd.euceg.core.filestorage.IFileStorageDirectory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class FsDirectory<T> implements IFsElement<T> {
/** */
    private String name;
    /** */
    private String path;
    /** */
    private String parentPath;
    /** */
    private boolean directory;
    
    /** */
    private boolean empty;
    /** */
    private T metadata;
    
    @Override
    public FsType getType() {
       return FsType.directory;
    }

    public static <R> FsDirectory<R> createDirectory(final IFileStorageDirectory directory, R metadata) {
        return FsDirectory.<R>builder()
                .name(directory.getName())
                .path(directory.getRelativePath().toString())
                .parentPath(directory.getRelativeParentPath().toString())
                .directory(directory.isDirectory())  
                .empty(directory.isEmpty())
                .metadata(metadata)
                .build();
    }
    
    public static <R> FsDirectory<R> createDirectory(final IFileStorageDirectory directory) {
       return createDirectory(directory,null);
    }
}