package com.pmi.tpd.core.user.permission;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.core.exception.RequestCanceledException;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class PermissionModificationCanceledException extends RequestCanceledException {

    /**
     *
     */
    private static final long serialVersionUID = -1200586835416968017L;

    public PermissionModificationCanceledException(@Nonnull final KeyedMessage message,
            @Nonnull final List<KeyedMessage> cancelMessages) {
        super(message, cancelMessages);
    }
}
