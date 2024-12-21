package com.pmi.tpd.core.cache;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author christophe friederich
 * @since 2.2
 */
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@ConfigurationProperties(prefix = "hazelcast.cache")
public class CacheProperties {

    private String uri;

    private String provider;

}
