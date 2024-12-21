package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.time.Duration;
import java.util.Arrays;

import javax.inject.Inject;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import com.pmi.tpd.core.elasticsearch.converter.DateTimeToLong;
import com.pmi.tpd.core.elasticsearch.converter.LocalDateToLong;
import com.pmi.tpd.core.elasticsearch.converter.LongToDateTime;
import com.pmi.tpd.core.elasticsearch.converter.LongToLocalDate;

/**
 * Configuration for Spring Data Elasticsearch using
 * {@link org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate}.
 */
@Configuration
public class ElasticsearchRestTemplateConfiguration extends AbstractElasticsearchConfiguration {

    @Inject
    private ClusterConnectionInfo clusterConnectionInfo;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final String elasticsearchHostPort = clusterConnectionInfo.getHost() + ':'
                + clusterConnectionInfo.getHttpPort();

        ClientConfiguration.TerminalClientConfigurationBuilder configurationBuilder = ClientConfiguration.builder()
                .connectedTo(elasticsearchHostPort);

        final String proxy = System.getenv("DATAES_ELASTICSEARCH_PROXY");

        if (proxy != null) {
            configurationBuilder = configurationBuilder.withProxy(proxy);
        }

        if (clusterConnectionInfo.isUseSsl()) {
            configurationBuilder = ((ClientConfiguration.MaybeSecureClientConfigurationBuilder) configurationBuilder)
                    .usingSsl();
        }

        return RestClients.create(configurationBuilder //
                .withConnectTimeout(Duration.ofSeconds(20)) //
                .withSocketTimeout(Duration.ofSeconds(20)) //
                .build()) //
                .rest();
    }

    @Override
    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Arrays
                .asList(new DateTimeToLong(), new LongToDateTime(), new LongToLocalDate(), new LocalDateToLong()));
    }

    @Override
    public ElasticsearchOperations elasticsearchOperations(final ElasticsearchConverter elasticsearchConverter,
        final RestHighLevelClient elasticsearchClient) {

        final ElasticsearchRestTemplate template = new ElasticsearchRestTemplate(elasticsearchClient,
                elasticsearchConverter) {

            @Override
            public <T> T execute(final ClientCallback<T> callback) {
                try {
                    return super.execute(callback);
                } catch (final DataAccessResourceFailureException e) {
                    try {
                        Thread.sleep(1_000);
                    } catch (final InterruptedException ignored) {
                    }
                    return super.execute(callback);
                }
            }
        };
        template.setRefreshPolicy(refreshPolicy());

        return template;
    }

    @Override
    protected RefreshPolicy refreshPolicy() {
        return RefreshPolicy.IMMEDIATE;
    }
}