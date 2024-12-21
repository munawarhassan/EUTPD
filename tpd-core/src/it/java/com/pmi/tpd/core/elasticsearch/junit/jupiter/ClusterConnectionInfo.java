package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import javax.annotation.Nullable;

import org.springframework.util.Assert;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * The information about the ClusterConnection.<br/>
 * The {@link #host}, {@link #httpPort} and {@link #useSsl} values specify the
 * values needed to connect to the cluster
 * with a rest client for both a local started cluster and for one defined by
 * the cluster URL when creating the
 * {@link ClusterConnection}.<br/>
 * The object must be created by using a {@link ClusterConnectionInfo.Builder}.
 */
public final class ClusterConnectionInfo {

  private final boolean useSsl;

  private final String host;

  private final int httpPort;

  private final int transportPort;

  private final String clusterName;

  private final ElasticsearchContainer elasticsearchContainer;

  public static Builder builder() {
    return new Builder();
  }

  private ClusterConnectionInfo(final String host, final int httpPort, final boolean useSsl, final int transportPort,
      @Nullable final ElasticsearchContainer elasticsearchContainer) {
    this.host = host;
    this.httpPort = httpPort;
    this.useSsl = useSsl;
    this.transportPort = transportPort;
    this.elasticsearchContainer = elasticsearchContainer;
    this.clusterName = "docker-cluster";
  }

  @Override
  public String toString() {
    return "ClusterConnectionInfo{" + //
        "useSsl=" + useSsl + //
        ", host='" + host + '\'' + //
        ", httpPort=" + httpPort + //
        ", transportPort=" + transportPort + //
        '}'; //
  }

  public String getHost() {
    return host;
  }

  public int getHttpPort() {
    return httpPort;
  }

  public int getTransportPort() {
    return transportPort;
  }

  public String getClusterName() {
    return clusterName;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  @Nullable
  public ElasticsearchContainer getElasticsearchContainer() {
    return elasticsearchContainer;
  }

  public static class Builder {

    boolean useSsl = false;

    private String host;

    private int httpPort;

    private int transportPort;

    @Nullable
    private ElasticsearchContainer elasticsearchContainer;

    public Builder withHostAndPort(final String host, final int httpPort) {
      Assert.hasLength(host, "host must not be empty");
      this.host = host;
      this.httpPort = httpPort;
      return this;
    }

    public Builder useSsl(final boolean useSsl) {
      this.useSsl = useSsl;
      return this;
    }

    public Builder withTransportPort(final int transportPort) {
      this.transportPort = transportPort;
      return this;
    }

    public Builder withElasticsearchContainer(final ElasticsearchContainer elasticsearchContainer) {
      this.elasticsearchContainer = elasticsearchContainer;
      return this;
    }

    public ClusterConnectionInfo build() {
      return new ClusterConnectionInfo(host, httpPort, useSsl, transportPort, elasticsearchContainer);
    }
  }
}