package com.pmi.tpd.core.event.publisher;

import java.lang.annotation.Annotation;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * Annotation based {@link IAsynchronousEventResolver}. This will check whether the event is annotated with the given
 * annotation.
 * </p>
 * <p>
 * The default annotation used is {@link AsynchronousPreferred}
 * </p>
 *
 * @see AsynchronousPreferred
 * @author Christophe Friederich
 * @since 1.0
 */
final class AnnotationAsynchronousEventResolver implements IAsynchronousEventResolver {

    /** annotation used. */
    private final Class<? extends Annotation> annotationClass;

    AnnotationAsynchronousEventResolver() {
        this(AsynchronousPreferred.class);
    }

    AnnotationAsynchronousEventResolver(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = Assert.notNull(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAsynchronousEvent(@Nonnull final Object event) {
        return Assert.notNull(event).getClass().getAnnotation(annotationClass) != null;
    }
}
