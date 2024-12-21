package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Combines the {@link SpringDataElasticsearchExtension} and the {@link SpringExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SpringDataElasticsearchExtension.class)
@ExtendWith(SpringExtension.class)
@Tag(Tags.TEST_CONTAINERS)
public @interface SpringTestcontainers {

    /**
     * Whether tests should be disabled (rather than failing) when Docker is not available.
     */
    boolean disabledWithoutDocker() default false;
}
