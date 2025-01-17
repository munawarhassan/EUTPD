package com.pmi.tpd.api.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for externalized configuration. Add this to a class definition or a {@code @Bean} method in a
 * {@code @Configuration} class if you want to bind and validate some external Properties (e.g. from a .properties
 * file).
 * <p>
 * Note that contrary to {@code @Value}, SpEL expressions are not evaluated since property values are externalized.
 *
 * @author Dave Syer
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see EnableConfigurationProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

    /**
     * The name prefix of the properties that are valid to bind to this object. Synonym for {@link #prefix()}.
     *
     * @return the name prefix of the properties to bind
     */
    String value() default "";

    /**
     * The name prefix of the properties that are valid to bind to this object. Synonym for {@link #value()}.
     *
     * @return the name prefix of the properties to bind
     */
    String prefix() default "";

    /**
     * Flag to indicate that when binding to this object invalid fields should be ignored. Invalid means invalid
     * according to the binder that is used, and usually this means fields of the wrong type (or that cannot be coerced
     * into the correct type).
     *
     * @return the flag value (default false)
     */
    boolean ignoreInvalidFields() default false;

    /**
     * Flag to indicate that when binding to this object fields with periods in their names should be ignored.
     *
     * @return the flag value (default false)
     */
    boolean ignoreNestedProperties() default false;

    /**
     * Flag to indicate that when binding to this object unknown fields should be ignored. An unknown field could be a
     * sign of a mistake in the Properties.
     *
     * @return the flag value (default true)
     */
    boolean ignoreUnknownFields() default true;

    /**
     * Flag to indicate that an exception should be raised if a Validator is available and validation fails. If it is
     * set to false, validation errors will be swallowed. They will be logged, but not propagated to the caller.
     *
     * @return the flag value (default true)
     */
    boolean exceptionIfInvalid() default true;

}
