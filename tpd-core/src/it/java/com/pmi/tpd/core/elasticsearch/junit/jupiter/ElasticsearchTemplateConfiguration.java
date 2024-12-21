package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

/**
 * Configuration for Spring Data Elasticsearch using
 * {@link org.springframework.data.elasticsearch.core.ElasticsearchTemplate}.
 */
@Configuration
public class ElasticsearchTemplateConfiguration extends ElasticsearchConfigurationSupport {

    @Bean
    public Client elasticsearchClient(final ClusterConnectionInfo clusterConnectionInfo) throws UnknownHostException {

        final Settings settings = Settings.builder()
                .put("cluster.name", clusterConnectionInfo.getClusterName())
                .build();
        final TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(clusterConnectionInfo.getHost()),
                clusterConnectionInfo.getTransportPort()));

        return client;
    }

    @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
    public ElasticsearchTemplate elasticsearchTemplate(final Client elasticsearchClient,
        final ElasticsearchConverter elasticsearchConverter) {

        final ElasticsearchTemplate template = new ElasticsearchTemplate(elasticsearchClient, elasticsearchConverter);
        template.setRefreshPolicy(refreshPolicy());

        return template;
    }

    @Override
    protected RefreshPolicy refreshPolicy() {
        return RefreshPolicy.IMMEDIATE;
    }
}