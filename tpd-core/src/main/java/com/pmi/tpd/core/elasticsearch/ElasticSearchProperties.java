package com.pmi.tpd.core.elasticsearch;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 * {@code
 *   elasticsearch:
 *       useEmbedded: (true)|false
 *       clusterNodes:
 *       clusterName:
 *       ignoreClusterName: true|(false)
 *       transportSniff: (true)|false
 *       nodesSamplerInterval: 5s
 *       clientPingTimeout: 5s
 * }
 * </pre>
 *
 * @author Christophe Friederich
 * @since 2.2
 */
@Getter
@Setter
@ConfigurationProperties(ElasticSearchConfigurer.PREFIX_PROPERTY)
public class ElasticSearchProperties {

  /** */
  private boolean useEmbedded;

  /** */
  private String clusterNodes;

  /** */
  private boolean enableMemoryLock;

}
