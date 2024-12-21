package com.pmi.tpd.core.avatar.spi;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import com.pmi.tpd.core.avatar.AbstractAvatarSupplier;

/**
 * @since 2.4
 */
public class ResourceAvatarSupplier extends AbstractAvatarSupplier {

    private final ClassPathResource resource;

    public ResourceAvatarSupplier(final String path) {
        this(MediaType.IMAGE_PNG_VALUE, path);
    }

    public ResourceAvatarSupplier(final String contentType, final String path) {
        super(contentType);

        resource = new ClassPathResource(path);
    }

    @Nonnull
    @Override
    public InputStream open() throws IOException {
        return resource.getInputStream();
    }

    public ClassPathResource getResource() {
        return resource;
    }
}
