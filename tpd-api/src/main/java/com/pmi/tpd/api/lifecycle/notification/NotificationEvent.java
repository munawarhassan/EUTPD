package com.pmi.tpd.api.lifecycle.notification;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public final class NotificationEvent {

    /** */
    private final NotificationRequest notification;

    /**
     * @param notification
     *            a notification request.
     */
    public NotificationEvent(@Nonnull final NotificationRequest notification) {
        this.notification = Assert.checkNotNull(notification, "notification");
    }

    /**
     * @return Returns the associated notification request.
     */
    public NotificationRequest getNotification() {
        return notification;
    }
}
