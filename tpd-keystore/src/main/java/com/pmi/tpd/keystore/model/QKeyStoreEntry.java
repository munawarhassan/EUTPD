package com.pmi.tpd.keystore.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

/**
 * QKeyStoreEntry is a Querydsl query type for KeyStoreEntry
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QKeyStoreEntry extends EntityPathBase<KeyStoreEntry> {

    private static final long serialVersionUID = -745638316L;

    public static final QKeyStoreEntry keyStoreEntry = new QKeyStoreEntry("keyStoreEntry");

    public final StringPath algorithm = createString("algorithm");

    public final StringPath alias = createString("alias");

    public final BooleanPath expired = createBoolean("expired");

    public final DateTimePath<org.joda.time.DateTime> expiredDate = createDateTime("expiredDate",
        org.joda.time.DateTime.class);

    public final NumberPath<Integer> keySize = createNumber("keySize", Integer.class);

    public final DateTimePath<org.joda.time.DateTime> lastModified = createDateTime("lastModified",
        org.joda.time.DateTime.class);

    public final EnumPath<EntryType> type = createEnum("type", EntryType.class);

    public final BooleanPath valid = createBoolean("valid");

    public QKeyStoreEntry(final String variable) {
        super(KeyStoreEntry.class, forVariable(variable));
    }

    public QKeyStoreEntry(final Path<KeyStoreEntry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QKeyStoreEntry(final PathMetadata metadata) {
        super(KeyStoreEntry.class, metadata);
    }

}
