package com.pmi.tpd.euceg.api.entity;

/**
 * the send status of attachment.
 * 
 * @author Christophe Friederich
 * @since 1.1
 */
public enum AttachmentSendStatus {
    /** The attachment has not been sent yet. */
    NO_SEND,
    /** The attachment is sending, but not received acknowledge yet. */
    SENDING,
    /** the attachment has been sent and received success. */
    SENT,
}
