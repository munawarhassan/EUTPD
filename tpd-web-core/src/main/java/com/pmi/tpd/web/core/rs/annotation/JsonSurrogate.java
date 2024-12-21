package com.pmi.tpd.web.core.rs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a class is substitutable for another class for the purposes of rendering the original class as JSON.
 * <p>
 * Classes annotating themselves with this should indicate the class they are surrogates for in the value attribute and
 * should provide one of two constructors:
 * </p>
 * <p>
 * <ul>
 * <li>A public single argument constructor taking the an instance of the class in {@link #value()}</li>
 * </ul>
 * </p>
 * The annotated class should, through its structure or through any other annotations it has applied, be able to be
 * serialised to JSON by jax-rs.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonSurrogate {

    /**
     * @return the class the annotated type can act as a surrogate for.
     */
    Class<?> value();
}
