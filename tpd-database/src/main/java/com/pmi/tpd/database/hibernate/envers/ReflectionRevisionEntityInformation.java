package com.pmi.tpd.database.hibernate.envers;

import org.hibernate.envers.RevisionNumber;
import org.springframework.data.repository.history.support.RevisionEntityInformation;
import org.springframework.data.util.AnnotationDetectionFieldCallback;
import org.springframework.util.ReflectionUtils;

import com.pmi.tpd.api.util.Assert;

/**
 * {@link RevisionEntityInformation} that uses reflection to inspect a property annotated with {@link RevisionNumber} to
 * find out about the revision number type.
 */
public class ReflectionRevisionEntityInformation implements RevisionEntityInformation {

    private final Class<?> revisionEntityClass;

    private final Class<?> revisionNumberType;

    /**
     * Creates a new {@link ReflectionRevisionEntityInformation} inspecting the given revision entity class.
     *
     * @param revisionEntityClass
     *            must not be {@literal null}.
     */
    public ReflectionRevisionEntityInformation(final Class<?> revisionEntityClass) {

        Assert.notNull(revisionEntityClass);

        final AnnotationDetectionFieldCallback fieldCallback = new AnnotationDetectionFieldCallback(
                RevisionNumber.class);
        ReflectionUtils.doWithFields(revisionEntityClass, fieldCallback);

        this.revisionNumberType = fieldCallback.getType();
        this.revisionEntityClass = revisionEntityClass;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getRevisionNumberType() {
        return revisionNumberType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefaultRevisionEntity() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getRevisionEntityClass() {
        return revisionEntityClass;
    }
}
