package com.pmi.tpd.keystore.preference;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.concurrent.TimeUnit.MINUTES;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pmi.tpd.api.cache.GoogleCacheInstruments;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.keystore.IKeyStorePropertyManager;

/**
 * <p>
 * CertifactePropertyManager class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 2.2
 */
@Named
@Singleton
public class DefaultKeyStorePropertyManager implements IKeyStorePropertyManager, IStartable {

  /** cache of userkey -> propertyset for exclusive access. */
  protected final LoadingCache<String, IPropertyAccessor> psCache = CacheBuilder.newBuilder()
      .maximumSize(500)
      .expireAfterAccess(30, MINUTES)
      .build(CacheLoader.from(this::createPropertySet));

  /** */
  private static final String ENTITY_TYPE = "KeyStore";

  /** */
  private final IPropertySetFactory propertySetFactory;

  /**
   * <p>
   * Constructor for CertifactePropertyManager.
   * </p>
   *
   * @param propertySetFactory
   *                           a
   *                           {@link com.pmi.tpd.api.context.propertyset.core.context.propertyset.IPropertySetFactory}
   *                           object.
   */
  @Autowired
  public DefaultKeyStorePropertyManager(@Nonnull final IPropertySetFactory propertySetFactory) {
    this.propertySetFactory = checkNotNull(propertySetFactory, "propertySetFactory");
  }

  /**
   * <p>
   * onClearCache.
   * </p>
   *
   * @param event
   *              a {@link com.pmi.tpd.core.lifecycle.ClearCacheEvent} object.
   */
  @EventListener
  public void onClearCache(final ClearCacheEvent event) {
    psCache.invalidateAll();
  }

  /** {@inheritDoc} */
  @Override
  public void start() {
    new GoogleCacheInstruments(getClass().getSimpleName()).addCache(psCache).install();
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public IPropertyAccessor getPropertySet(final String alias) {
    if (Strings.isNullOrEmpty(alias)) {
      return null;
    }
    return psCache.getUnchecked(alias);
  }

  /** {@inheritDoc} */
  @Override
  public void clearCache(@Nonnull final String alias) {
    checkNotNull(alias, "alias");
    psCache.invalidate(alias);
  }

  private IPropertyAccessor createPropertySet(final String alias) {
    return propertySetFactory.buildCaching(ENTITY_TYPE, (long) alias.hashCode(), true);

  }
}
