package com.pmi.tpd.keystore;

import static com.pmi.tpd.api.paging.PageUtils.newRequest;
import static org.mockito.ArgumentMatchers.same;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.KeyPair;
import com.pmi.tpd.api.crypto.KeyPairHelper;
import com.pmi.tpd.api.crypto.KeyStoreHelper;
import com.pmi.tpd.api.crypto.X509CertHelper;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.keystore.preference.IKeyStorePreferences;
import com.pmi.tpd.keystore.preference.KeyStorePreferenceKeys;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultKeyStoreServiceTest extends AbstractServiceTest {

    /** */
    private static final String KEY_PAIRS_ALIAS = "acc-euceg-99962-as4";

    /** */
    private static final String KEY_EU_CEG_ALAIS = "euceg_ec";

    /** */
    private final Resource keyPairsLocation;

    /** */
    private final Resource certificatesLocation;

    /** */
    @Mock
    private IKeyStorePreferencesManager preferencesManager;

    @Mock(lenient = true)
    IApplicationProperties applicationProperties;

    private final I18nService i18nService = new SimpleI18nService();

    private IEventPublisher eventPublisher;

    /** */
    private final ResourceLoader resourceResolver = new DefaultResourceLoader();

    /** */
    private final Resource certLocaction;

    /** */
    private final Resource pkcs12Location;

    public Path temporaryFolder;

    @BeforeEach
    public void start(@TempDir final Path tempDir) {
        temporaryFolder = tempDir;
        this.eventPublisher = mock(IEventPublisher.class);
    }

    public DefaultKeyStoreServiceTest() throws IOException {
        super(DefaultKeyStoreService.class, IKeyStoreService.class);
        keyPairsLocation = getClassResource("key-pairs.jks");
        certificatesLocation = getClassResource("certificates.jks");
        certLocaction = getClassResource("euceg_ec.cer");
        pkcs12Location = getClassResource("acc_euceg_99962_as4.p12");
    }

    final String password = "test123";

    private DefaultKeyStoreService createKeyStoreService(final Resource location, final List<KeyStoreEntry> entries)
            throws Exception {
        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.setLocation(location);
        configuration.setPassword(password);
        when(applicationProperties.getConfiguration(eq(KeyStoreProperties.class))).thenReturn(configuration);

        final DefaultKeyStoreService service = new DefaultKeyStoreService(preferencesManager, eventPublisher,
                i18nService, applicationProperties) {

            @Override
            public List<KeyStoreEntry> getEntries() {
                if (entries != null) {
                    return entries;
                }
                return super.getEntries();
            }
        };
        service.init();
        return service;
    }

    private DefaultKeyStoreService copy(final DefaultKeyStoreService keyStoreService) throws Exception {
        final File newLocation = new File(temporaryFolder.toFile(), RandomUtil.uuid() + ".jks");
        Files.copy(keyPairsLocation.getInputStream(), newLocation.toPath());

        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.setLocation(resourceResolver.getResource(newLocation.toURI().toString()));
        configuration.setPassword(password);
        when(applicationProperties.getConfiguration(eq(KeyStoreProperties.class))).thenReturn(configuration);

        final DefaultKeyStoreService service = new DefaultKeyStoreService(preferencesManager, eventPublisher,
                i18nService, applicationProperties);
        service.init();
        return service;
    }

    private DefaultKeyStoreService emptyKeyStore() throws Exception {
        final File location = new File(temporaryFolder.toFile(), RandomUtil.uuid() + ".jks");
        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.setLocation(resourceResolver.getResource(location.toURI().toString()));
        configuration.setPassword(password);
        when(applicationProperties.getConfiguration(eq(KeyStoreProperties.class))).thenReturn(configuration);

        final DefaultKeyStoreService service = new DefaultKeyStoreService(preferencesManager, eventPublisher,
                i18nService, applicationProperties);
        service.init();
        return service;
    }

    @Test
    public void getEntriesWithCertificates() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        final List<KeyStoreEntry> list = keyStoreService.getEntries();
        assertEquals(3, list.size());
    }

    @Test
    public void getExistingAliases() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(keyPairsLocation, null);
        final List<String> aliases = keyStoreService.getAliases();
        assertNotNull(aliases);
        assertEquals(1, aliases.size());
        assertEquals(KEY_PAIRS_ALIAS, Iterables.getFirst(aliases, null));
    }

    @Test
    public void removeEntry() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(keyPairsLocation, null);
        final DefaultKeyStoreService temp = copy(keyStoreService);
        temp.remove(KEY_PAIRS_ALIAS);
        final List<String> aliases = temp.getAliases();
        assertThat(aliases, IsEmptyCollection.empty());
    }

    @Test
    public void getCertificateOnKeyPairs() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(keyPairsLocation, null);
        final Certificate certificate = keyStoreService.getCertificate(KEY_PAIRS_ALIAS).get();
        assertNotNull(certificate);
        assertEquals("X.509", certificate.getType());
    }

    @Test
    public void getKeyOnKeyPairs() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(keyPairsLocation, null);
        final Key key = keyStoreService.getKey(KEY_PAIRS_ALIAS, password).get();
        assertTrue(KeyStoreHelper.isKeyPairEntry(KEY_PAIRS_ALIAS, keyStoreService.getKeyStore()),
            "should be a keypair");
        assertNotNull(key);
    }

    @Test
    public void getCertificate() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        final Certificate certificate = keyStoreService.getCertificate(KEY_PAIRS_ALIAS).get();
        assertNotNull(certificate);
        assertEquals("X.509", certificate.getType());
    }

    @Test
    public void getKeyOnCertificate() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(keyPairsLocation, null);
        keyStoreService.getKey(KEY_PAIRS_ALIAS, password).get();
        assertFalse(KeyStoreHelper.isTrustedCertificateEntry(KEY_PAIRS_ALIAS, keyStoreService.getKeyStore()));
    }

    @Test
    public void addCertificate() throws Exception, ApplicationException {
        final DefaultKeyStoreService keyStoreService = emptyKeyStore();
        final IKeyStorePreferences preferences = mock(IKeyStorePreferences.class);

        when(this.preferencesManager.getPreferences(same(KEY_EU_CEG_ALAIS))).thenReturn(preferences);
        when(preferences.exists(same(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION))).thenReturn(true);

        final List<X509Certificate> certs = X509CertHelper.loadCertificates(certLocaction.getInputStream());
        assertEquals(1, certs.size());
        keyStoreService.storeCertificate(Iterables.getFirst(certs, null), KEY_EU_CEG_ALAIS);
        final Certificate certificate = keyStoreService.getCertificate(KEY_EU_CEG_ALAIS).get();
        assertNotNull(certificate);
        verify(preferences).remove(same(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION));
    }

    @Test
    public void addKey() throws Exception {
        final DefaultKeyStoreService keyStoreService = emptyKeyStore();
        try (InputStream in = pkcs12Location.getInputStream()) {
            final KeyPair keypair = KeyPairHelper.extractKeyPairPkcs12(in, password);
            keyStoreService.storeKey(keypair, KEY_PAIRS_ALIAS, password);
        }
        final Key key = keyStoreService.getKey(KEY_PAIRS_ALIAS, password).get();
        final List<String> aliases = keyStoreService.getAliases();
        assertEquals(1, aliases.size());
        assertNotNull(key);
        assertThat((PrivateKey) key, Is.isA(PrivateKey.class));
        assertEquals("RSA", key.getAlgorithm());

    }

    @Test
    public void findAllWithSimplePageableRequest() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        final Page<KeyStoreEntry> page = keyStoreService.findAll(newRequest(0, 20));
        assertEquals(3, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    public void findAllWithMutltiPage() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        Page<KeyStoreEntry> page = keyStoreService.findAll(newRequest(0, 2));
        assertEquals(2, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());
        assertTrue(page.hasNext());
        page = keyStoreService.findAll(page.nextPageable());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());

    }

    @Test
    public void findAllWithExpireDateBeforeWithSimplePageableRequest() throws Exception {
        final DateTime now = new DateTime();
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation,
            ImmutableList.<KeyStoreEntry> builder()
                    .add(KeyStoreEntry.builder()
                            .alias("cer-1-month")
                            .expiredDate(now.minusMonths(1).minusDays(5))
                            .build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-35-days").expiredDate(now.minusDays(35)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-1-year").expiredDate(now.minusYears(1)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-10-days").expiredDate(now.minusDays(10)).build())
                    .build());
        final Page<KeyStoreEntry> page = keyStoreService.findAllWithExpireDateBefore(newRequest(0, 20),
            now.minusDays(30));
        assertEquals(3, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());

    }

    @Test
    public void findAllWithExpireDateBeforeWithMutltiPage() throws Exception {
        final DateTime now = new DateTime();
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation,
            ImmutableList.<KeyStoreEntry> builder()
                    .add(KeyStoreEntry.builder()
                            .alias("cer-1-month")
                            .expiredDate(now.minusMonths(1).minusDays(5))
                            .build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-2-month").expiredDate(now.minusMonths(2)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-3-month").expiredDate(now.minusMonths(3)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-35-days").expiredDate(now.minusDays(35)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-1-year").expiredDate(now.minusYears(1)).build()) // ok
                    .add(KeyStoreEntry.builder().alias("cer-10-days").expiredDate(now.minusDays(10)).build())
                    .add(KeyStoreEntry.builder().alias("cer-1-days").expiredDate(now.minusDays(1)).build())
                    .add(KeyStoreEntry.builder().alias("cer+10-days").expiredDate(now.plusDays(10)).build())
                    .build());
        Page<KeyStoreEntry> page = keyStoreService.findAllWithExpireDateBefore(newRequest(0, 2), now.minusDays(30));
        assertEquals(2, page.getNumberOfElements());
        assertEquals(5, page.getTotalElements());
        assertTrue(page.hasNext());
        page = keyStoreService.findAllWithExpireDateBefore(page.nextPageable(), now.minusDays(30));
        assertEquals(2, page.getNumberOfElements());
        assertEquals(5, page.getTotalElements());

    }

    @Test
    public void getAlias() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        final KeyStoreEntry entry = keyStoreService.get(KEY_PAIRS_ALIAS);
        assertNotNull(entry);
        assertEquals(KEY_PAIRS_ALIAS, entry.getAlias());
    }

    @Test
    public void getAliases() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        final List<String> entry = keyStoreService.getAliases();
        assertNotNull(entry);
        assertEquals(3, entry.size());
    }

    @Test
    public void isEntryExist() throws Exception {
        final DefaultKeyStoreService keyStoreService = createKeyStoreService(certificatesLocation, null);
        assertTrue(keyStoreService.isEntryExist(KEY_EU_CEG_ALAIS));
        assertFalse(keyStoreService.isEntryExist("wrong"));
    }

    @Test
    public void storeKeyPair() throws Exception {
        final DefaultKeyStoreService keyStoreService = copy(createKeyStoreService(certificatesLocation, null));
        try (InputStream in = pkcs12Location.getInputStream()) {
            final KeyPair keypair = KeyPairHelper.extractKeyPairPkcs12(in, password);
            keyStoreService.storeKey(keypair, KEY_PAIRS_ALIAS);
        }
        assertEquals(1, keyStoreService.getAliases().size());
        assertTrue(keyStoreService.isEntryExist(KEY_PAIRS_ALIAS));
    }

    private final Resource getClassResource(final String resource) {
        return new ClassPathResource(resource, this.getClass());
    }

}
