package com.pmi.tpd.core.euceg.event;

public class EucegChannels {

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for euceg business actions.
     */
    public static final String EUCEG = "audit.channel.euceg";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for product actions.
     */
    public static final String PRODUCT = "audit.channel.euceg.product";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for submission actions.
     */
    public static final String SUBMISSION = "audit.channel.euceg.submission";

    /**
     * Used to notify listeners that the event should be displayed in the audit UI for attachment actions.
     */
    public static final String ATTACHMENT = "audit.channel.euceg.attachment";
}
