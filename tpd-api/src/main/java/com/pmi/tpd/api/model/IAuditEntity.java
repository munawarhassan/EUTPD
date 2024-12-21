package com.pmi.tpd.api.model;

import java.util.Comparator;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

import com.google.common.collect.Ordering;

public interface IAuditEntity {

    @SuppressWarnings("null")
    @Nonnull
    public static final Comparator<IAuditEntity> LAST_MODIFICATION_DESC_ORDERING = Ordering.from((left,
        right) -> ((IAuditEntity) left).getLastModifiedDate().compareTo(((IAuditEntity) right).getLastModifiedDate()))
            .reverse();

    /**
     * <p>
     * Getter for the field <code>createdBy</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getCreatedBy();

    /**
     * <p>
     * Getter for the field <code>createdDate</code>.
     * </p>
     *
     * @return a {@link DateTime} object.
     */
    DateTime getCreatedDate();

    /**
     * <p>
     * Getter for the field <code>lastModifiedBy</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getLastModifiedBy();

    /**
     * <p>
     * Getter for the field <code>lastModifiedDate</code>.
     * </p>
     *
     * @return a {@link DateTime} object.
     */
    DateTime getLastModifiedDate();

}