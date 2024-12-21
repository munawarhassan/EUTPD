package com.pmi.tpd.core.security.web.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which can be applied to rest abd mvc Controller methods to not require xsrf protection
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface IgnoresXsrf {
}
