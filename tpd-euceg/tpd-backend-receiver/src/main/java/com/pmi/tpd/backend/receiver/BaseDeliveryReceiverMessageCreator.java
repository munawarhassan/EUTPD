package com.pmi.tpd.backend.receiver;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;

import org.eu.ceg.AS4Payload;
import org.eu.ceg.AppResponse;
import org.eu.ceg.ErrorResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.backend.core.IEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.delivery.IDeliveryReceiverMessageCreator;
import com.pmi.tpd.euceg.backend.core.delivery.RejectedMessageException;
import com.pmi.tpd.euceg.backend.core.message.IBackendErrorMessage;
import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;

public class BaseDeliveryReceiverMessageCreator implements IDeliveryReceiverMessageCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDeliveryReceiverMessageCreator.class);

    private static final String CONTENT_TYPE = "application/xml";

    private final IEncryptionProvider encryptionProvider;

    public BaseDeliveryReceiverMessageCreator(@Nonnull final IEncryptionProvider encryptionProvider) {
        this.encryptionProvider = checkNotNull(encryptionProvider, "encryptionProvider");
    }

    @Override
    public Object createIncommingPayload(final DataSource source) {
        final byte[] bytes = encryptionProvider.decryptContent(source);
        return Eucegs.unmarshal(bytes);
    }

    @Override
    public DataSource createResponsePayload(final Object incomingPayload,
        final String messageId,
        final Path workingDirectory) {
        AppResponse appResponse = null;

        try {
            appResponse = doCreateResponse(incomingPayload, messageId, workingDirectory);
        } catch (final RejectedMessageException e) {
            final IBackendErrorMessage errorMessage = e.getFailureMessage();
            appResponse = new ErrorResponse().withCode(errorMessage.getErrorCode())
                    .withMessage(errorMessage.getErrorDetail())
                    .withUuid(errorMessage.getMessageId())
                    .withDate(DateTime.now());
        }

        final String payload = Eucegs.marshal(appResponse);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Receiver  payload:{}", payload);
        }
        // create AS4Payload with encrypted payload
        final AS4Payload as4Payload = encryptionProvider.createAs4Payload(payload, workingDirectory);
        // store xml as4payload in file
        if (workingDirectory != null) {
            File as4PayloadFile = null;
            try {
                as4PayloadFile = Eucegs.marshallInFile(as4Payload, workingDirectory);
                return new FileDataSource(as4PayloadFile) {

                    @Override
                    public String getContentType() {
                        return CONTENT_TYPE;
                    }
                };
            } catch (final IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

        } else {
            return new ByteArrayDataSource(Eucegs.marshal(as4Payload).getBytes(), CONTENT_TYPE);
        }
    }

    protected AppResponse doCreateResponse(final Object incomingPayload,
        final String messageId,
        final Path workingDirectory) throws RejectedMessageException {
        return null;
    }

}
