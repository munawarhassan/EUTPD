package com.pmi.tpd.core.euceg.filestorage;

import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;

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
public class FsFile<T> implements IFsElement<T> {
    
    private String uuid;

    private String name;

    private String parentPath;

    private long size;

    private boolean directory;
    
    private String mimeType;
    
    private T metadata;
    

    @Override
    public FsType getType() {
       return FsType.file;
    }
    
    public static <R> FsFile<R> createFile(final IFileStorageFile file, R metadata) {
        return FsFile.<R>builder()
                .uuid(file.getUUID())
                .name(file.getName())
                .parentPath(file.getRelativeParentPath().toString())
                .directory(file.isDirectory())
                .size(file.getSize())
                .metadata(metadata)
                .mimeType(file.getMimeType().orElse(null))
                .build();
    }
}