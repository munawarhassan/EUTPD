package com.pmi.tpd.core.exception;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class RequestCanceledException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -4750255033712241122L;

    private final List<KeyedMessage> cancelMessages;

    public RequestCanceledException(@Nonnull final KeyedMessage message,
            @Nonnull final List<KeyedMessage> cancelMessages) {
        super(message);

        checkArgument(!checkNotNull(cancelMessages, "cancelMessages").isEmpty(),
            "At least one cancellation message must be provided");

        this.cancelMessages = cancelMessages;
    }

    @Nonnull
    public List<KeyedMessage> getCancelMessages() {
        return cancelMessages;
    }
}
