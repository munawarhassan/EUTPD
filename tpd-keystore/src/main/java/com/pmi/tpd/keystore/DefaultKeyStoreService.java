package com.pmi.tpd.keystore;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.joda.time.DateTime;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.api.crypto.KeyInfo;
import com.pmi.tpd.api.crypto.KeyPair;
import com.pmi.tpd.api.crypto.KeyPairHelper;
import com.pmi.tpd.api.crypto.KeyStoreHelper;
import com.pmi.tpd.api.crypto.KeyStoreType;
import com.pmi.tpd.api.crypto.KeystoreException;
import com.pmi.tpd.api.crypto.X509CertHelper;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.keystore.event.KeyCreatedEvent;
import com.pmi.tpd.keystore.event.KeyDeletedEvent;
import com.pmi.tpd.keystore.event.KeyUpdatedEvent;
import com.pmi.tpd.keystore.model.EntryType;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.keystore.model.KeyStoreEntry.KeyStoreEntryBuilder;
import com.pmi.tpd.keystore.model.QKeyStoreEntry;
import com.pmi.tpd.keystore.preference.IKeyStorePreferences;
import com.pmi.tpd.keystore.preference.KeyStorePreferenceKeys;
import com.pmi.tpd.security.annotation.Unsecured;
import com.querydsl.collections.CollQuery;
import com.querydsl.collections.CollQueryTemplates;
import com.querydsl.collections.DefaultEvaluatorFactory;
import com.querydsl.collections.DefaultQueryEngine;
import com.querydsl.collections.QueryEngine;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 */
public class DefaultKeyStoreService implements IKeyStoreService, IKeyProvider {

  /** */
  private final IKeyStorePreferencesManager preferencesManager;

  /** */
  private final IEventPublisher eventPublisher;

  /** */
  private final I18nService i18nService;

  /** */
  private final IApplicationProperties applicationProperties;

  /** */
  private final KeyStoreType keyStoreType = KeyStoreType.JKS;

  /** */
  private final QueryEngine queryEngine;

  /** */
  private final PathBuilder<KeyStoreEntry> builder;

  /**
   * @param preferencesManager
   *                           preferencesManager.
   * @param location
   *                           location where store {@link KeyStore} (can
   *                           <b>not</b> be null).
   * @param password
   *                           the password used to open {@link KeyStore} (can
   *                           <b>not</b> be null or empty).
   */
  @Inject
  public DefaultKeyStoreService(@Nonnull final IKeyStorePreferencesManager preferencesManager,
      @Nonnull final IEventPublisher eventPublisher, @Nonnull final I18nService i18nService,
      @Nonnull final IApplicationProperties applicationProperties) {
    this.preferencesManager = checkNotNull(preferencesManager, "preferencesManager");
    this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
    this.i18nService = checkNotNull(i18nService, "i18nService");
    this.builder = new PathBuilder<>(QKeyStoreEntry.keyStoreEntry.getType(),
        QKeyStoreEntry.keyStoreEntry.getMetadata());
    final DefaultEvaluatorFactory evaluatorFactory = new DefaultEvaluatorFactory(CollQueryTemplates.DEFAULT);
    this.queryEngine = new DefaultQueryEngine(evaluatorFactory);
  }

  /**
   * Initialize Keystore.
   *
   * @throws Exception
   *                   if error occurs
   */
  @PostConstruct
  public void init() throws Exception {
    // useful? enforce the creation of keystore if doesn't exist
    //
    // getKeyStore();
    // final String path = this.location.getFile().getAbsolutePath();
    // System.setProperty("javax.net.ssl.keyStorePassword", password);
    // System.setProperty("javax.net.ssl.keyStore", path);
    // System.setProperty("javax.net.ssl.trustStorePassword", password);
    // System.setProperty("javax.net.ssl.trustStore", path);

  }

