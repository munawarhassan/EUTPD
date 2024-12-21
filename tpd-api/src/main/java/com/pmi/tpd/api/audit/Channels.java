package com.pmi.tpd.api.audit;

/**
 * Represents the set of built-in audit log channel names. Listeners may use the channels to determine whether they are
 * interested in the event.
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class Channels {

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for authentication.
     */
    public static final String AUTHENTICATION = "audit.channel.authentication";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for application configuration.
     */
    public static final String APPLICATION_CONFIGURATION = "audit.channel.application_configuration";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for security.
     */
    public static final String SECURITY = "audit.channel.security";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for permission.
     */
    public static final String PERMISSION = "audit.channel.permission";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for administration log.
     */
    public static final String ADMIN_LOG = "audit.channel.admin_log";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for keystore.
     */
    public static final String KEYSTORE = "audit.channel.keystore";

    private Channels() {
        throw new UnsupportedOperationException(getClass().getName() + " is not intended to be instantiated.");
    }
}