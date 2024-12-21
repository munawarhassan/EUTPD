package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Wraps the {@link SpringDataElasticsearchExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SpringDataElasticsearchExtension.class)
@Tag(Tags.TEST_CONTAINERS)
public @interface Testcontainers {

    /**
     * Whether tests should be disabled (rather than failing) when Docker is not available.
     */
    boolean disabledWithoutDocker() default false;
}
