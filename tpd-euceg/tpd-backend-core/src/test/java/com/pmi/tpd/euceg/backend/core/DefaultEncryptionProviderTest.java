package com.pmi.tpd.euceg.backend.core;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.activation.DataHandler;

import org.bouncycastle.util.encoders.Hex;
import org.eu.ceg.AS4Payload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.io.ByteStreams;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.backend.core.spi.AesKeyGenerator;
import com.pmi.tpd.euceg.backend.core.spi.IKeyGenerator;
import com.pmi.tpd.euceg.backend.core.spi.SimpleKeyProvider;
import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultEncryptionProviderTest extends MockitoTestCase {

    @Spy
    private final BackendProperties backendPropertiesSender = BackendProperties.builder()
            .finalRecipient("finalRecipient")
            .fromPartyId("fromPartyId")
            .keyPairAlias("ACC-EUCEG-99962-AS4")
            .originalSender("originalSender")
            .password("password")
            .toPartyId("toPartyId")
            .partyIdType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:EUCEG")
            .trustedCertificateAlias("EUCEG_EC")
            .url("http://service.com/backend")
            .username("username")
            .build();

    @Spy
    private final IKeyProvider keyProviderSender = new SimpleKeyProvider(getResource("keystore-sender.jks"), "test123");

    @Spy
    private final IKeyGenerator keyGenerator = new AesKeyGenerator();

    @Mock(lenient = true)
    private IApplicationProperties applicationProperties;

    @InjectMocks
    private DefaultEncryptionProvider encryptionProviderSender;

    private final IKeyProvider keyProviderReceiver = new SimpleKeyProvider(getResource("keystore-receiver.jks"),
            "test123");

    private final BackendProperties backendPropertiesReceiver = BackendProperties.builder()
            .finalRecipient("finalRecipient")
            .fromPartyId("fromPartyId")
            .keyPairAlias("EUCEG_EC")
            .originalSender("originalSender")
            .password("password")
            .partyIdType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:EUCEG")
            .toPartyId("toPartyId")
            .trustedCertificateAlias("ACC-EUCEG-99962-AS4")
            .url("http://service.com/backend")
            .username("username")
            .build();

    private DefaultEncryptionProvider encryptionProviderReciever;

    @TempDir
    public File anotherTempDir;

    @BeforeEach
    public void setup() {
        encryptionProviderReciever = new DefaultEncryptionProvider(keyProviderReceiver);
        encryptionProviderReciever.setBackendProperties(backendPropertiesReceiver);
        when(applicationProperties.getConfiguration(eq(BackendProperties.class))).thenReturn(backendPropertiesSender);
    }

    @Test
    public void testEncryptPayloadWithFixedKey() throws Throwable {
        // fix generate key for test
        doReturn(Hex.decode("4ee05c43de6ccf29862509011e7ea35d9840497aa304af1d22b9f493a9052303")).when(keyGenerator)
                .getEncodedKey();
        final AS4Payload encryptedPayload = encryptionProviderSender.createAs4Payload("payload",
            anotherTempDir.toPath());
        assertNotNull(encryptedPayload);
        assertNotNull(encryptedPayload.getDocumentHash());
        assertEquals(128, encryptedPayload.getDocumentHash().length(), "DocumentHash should be 128 bytes long");
        assertNotNull(encryptedPayload.getContent());
        final byte[] encryptedByte = ByteStreams.toByteArray(encryptedPayload.getContent().getInputStream());
        assertEquals("1hdDXkufvIL49k8dkK+uTw==",
            Base64.getEncoder().encodeToString(encryptedByte),
            "Encrypted content should be equals to encrypted test content");
        assertNotNull(encryptedPayload.getKey());
    }

    @Test
    public void testEncrypt_DecryptPayload() throws Throwable {
        final AS4Payload encryptedPayload = encryptionProviderSender.createAs4Payload("payload",
            anotherTempDir.toPath());
        final String xml = Eucegs.marshal(encryptedPayload);
        final byte[] decryptedByte = encryptionProviderReciever
                .decryptContent(new ByteArrayDataSource(xml, "text/xml"));
        assertEquals("payload", new String(decryptedByte));
    }

    @Test
    public void testWithWrongTrustedCertificateAliasName() {
        doReturn("wrong_name").when(backendPropertiesSender).getTrustedCertificateAlias();
        Assertions.assertThrows(BackendException.class,
            () -> encryptionProviderSender.createAs4Payload("payload", anotherTempDir.toPath()));
    }

    @Test
    public void testWithWrongKeyPairName() throws IOException {
        doReturn("wrong_name").when(backendPropertiesSender).getKeyPairAlias();
        final String xml = Eucegs
                .marshal(new AS4Payload().withContent(new DataHandler(new ByteArrayDataSource("payload", "text/plain")))
                        .withDocumentHash("hash code")
                        .withKey("key".getBytes()));

        Assertions.assertThrows(BackendException.class,
            () -> encryptionProviderSender.decryptContent(new ByteArrayDataSource(xml, "text/xml")));
    }

}
