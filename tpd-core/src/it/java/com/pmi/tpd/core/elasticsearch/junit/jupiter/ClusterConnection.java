package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.support.VersionInfo;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * This class manages the connection to an Elasticsearch Cluster, starting a
 * containerized one if necessary. The
 * information about the ClusterConnection is stored both as a variable in the
 * instance for direct access from JUnit 5
 * and in a static ThreadLocal<ClusterConnectionInfo> accessible with the
 * {@link ClusterConnection#clusterConnectionInfo()} method to be integrated in
 * the Spring setup
 */
public class ClusterConnection implements ExtensionContext.Store.CloseableResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConnection.class);

  private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;

  private static final int ELASTICSEARCH_DEFAULT_TRANSPORT_PORT = 9300;

  private static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";

  private static final ThreadLocal<ClusterConnectionInfo> clusterConnectionInfoThreadLocal = new ThreadLocal<>();

  @Nullable
  private final ClusterConnectionInfo clusterConnectionInfo;

  /**
   * creates the ClusterConnection, starting a container if necessary.
   *
   * @param clusterUrl
   *                   if null or empty a local cluster is tarted
   */
  public ClusterConnection(@Nullable final String clusterUrl) {
    clusterConnectionInfo = StringUtils.isEmpty(clusterUrl) ? startElasticsearchContainer() : parseUrl(clusterUrl);

    if (clusterConnectionInfo != null) {
      LOGGER.debug(clusterConnectionInfo.toString());
      clusterConnectionInfoThreadLocal.set(clusterConnectionInfo);
    } else {
      LOGGER.error("could not create ClusterConnectionInfo");
    }
  }

  /**
   * @return the {@link ClusterConnectionInfo} from the ThreadLocal storage.
   */
  @Nullable
  public static ClusterConnectionInfo clusterConnectionInfo() {
    return clusterConnectionInfoThreadLocal.get();
  }

  @Nullable
  public ClusterConnectionInfo getClusterConnectionInfo() {
    return clusterConnectionInfo;
  }

  /**
   * @param clusterUrl
   *                   the URL to parse
   * @return the connection information
   */
  private ClusterConnectionInfo parseUrl(final String clusterUrl) {
    try {
      final URL url = new URL(clusterUrl);

      if (!url.getProtocol().startsWith("http") || url.getPort() <= 0) {
        throw new ClusterConnectionException("invalid url " + clusterUrl);
      }

      return ClusterConnectionInfo.builder() //
          .withHostAndPort(url.getHost(), url.getPort()) //
          .useSsl(url.getProtocol().equals("https")) //
          .build();
    } catch (final MalformedURLException e) {
      throw new ClusterConnectionException(e);
    }

  }

  @Nullable
  private ClusterConnectionInfo startElasticsearchContainer() {

    LOGGER.debug("Starting Elasticsearch Container");

    try {
      final String elasticsearchVersion = VersionInfo.versionProperties()
          .getProperty(VersionInfo.VERSION_ELASTICSEARCH_CLIENT);

      final String dockerImageName = ELASTICSEARCH_DEFAULT_IMAGE + ':' + elasticsearchVersion;
      LOGGER.debug("Docker image: {}", dockerImageName);
      final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(dockerImageName);
      elasticsearchContainer.start();
      return ClusterConnectionInfo.builder() //
          .withHostAndPort(elasticsearchContainer.getHost(),
              elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_PORT)) //
          .withTransportPort(elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_TRANSPORT_PORT)) //
          .withElasticsearchContainer(elasticsearchContainer) //
          .build();
    } catch (final Exception e) {
      LOGGER.error("Could not start Elasticsearch container", e);
    }

    return null;
  }

  @Override
  public void close() {

    if (clusterConnectionInfo != null && clusterConnectionInfo.getElasticsearchContainer() != null) {
      LOGGER.debug("Stopping container");
      clusterConnectionInfo.getElasticsearchContainer().stop();
    }

    LOGGER.debug("closed");
  }
}