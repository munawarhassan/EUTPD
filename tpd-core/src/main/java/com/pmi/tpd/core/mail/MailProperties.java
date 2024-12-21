package com.pmi.tpd.core.mail;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * See <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a>.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties("app.mail")
public final class MailProperties {

    /**
     * SMTP server host.
     */
    private String host;

    /**
     * SMTP server port.
     */
    private Integer port;

    /**
     * Login user of the SMTP server.
     */
    private String user;

    /**
     * Login password of the SMTP server.
     */
    private String password;

    /**
     * Default MimeMessage encoding.
     */
    private String defaultEncoding = "UTF-8";

    /** */
    private boolean tls;

    /** */
    private String protocol;

    /** */
    private String from;

}
