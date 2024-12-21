package com.pmi.tpd.core.elasticsearch;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

/**
 * RestClientFactoryBean
 */
public class RestClientFactoryBean implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientFactoryBean.class);

    private RestHighLevelClient client;

    private final ClientConfiguration clientConfiguration;

    public RestClientFactoryBean(final ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    @Override
    public void destroy() {
        try {
            LOGGER.info("Closing elasticSearch  client");
            if (client != null) {
                client.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Error closing ElasticSearch client: ", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        buildClient();
    }

    @Override
    public RestHighLevelClient getObject() {

        if (client == null) {
            throw new FactoryBeanNotInitializedException();
        }

        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    protected void buildClient() throws Exception {
        client = RestClients.create(clientConfiguration).rest();
    }

}
