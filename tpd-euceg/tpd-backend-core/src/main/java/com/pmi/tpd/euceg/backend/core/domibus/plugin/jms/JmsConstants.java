package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

public final class JmsConstants {

    /**
     * This queue is used to inform the TDP about the message status after sending a message to access point
     */
    public static final String JMS_QUEUE_REPLY_NAME = "domibus.backend.jms.replyQueue";

    /**
     * #This queue is the entry point for messages to be sent to access point
     */
    public static final String JMS_QUEUE_IN_NAME = "domibus.backend.jms.inQueue";

    /**
     * This queue contains the received messages, TDP listens to this queue to consume the received messages from
     * responder
     */
    public static final String JMS_QUEUE_OUT_NAME = "domibus.backend.jms.outQueue";

    /**
     * This queue is used to inform the TDP that an error occurred during the processing of receiving a message
     */
    public static final String JMS_QUEUE_ERROR_NOTIFY_CONSUMER_NAME = "domibus.backend.jms.errorNotifyConsumer";

    /**
     * This queue is used to inform the client that an error occurred during the processing of sending a message
     */
    public static final String JMS_QUEUE_ERROR_NOTIFY_PRODUCER_NAME = "domibus.backend.jms.errorNotifyProducer";

    public static final String MESSAGE_TYPE_MESSAGE_SENT = "messageSent";

    public static final String MESSAGE_TYPE_RESPONSE_SUBMIT = "submitResponse";

}
