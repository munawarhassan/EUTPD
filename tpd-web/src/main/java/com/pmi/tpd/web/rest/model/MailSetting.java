package com.pmi.tpd.web.rest.model;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.core.mail.MailProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Christophe Friederich
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
@Jacksonized
public class MailSetting {

    /** */
    private String hostname;

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private int port;

    /** */
    private Boolean tls;

    /** */
    private String emailFrom;

    /**
     * Create and initialise a new instance of {@link MailSetting} with given application properties.
     *
     * @param props
     *            a application properties
     * @return Returns new initialised instance of {@link MailSetting}.
     */
    @Nonnull
    public static MailSetting create(@Nonnull final IApplicationProperties applicationProperties) {
        final MailSetting setting = new MailSetting();
        final MailProperties mail = applicationProperties.getConfiguration(MailProperties.class);

        setting.hostname = mail.getHost();
        setting.username = mail.getUser();
        setting.password = mail.getPassword();
        setting.port = mail.getPort() == null ? 25 : mail.getPort();
        setting.tls = mail.isTls();
        setting.emailFrom = mail.getFrom();

        return setting;
    }

    /**
     * @param applicationProperties
     */
    public void save(final IApplicationProperties applicationProperties) {
        final MailProperties mail = applicationProperties.getConfiguration(MailProperties.class);

        mail.setHost(hostname);
        mail.setUser(username);
        mail.setPassword(password);
        mail.setPort(port);
        mail.setTls(tls == null ? false : tls);
        mail.setFrom(emailFrom);
        applicationProperties.storeConfiguration(mail);
    }

}
