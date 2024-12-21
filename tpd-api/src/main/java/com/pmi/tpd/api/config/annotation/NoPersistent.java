package com.pmi.tpd.api.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a property is ignored ignore property when store properties configuration bean.
 *
 * @author Christophe Friederich
 * @since 2.2
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPersistent {
}