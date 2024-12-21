package com.pmi.tpd.euceg.backend.core.spi;

import static com.pmi.tpd.api.util.Assert.checkHasText;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.core.io.Resource;

import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.api.crypto.KeyStoreHelper;
import com.pmi.tpd.api.crypto.KeyStoreType;
import com.pmi.tpd.api.util.Assert;

public class SimpleKeyProvider implements IKeyProvider {

    private final URL location;

    private final Resource resource;

    private final String password;

    private final KeyStoreType keyStoreType = KeyStoreType.JKS;

    public SimpleKeyProvider(final @Nonnull URL location, final String password) {
        this.location = Assert.checkNotNull(location, "location");;
        this.password = password;
        this.resource = null;
    }

    public SimpleKeyProvider(final @Nonnull Resource resource, final String password) {
        this.location = null;
        this.password = password;
        this.resource = Assert.checkNotNull(resource, "resource");
    }

    @Override
    @Nonnull
    public Optional<Key> getKey(final @Nonnull String alias) {
        return getKey(alias, password);
    }

    @SuppressWarnings("null")
    @Override
    @Nonnull
    public Optional<Certificate> getCertificate(@Nonnull final String alias) {
        checkHasText(alias, "alias");
        try {
            return Optional.ofNullable(getKeyStore().getCertificate(alias));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("null")
    @Override
    @Nonnull
    public Optional<Key> getKey(final String alias, final String password) {
        Assert.checkHasText(alias, "alias");
        final char[] pwd = Assert.checkHasText(password, "password").toCharArray();
        try {
            return Optional.ofNullable(getKeyStore().getKey(alias, pwd));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected KeyStore getKeyStore() {
        try (InputStream in = openStream()) {
            return KeyStoreHelper.load(in, password, this.keyStoreType.value());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("null")
    @Nonnull
    private InputStream openStream() throws IOException {
        if (resource != null) {
            return resource.getInputStream();
        }
        if (location != null) {
            return location.openStream();
        }
        throw new RuntimeCryptoException("keystore location should be filled");
    }

}
