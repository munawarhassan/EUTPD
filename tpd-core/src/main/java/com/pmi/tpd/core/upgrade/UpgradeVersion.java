package com.pmi.tpd.core.upgrade;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * UpgradeVersion class.
 * </p>
 *
 * @author devacfr
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface UpgradeVersion {

    /**
     * @return
     */
    String targetVersion() default "";

    /**
     * @return
     */
    String buildNumber() default "0";
}