  /**
   * @return Returns a {@link CollQuery} to fetch keys stored in {@link KeyStore}
   */
  protected CollQuery<KeyStoreEntry> query() {
    return new CollQuery<>(queryEngine);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Only for internal use")
  public KeyStoreProperties getConfiguration() {
    return this.applicationProperties.getConfiguration(KeyStoreProperties.class);
  }

  /**
   * @return Returns location where store keystore
   */
  @Nonnull
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public Resource getLocation() {
    return getConfiguration().getLocation();
  }

  /** {@inheritDoc} */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  @Nullable
  public KeyStoreEntry get(@Nonnull final String alias) {
    checkHasText(alias, "alias");
    final QKeyStoreEntry entity = QKeyStoreEntry.keyStoreEntry;
    return query().from(entity, getEntries()).where(entity.alias.equalsIgnoreCase(alias)).fetchOne();
  }

  /**
   * {@inheritDoc}
   */
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or hasGlobalPermission('ADMIN')")
  @Override
  public Page<KeyStoreEntry> findAll(@Nonnull final Pageable pageRequest) {
    checkNotNull(pageRequest, "pageRequest");
    final QKeyStoreEntry entity = QKeyStoreEntry.keyStoreEntry;
    final Predicate predicates = DslPagingHelper.createPredicates(pageRequest, entity, (Predicate[]) null);

    final CollQuery<KeyStoreEntry> query = DslPagingHelper
        .applyPagination(pageRequest, builder, query().from(entity, getEntries()).where(predicates));
    final QueryResults<KeyStoreEntry> results = query.fetchResults();
    final long totalElements = results.getTotal();
    return PageUtils.createPage(results.getResults(), pageRequest, totalElements);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Used internal to check certificate expiration in scheduled task")
  public Page<KeyStoreEntry> findAllWithExpireDateBefore(@Nonnull final Pageable pageRequest, final DateTime date) {
    checkNotNull(pageRequest, "pageRequest");
    checkNotNull(date, "date");
    final QKeyStoreEntry entity = QKeyStoreEntry.keyStoreEntry;
    final CollQuery<KeyStoreEntry> query = DslPagingHelper.applyPagination(pageRequest,
        builder,
        query().from(entity, getEntries()).where(QKeyStoreEntry.keyStoreEntry.expiredDate.before(date)));
    final QueryResults<KeyStoreEntry> results = query.fetchResults();
    final long totalElements = results.getTotal();
    return PageUtils.createPage(results.getResults(), pageRequest, totalElements);
  }

  /**
   * {@inheritDoc}
   */
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or hasGlobalPermission('ADMIN')")
  @Override
  public List<KeyStoreEntry> getEntries() {
    final List<KeyStoreEntry> list = Lists.newArrayList();
    try {
      for (final String alias : getAliases()) {
        EntryType entryType = null;
        final KeyStore keystore = getKeyStore();
        final KeyStoreEntryBuilder keyStoreEntry = KeyStoreEntry.builder().alias(alias);

        final Date expiredDate = KeyStoreHelper.getCertificateExpiry(alias, keystore);
        if (expiredDate != null) {
          keyStoreEntry.expiredDate(new DateTime(expiredDate));
        }
        Date lastModified = null;
        if (!keystore.getType().equals(KeyStoreType.PKCS12.value())) {
          lastModified = keystore.getCreationDate(alias);
        }
        if (lastModified != null) {
          keyStoreEntry.lastModified(new DateTime(lastModified));
        }

        final KeyInfo keyInfo = KeyStoreHelper.getKeyInfo(alias, keystore, null);
        if (keyInfo != null) {
          keyStoreEntry.algorithm(keyInfo.getAlgorithm()).keySize(keyInfo.getSize());
        }
        if (KeyStoreHelper.isTrustedCertificateEntry(alias, keystore)) {
          entryType = EntryType.TrustedCertificate;
        } else if (KeyStoreHelper.isKeyPairEntry(alias, keystore)) {
          entryType = EntryType.KeyPair;
        } else if (KeyStoreHelper.isKeyEntry(alias, keystore)) {
          entryType = EntryType.Key;
        } else {
          continue;
        }
        keyStoreEntry.type(entryType);

        keyStoreEntry.valid(KeyStoreHelper.getValidity(alias, keystore));
        keyStoreEntry.expired(KeyStoreHelper.getExpired(alias, keystore));
        list.add(keyStoreEntry.build());
      }
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return list;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or hasGlobalPermission('ADMIN')")
  @Nonnull
  public List<String> getAliases() {
    try {
      return Collections.list(getKeyStore().aliases());
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public void remove(@Nonnull final String alias) {
    checkHasText(alias, "alias");
    final KeyStore keystore = this.getKeyStore();
    try {
      keystore.deleteEntry(alias);
      save(keystore, this.getConfiguration().getLocation().getFile(), getConfiguration().getPassword());
      clearPreference(alias);
      this.eventPublisher.publish(new KeyDeletedEvent(this, alias));
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN') or hasGlobalPermission('ADMIN')")
  public boolean isEntryExist(@Nonnull final String alias) {
    checkHasText(alias, "alias");
    try {
      return getAliases().contains(alias);
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Only for internal use")
  @Nullable
  public Optional<Certificate> getCertificate(@Nonnull final String alias) {
    checkHasText(alias, "alias");
    try {
      return Optional.ofNullable(getKeyStore().getCertificate(alias));
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public void storeCertificate(@Nonnull final Certificate certificate, @Nonnull final String alias) {
    checkNotNull(certificate, "certificate");
    checkHasText(alias, "alias");
    final KeyStore keystore = getKeyStore();
    try {
      // Add the certificate
      keystore.setCertificateEntry(alias, certificate);
      final boolean isNew = !this.isEntryExist(alias);
      save(keystore, this.getConfiguration().getLocation().getFile(), this.getConfiguration().getPassword());
      clearPreference(alias);
      if (isNew) {
        this.eventPublisher.publish(new KeyCreatedEvent(this, alias));
      } else {
        this.eventPublisher.publish(new KeyUpdatedEvent(this, alias));
      }
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public void storeKey(@Nonnull final KeyPair keypair, @Nonnull final String alias) {
    this.storeKey(keypair, alias, getConfiguration().getPassword());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public void storeKey(@Nonnull final KeyPair keypair, @Nonnull final String alias, @Nonnull final String password) {
    Assert.checkNotNull(keypair, "keypair");
    Assert.checkHasText(alias, "alias");
    Assert.checkHasText(password, "password");
    final KeyStore keystore = getKeyStore();
    try {
      final boolean isNew = !this.isEntryExist(alias);
      keystore.setKeyEntry(alias, keypair.getPrivateKey(), password.toCharArray(), keypair.getCertificateChain());
      save(keystore, this.getConfiguration().getLocation().getFile(), this.getConfiguration().getPassword());
      clearPreference(alias);
      if (isNew) {
        this.eventPublisher.publish(new KeyCreatedEvent(this, alias));
      } else {
        this.eventPublisher.publish(new KeyUpdatedEvent(this, alias));
      }
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * @see KeyStore#load(InputStream, char[])
   */
  @Override
  @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
  public String validateKeyPair(@Nonnull final InputStream keypairFile, @Nonnull final String password) {
    Assert.checkNotNull(keypairFile, "keypairFile");
    Assert.checkHasText(password, "password");

    try {
      final KeyPair keypair = KeyPairHelper.extractKeyPairPkcs12(keypairFile, password);
      final List<X509Certificate> x509Certs = X509CertHelper
          .orderX509CertChain(Arrays.asList(keypair.getCertificateChain()));
      return X509CertHelper.getCertificateAlias(Iterables.getFirst(x509Certs, null));
    } catch (final IOException | NoSuchProviderException | KeyStoreException | NoSuchAlgorithmException
        | UnrecoverableKeyException | CertificateException e) {
      // see KeyStore.load function for explanation
      if (e instanceof IOException && e.getCause() instanceof UnrecoverableKeyException) {
        throw new KeystoreWrongPasswordException(
            i18nService.createKeyedMessage("app.service.keystore.keypair.wrongpassword"));
      }
      throw new KeystoreException(i18nService.createKeyedMessage("app.service.keystore.keypair.internalerror"));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Only for internal use")
  @Nullable
  public Optional<Key> getKey(final String alias) {
    return getKey(alias, getConfiguration().getPassword());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Unsecured("Only for internal use")
  @Nullable
  public Optional<Key> getKey(final String alias, final String password) {
    Assert.checkHasText(alias, "alias");
    final char[] pwd = Assert.checkHasText(password, "password").toCharArray();
    try {
      return Optional.ofNullable(getKeyStore().getKey(alias, pwd));
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * @return Returns a {@link KeyStore} representing the current {@link KeyStore}
   *         if exists, otherwise create it.
   */
  @VisibleForTesting
  protected KeyStore getKeyStore() {
    final Resource location = getConfiguration().getLocation();
    final Resource defaultLocation = getConfiguration().getDefaultLocation();
    try {
      if (!location.exists()) {
        final File f = location.getFile();
        f.getParentFile().mkdirs();
        KeyStore keystore = null;
        if (defaultLocation != null && defaultLocation.exists()) {
          keystore = KeyStoreHelper.load(defaultLocation.getFile(),
              getConfiguration().getPassword(),
              this.keyStoreType.value());
        } else {
          keystore = create(this.keyStoreType.value());
        }
        save(keystore, location.getFile(), getConfiguration().getPassword());
      }
      return KeyStoreHelper.load(location.getFile(), getConfiguration().getPassword(), this.keyStoreType.value());
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  @Unsecured("Only for internal use")
  public KeyManager[] getKeyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
    final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(getKeyStore(), this.getConfiguration().getPassword().toCharArray());
    return kmf.getKeyManagers();
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  @Unsecured("Only for internal use")
  public TrustManager[] getTrustManagers() throws NoSuchAlgorithmException, KeyStoreException {
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(getKeyStore());
    return tmf.getTrustManagers();
  }

  /**
   * Does the supplied KeyStore contain any key entries? ie any entries that
   * contain a key with no certificate chain.
   *
   * @param keyStore
   *                 KeyStore
   * @return True if it does
   */
  @VisibleForTesting
  protected static boolean containsKey(@Nonnull final KeyStore keyStore) {
    Assert.checkNotNull(keyStore, "keyStore");
    try {
      final Enumeration<String> aliases = keyStore.aliases();

      while (aliases.hasMoreElements()) {
        final String alias = aliases.nextElement();

        if (KeyStoreHelper.isKeyEntry(alias, keyStore)) {
          return true;
        }
      }

      return false;
    } catch (final KeyStoreException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Save a KeyStore to a file protected by a password.
   *
   * @param keyStore
   *                     The KeyStore
   * @param keyStoreFile
   *                     The file to save the KeyStore to
   * @param password
   *                     The password to protect the KeyStore with
   */
  @VisibleForTesting
  protected static void save(@Nonnull final KeyStore keyStore,
      @Nonnull final File keyStoreFile,
      @Nonnull final String password) {
    checkNotNull(keyStore, "keyStore");
    checkNotNull(keyStore, "keyStoreFile");
    checkHasText(password, "password");
    FileOutputStream fos = null;

    try {
      fos = new FileOutputStream(keyStoreFile);
      keyStore.store(fos, password.toCharArray());
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      try {
        Closeables.close(fos, false);
      } catch (final IOException e) {
      }
    }
  }

  /**
   * Create new {@link KeyStore}.
   *
   * @param keyStoreType
   *                     the type of keystore (can <b>not</b> be null).
   * @return Returns a new instance of {@link KeyStore} of the specified type.
   */
  @VisibleForTesting
  @Nonnull
  protected static KeyStore create(@Nonnull final String keyStoreType) {
    checkHasText(keyStoreType, "keyStoreType");
    final KeyStore keyStore = KeyStoreHelper.getKeyStoreInstance(keyStoreType);
    try {
      keyStore.load(null, null);
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }

    return keyStore;
  }

  /**
   * Copy a KeyStore.
   *
   * @param keyStore
   *                 KeyStore to copy
   * @return Copy
   */
  @VisibleForTesting
  @Nonnull
  protected static KeyStore copy(@Nonnull final KeyStore keyStore) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      final char[] emptyPassword = {};

      keyStore.store(baos, emptyPassword);

      final KeyStore theCopy = create(keyStore.getType());
      theCopy.load(new ByteArrayInputStream(baos.toByteArray()), emptyPassword);

      return theCopy;
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void clearPreference(@Nonnull final String alias) {
    checkNotNull(alias, "alias");
    final IKeyStorePreferences pref = preferencesManager.getPreferences(alias);
    try {
      if (pref != null && pref.exists(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION)) {
        pref.remove(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION);
      }
    } catch (final ApplicationException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
    preferencesManager.clearCache(alias);
  }
}
