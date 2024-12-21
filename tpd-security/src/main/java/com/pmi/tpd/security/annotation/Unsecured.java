package com.pmi.tpd.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which may be applied to a method which <i>intentionally</i> has no permission check applied.
 * <p>
 * The existence of this annotation is to signify that due consideration has been applied to any service methods which
 * do not require an explicit permission, to ensure that they are being omitted <i>deliberately</i> rather than
 * accidentally. Additionally, this annotation serves to call out methods for continued reevaluation to ensure that the
 * lack of an explicit permission is still acceptable.
 * <p>
 * Note: IF a method doesn't need a security check because its functionality imposes security as a side effect, or
 * through some other mechanism besides Spring Security annotations, it should be annotated {@link Secured} rather than
 * {@code Unsecured}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Unsecured {

    /**
     * A short explanation of why the method doesn't need to be secured to help developers quickly understand the
     * justification for having no security check.
     *
     * @return an explanation of why the method is not secured
     */
    String value();
}
