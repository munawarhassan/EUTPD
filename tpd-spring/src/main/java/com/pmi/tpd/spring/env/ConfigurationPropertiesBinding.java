package com.pmi.tpd.spring.env;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * Qualifier for beans that are needed to configure the binding of {@link ConfigurationProperties} (e.g. Converters).
 *
 * @author Dave Syer
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPropertiesBinding {

  /**
   * Concrete value for the {@link Qualifier @Qualifier}.
   */
  String VALUE = "com.pmi.tpd.spring.env.ConfigurationPropertiesBinding";
}
