package com.pmi.tpd.euceg.backend.core.delivery;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AS4Payload;
import org.eu.ceg.AppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.IEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;

public class DefaultDeliverySenderCreator implements IDeliverySenderMessageCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDeliverySenderCreator.class);

    private static final String CONTENT_TYPE = "application/xml";

    private final IEncryptionProvider encryptionProvider;

    private final I18nService i18nService;

    public DefaultDeliverySenderCreator(final IEncryptionProvider encryptionProvider, final I18nService i18nService) {
        this.encryptionProvider = encryptionProvider;
        this.i18nService = i18nService;
    }

    @Override
    public AppResponse createPayloadResponse(final @Nonnull DataSource source) {
        try {
            final byte[] clearPayload = encryptionProvider.decryptContent(source);
            return Eucegs.unmarshal(clearPayload);
        } catch (final BackendException e) {
            LOGGER.error("Error while decrypting content");
            throw new EucegException(
                    i18nService.createKeyedMessage("app.service.euceg.backend.decryptioncontent.failed"), e);
        }

    }

    @Override
    @Nonnull
    public DataSource createRequestPayload(@Nonnull final Object payload, final @Nullable Path workingDirectory)
            throws IOException, BackendException {
        checkNotNull(payload, "payload");
        // create AS4Payload with encrypted payload
        final AS4Payload as4Payload = encryptionProvider.createAs4Payload(payload, workingDirectory);
        // store xml as4payload in file
        if (workingDirectory != null) {
            final File as4PayloadFile = Eucegs.marshallInFile(as4Payload, workingDirectory);
            final FileDataSource fileDS = new FileDataSource(as4PayloadFile) {

                @Override
                public String getContentType() {
                    return CONTENT_TYPE;
                }
            };
            return fileDS;
        } else {
            return new ByteArrayDataSource(Eucegs.marshal(as4Payload).getBytes(), CONTENT_TYPE);
        }
    }

}