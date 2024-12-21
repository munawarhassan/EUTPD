package com.pmi.tpd.keystore.spring;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.keystore.CertificateValidityScheduler;
import com.pmi.tpd.keystore.DefaultKeyStoreService;
import com.pmi.tpd.keystore.ICertificateMailNotifier;
import com.pmi.tpd.keystore.IKeyStorePreferencesManager;
import com.pmi.tpd.keystore.IKeyStorePropertyManager;
import com.pmi.tpd.keystore.IKeyStoreService;
import com.pmi.tpd.keystore.KeyStoreProperties;
import com.pmi.tpd.keystore.preference.DefaultKeyStorePreferencesManager;
import com.pmi.tpd.keystore.preference.DefaultKeyStorePropertyManager;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;

/**
 * <p>
 * KeyStoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({ KeyStoreProperties.class })
public class KeyStoreConfigurer {

  @Inject
  private IApplicationProperties applicationProperties;

  @Inject
  private I18nService i18nService;

  @Inject
  private IEventPublisher eventPublisher;

  @Bean
  public IKeyStorePropertyManager getKeyStorePropertyManager(@Nonnull final IPropertySetFactory propertySetFactory) {
    return new DefaultKeyStorePropertyManager(propertySetFactory);
  }

  @Bean
  public IKeyStorePreferencesManager getKeyStorePreferencesManager(
      @Nonnull final IKeyStorePropertyManager propertyManager) {
    return new DefaultKeyStorePreferencesManager(propertyManager);
  }

  /**
   * @param preferencesManager
   *                           a preferences Manager.
   * @return Returns {@link IKeyStoreService} instance.
   */
  @Bean
  public IKeyStoreService keyStoreService(@Nonnull final IKeyStorePreferencesManager preferencesManager) {
    return new DefaultKeyStoreService(preferencesManager, eventPublisher, i18nService, applicationProperties);
  }

  /**
   * @param keyStoreService
   * @param preferencesManager
   * @param notifier
   * @return
   */
  @Bean
  public IScheduledJobSource certificateValidityScheduler(@Nonnull final IKeyStoreService keyStoreService,
      @Nonnull final IKeyStorePreferencesManager preferencesManager,
      @Nonnull final ICertificateMailNotifier notifier) {
    return new CertificateValidityScheduler(keyStoreService, preferencesManager, notifier);
  }

}