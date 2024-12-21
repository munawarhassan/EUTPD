package com.pmi.tpd.core.elasticsearch;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.ContextConfiguration;

import com.pmi.tpd.core.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.pmi.tpd.core.elasticsearch.junit.jupiter.SpringTestcontainers;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * class demonstrating the setup of a JUnit 5 test in Spring Data Elasticsearch that uses the rest client. The
 * ContextConfiguration must include the {@link ElasticsearchRestTemplateConfiguration} class.
 */
@SpringTestcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = { ElasticsearchRestTemplateConfiguration.class })
@DisplayName("a sample JUnit 5 test with rest client")
public class JUnit5SampleRestClientBasedTestIT extends TestCase {

    @Inject
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    @DisplayName("should have a ElasticsearchRestTemplate")
    void shouldHaveARestTemplate() {
        assertThat(elasticsearchOperations, notNullValue());
        assertThat(elasticsearchOperations, Matchers.isA(ElasticsearchRestTemplate.class));

    }
}
