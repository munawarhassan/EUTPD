package com.pmi.tpd.api.event.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to be used with events to tell whether they can be handled asynchronously
 * </p>
 * <p>
 * This is the default annotation to be used with
 * {@link com.pmi.tpd.core.event.internal.IAsynchronousEventResolver IAsynchronousEventResolver}
 * </p>
 * .
 *
 * @see com.pmi.tpd.core.event.internal.IAsynchronousEventResolver IAsynchronousEventResolver
 * @author Christophe Friederich
 * @since 1.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface AsynchronousPreferred {
}
