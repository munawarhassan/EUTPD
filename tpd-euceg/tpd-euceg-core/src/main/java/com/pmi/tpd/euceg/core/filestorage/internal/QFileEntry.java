package com.pmi.tpd.euceg.core.filestorage.internal;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

/**
 * QFileEntry is a Querydsl query type for FileEntry
 */
public class QFileEntry extends EntityPathBase<FileEntry> {

    private static final long serialVersionUID = -745638316L;

    public static final QFileEntry fileEntry = new QFileEntry("fileEntry");

    public final StringPath name = createString("name");

    public QFileEntry(final String variable) {
        super(FileEntry.class, forVariable(variable));
    }

    public QFileEntry(final Path<FileEntry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFileEntry(final PathMetadata metadata) {
        super(FileEntry.class, metadata);
    }

}
