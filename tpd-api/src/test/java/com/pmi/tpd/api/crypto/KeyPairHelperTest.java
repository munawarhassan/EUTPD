package com.pmi.tpd.api.crypto;

import java.io.IOException;
import java.security.UnrecoverableKeyException;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class KeyPairHelperTest extends TestCase {

    @Test
    public void shouldThrowPasswordException() throws Exception {
        try {
            KeyPairHelper.extractKeyPairPkcs12(getResourceAsStream("acc_euceg_99962_as4.p12"),
                "the_incorrect_password");
            fail();
        } catch (final IOException e) {
            // according to java.security.Keystore#load function
            if (!(e.getCause() instanceof UnrecoverableKeyException)) {
                fail();
            }
        }
    }

    @Test
    public void shouldOpenKeystore() throws Exception {
        final KeyPair result = KeyPairHelper.extractKeyPairPkcs12(getResourceAsStream("acc_euceg_99962_as4.p12"),
            "test123");
        assertNotNull(result);
    }
}
