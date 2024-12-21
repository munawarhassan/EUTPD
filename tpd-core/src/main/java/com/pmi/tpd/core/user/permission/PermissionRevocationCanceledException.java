package com.pmi.tpd.core.user.permission;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.core.exception.RequestCanceledException;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class PermissionRevocationCanceledException extends RequestCanceledException {

    /**
     *
     */
    private static final long serialVersionUID = -8724164759058542421L;

    /**
     * @param message
     * @param cancelMessages
     */
    public PermissionRevocationCanceledException(@Nonnull final KeyedMessage message,
            @Nonnull final List<KeyedMessage> cancelMessages) {
        super(message, cancelMessages);
    }
}
