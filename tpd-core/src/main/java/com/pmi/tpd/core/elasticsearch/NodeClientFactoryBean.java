package com.pmi.tpd.core.elasticsearch;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.LogConfigurator;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Strings;

/**
 * NodeClientFactoryBean
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Ilkang Na
 */

public class NodeClientFactoryBean implements FactoryBean<Client>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(NodeClientFactoryBean.class);

    private boolean local = true;

    /** */
    @SuppressWarnings("unused")
    private boolean enableHttp;

    /** */
    private String clusterName;

    /** */
    private NodeClient nodeClient;

    /** */
    private String pathData;

    /** */
    private String pathHome;

    /** */
    private String pathConfiguration;

    /** */
    private TestNode node;

    /** */
    private boolean enableMemoryLock;

    public static class TestNode extends Node {

        private static final String DEFAULT_NODE_NAME = "spring-data-elasticsearch-nodeclientfactorybean-test";

        public TestNode(final Settings preparedSettings, final Collection<Class<? extends Plugin>> classpathPlugins) {

            super(InternalSettingsPreparer
                    .prepareEnvironment(preparedSettings, Collections.emptyMap(), null, () -> DEFAULT_NODE_NAME),
                    classpathPlugins, false);
        }

        protected void registerDerivedNodeNameWithLogger(final String nodeName) {
            try {
                LogConfigurator.setNodeName(nodeName);
            } catch (final Exception e) {
                // nagh - just forget about it
            }
        }
    }

    NodeClientFactoryBean() {
    }

    public NodeClientFactoryBean(final boolean local) {
        this.local = local;
    }

    @Override
    public NodeClient getObject() throws Exception {
        return nodeClient;
    }

    @Override
    public Class<? extends Client> getObjectType() {
        return NodeClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        node = new TestNode(Settings.builder()
                .put(loadConfig())
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("client.transport.sniff", !local)
                .put("path.home", this.pathHome)
                .put("path.data", this.pathData)
                .put("cluster.name", this.clusterName)
                .put("node.max_local_storage_nodes", 100)
                .put("bootstrap.memory_lock", this.enableMemoryLock)
                .build(), asList(Netty4Plugin.class, CommonAnalysisPlugin.class, PainlessPlugin.class));
        nodeClient = (NodeClient) node.start().client();
    }

    private Settings loadConfig() throws IOException {
        if (!Strings.isNullOrEmpty(pathConfiguration)) {
            final InputStream stream = getClass().getClassLoader().getResourceAsStream(pathConfiguration);
            if (stream != null) {
                return Settings.builder()
                        .loadFromStream(pathConfiguration,
                            getClass().getClassLoader().getResourceAsStream(pathConfiguration),
                            false)
                        .build();
            }
            logger.error(String.format("Unable to read node configuration from file [%s]", pathConfiguration));
        }
        return Settings.builder().build();
    }

    public void setLocal(final boolean local) {
        this.local = local;
    }

    public void setEnableHttp(final boolean enableHttp) {
        this.enableHttp = enableHttp;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public void setPathData(final String pathData) {
        this.pathData = pathData;
    }

    public void setPathHome(final String pathHome) {
        this.pathHome = pathHome;
    }

    public void setPathConfiguration(final String configuration) {
        this.pathConfiguration = configuration;
    }

    public void setEnableMemoryLock(final boolean enableMemoryLock) {
        this.enableMemoryLock = enableMemoryLock;
    }

    @Override
    public void destroy() throws Exception {
        try {
            logger.info("Closing elasticSearch  client");
            if (node != null) {
                node.close();
            }
            if (nodeClient != null) {
                nodeClient.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing ElasticSearch client: ", e);
        }
    }
}
