package com.pmi.tpd.web.rest.avatar;

import static com.pmi.tpd.api.util.Assert.notNull;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.pmi.tpd.core.avatar.AbstractAvatarSupplier;

/**
 * @since 2.4
 */
public class FilePartAvatarSupplier extends AbstractAvatarSupplier {

    /** */
    @Nonnull
    private final InputStream inputStream;

    public FilePartAvatarSupplier(@Nonnull final FormDataBodyPart file, @Nonnull final InputStream inputStream) {
        super(notNull(file, "file").getMediaType().toString());
        this.inputStream = notNull(inputStream, "inputStream");
    }

    @Nonnull
    @Override
    public InputStream open() throws IOException {
        return inputStream;
    }
}
