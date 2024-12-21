package com.pmi.tpd.core.elasticsearch;

import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;

import com.pmi.tpd.core.elasticsearch.junit.jupiter.ElasticsearchTemplateConfiguration;
import com.pmi.tpd.core.elasticsearch.junit.jupiter.SpringTestcontainers;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * class demonstrating the setup of a JUnit 5 test in Spring Data Elasticsearch that uses the transport client. The
 * ContextConfiguration must include the {@link ElasticsearchTemplateConfiguration} class
 */
@SpringTestcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = { ElasticsearchTemplateConfiguration.class })
@DisplayName("a sample JUnit 5 test with transport client")
public class JUnit5SampleTransportClientBasedTestIT extends TestCase {

    @Inject
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    @DisplayName("should have a ElasticsearchTemplate")
    void shouldHaveATemplate() {
        assertThat(elasticsearchOperations, notNullValue());
        assertThat(elasticsearchOperations, isA(ElasticsearchTemplate.class));
    }
}
