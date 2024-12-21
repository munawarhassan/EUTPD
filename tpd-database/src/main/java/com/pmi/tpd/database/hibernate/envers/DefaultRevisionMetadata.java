package com.pmi.tpd.database.hibernate.envers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.data.history.RevisionMetadata;

import com.pmi.tpd.api.util.Assert;

public class DefaultRevisionMetadata implements RevisionMetadata<Integer> {

    private final DefaultRevisionEntity entity;

    private final RevisionType revisionType;

    /**
     * Creates a new {@link DefaultRevisionMetadata}.
     *
     * @param entity
     *            must not be {@literal null}.
     */
    public DefaultRevisionMetadata(final DefaultRevisionEntity entity) {
        this(entity, RevisionType.UNKNOWN);
    }

    public DefaultRevisionMetadata(final DefaultRevisionEntity entity, final RevisionType revisionType) {
        this.entity = Assert.notNull(entity, "entity");;
        this.revisionType = revisionType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.history.RevisionMetadata#getRevisionNumber()
     */
    @Override
    public Optional<Integer> getRevisionNumber() {
        return Optional.of(entity.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.history.RevisionMetadata#getRevisionDate()
     */
    @Override
    public Optional<Instant> getRevisionInstant() {
        return Optional.of(convertToInstant(entity.getTimestamp()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionType getRevisionType() {
        return revisionType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.history.RevisionMetadata#getDelegate()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getDelegate() {
        return (T) entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultRevisionMetadata that = (DefaultRevisionMetadata) o;
        return getRevisionNumber().equals(that.getRevisionNumber())
                && getRevisionInstant().equals(that.getRevisionInstant())
                && revisionType.equals(that.getRevisionType());
    }

    @Override
    public String toString() {
        return "DefaultRevisionMetadata{" + "entity=" + entity + ", revisionType=" + revisionType + '}';
    }

    private static Instant convertToInstant(final Object timestamp) {

        if (timestamp instanceof Instant) {
            return (Instant) timestamp;
        }

        if (timestamp instanceof LocalDateTime) {
            return ((LocalDateTime) timestamp).atZone(ZoneOffset.systemDefault()).toInstant();
        }

        if (timestamp instanceof Long) {
            return Instant.ofEpochMilli((Long) timestamp);
        }

        if (Date.class.isInstance(timestamp)) {
            return Date.class.cast(timestamp).toInstant();
        }

        throw new IllegalArgumentException(String.format("Can't convert %s to Instant!", timestamp));
    }

}
