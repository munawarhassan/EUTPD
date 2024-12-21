package com.pmi.tpd.core.mail;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.util.Assert;

/**
 * Service for sending e-mails.
 * <p/>
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
@Named
@Singleton
public class DefaultMailService implements IMailService, IStartable, IShutdown {

    /** */
    private static final String SMTP_AUTH = "mail.smtp.auth";

    /** */
    private static final String SMTP_START_TLS = "mail.smtp.starttls.enable";

    /** */
    // private static final String TRANSPORT_PROTOCOL = "mail.transport.protocol";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMailService.class);

    /** */
    private JavaMailSender javaMailSender;

    /** */
    private MailProperties mailProperties;

    /** */
    private final ITemplateEngine templateEngine;

    /** */
    private boolean started = false;

    /**
     *
     */
    public DefaultMailService(@Nonnull final MailProperties mailProperties,
            @Nonnull final ITemplateEngine templateEngine) {
        this.mailProperties = checkNotNull(mailProperties, "mailProperties");
        this.templateEngine = checkNotNull(templateEngine, "templateEngine");
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        javaMailSender = createJavaMailSender(mailProperties);
        try {
            checkConnection(javaMailSender);
            started = true;
        } catch (final Exception e) {
            LOGGER.error("Error occurred while starting Mail Service.", e);
        }
    }

    @Override
    public void shutdown() {
        started = false;
    }

    @Override
    public boolean isHostConfigured() {
        return started;
    }

    @EventListener
    public void onMailConfigurationChangedEvent(final ConfigurationChangedEvent<MailProperties> event)
            throws Exception {
        if (!event.isAssignable(MailProperties.class)) {
            return;
        }
        this.mailProperties = event.getNewConfiguration();
        if (started) {
            shutdown();
        }
        start();
    }

    /**
     * @return the javaMailSender
     */
    @Override
    public JavaMailSender getJavaMailSender() {
        return javaMailSender;
    }

    @Override
    @Async
    public void sendNow(final MailMessage message) throws MailException {
        sendEmail(getJavaMailSender(),
            Strings.isNullOrEmpty(message.getFrom()) ? this.mailProperties.getFrom() : message.getFrom(),
            message.getTo(),
            message.getSubject(),
            message.getText(),
            false,
            true);

    }

    @Override
    public void sendTest(@Nonnull final MailProperties mailProperties,
        @Nonnull final String from,
        @Nonnull final String to) {
        Assert.checkHasText(to, "to");
        final JavaMailSender mailSender = createJavaMailSender(mailProperties);
        checkConnection(mailSender);

        final Context context = new Context(Locale.getDefault());

        final String subject = "Test mail Connection";
        final String content = templateEngine.process("testConnection", context);
        sendEmail(mailSender, from, ImmutableSet.of(to), subject, content, false, true);
    }

    protected void sendEmail(final JavaMailSender sender,
        final String from,
        final Set<String> to,
        final String subject,
        final String content,
        final boolean isMultipart,
        final boolean isHtml) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Send e-mail[multipart '{}' and html '{}'] to '{}', from '{}' with subject '{}' and content={}",
                isMultipart,
                isHtml,
                to,
                from,
                subject,
                content);
        }

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = sender.createMimeMessage();
        try {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart,
                    ApplicationConstants.getDefaultCharset().name());
            message.setTo(to.toArray(new String[to.size()]));
            message.setFrom(from);
            message.setSubject(subject);
            message.setText(content, isHtml);
            sender.send(mimeMessage);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sent e-mail to User '{}'", to);
            }
        } catch (final Exception e) {
            LOGGER.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
        }
    }

    @Nonnull
    protected JavaMailSender createJavaMailSender(@Nonnull final MailProperties mailProperties) {
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setDefaultEncoding(mailProperties.getDefaultEncoding());

        final Properties properties = new Properties();

        sender.setHost(mailProperties.getHost());
        if (mailProperties.getPort() != null) {
            sender.setPort(mailProperties.getPort());
        }
        if (StringUtils.isNotBlank(mailProperties.getUser())) {
            sender.setUsername(mailProperties.getUser());
            // if a username is provided, the mail session needs to be configured with the mail.smtp.auth property set
            // to true. See JavaMailSenderImpl javadoc
            properties.setProperty(SMTP_AUTH, "true");
            if (StringUtils.isNotBlank(mailProperties.getUser())) {
                sender.setPassword(mailProperties.getPassword());
            }
        }
        if (mailProperties.isTls()) {
            properties.setProperty(SMTP_START_TLS, "true");
        }

        sender.setJavaMailProperties(properties);
        return sender;
    }

    protected void checkConnection(@Nonnull final JavaMailSender sender) {
        try {
            final JavaMailSenderImpl javaMailSender = (JavaMailSenderImpl) sender;
            javaMailSender.getSession()
                    .getTransport()
                    .connect(javaMailSender.getHost(),
                        javaMailSender.getPort(),
                        javaMailSender.getUsername(),
                        javaMailSender.getPassword());

        } catch (final MessagingException e) {
            throw new RuntimeException("Cannot connect to e-mail server. Error: " + e.getMessage());
        }
    }

}
