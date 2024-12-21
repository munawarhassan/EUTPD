package com.pmi.tpd.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which may be applied to methods which are secured using something other than Spring Security's AOP
 * annotations.
 * <p>
 * Note: If a method does not need security applied to it, perhaps because it is a trivial function which should be
 * available in all contexts to all users, it should be annotated {@link Unsecured}, not {@code Secured}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Secured {

    /**
     * A short explanation of how the method is secured to help developers quickly understand the security mechanism
     * that has been applied without having to dig into the implementation.
     *
     * @return an explanation of how the method is secured
     */
    String value();
}
