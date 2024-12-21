package com.pmi.tpd.metrics.heath;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.mail.IMailService;

/**
 * SpringBoot Actuator HealthIndicator check for JavaMail.
 */
public class JavaMailHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaMailHealthIndicator.class);

    private final IMailService mailService;

    public JavaMailHealthIndicator(final IMailService mailService) {
        this.mailService = Assert.notNull(mailService, "mailService must not be null");;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initializing JavaMail health indicator");
        }
        try {
            final JavaMailSenderImpl javaMailSender = (JavaMailSenderImpl) mailService.getJavaMailSender();
            javaMailSender.getSession().getTransport().connect(javaMailSender.getHost(),
                javaMailSender.getPort(),
                javaMailSender.getUsername(),
                javaMailSender.getPassword());

            builder.up().withDetail("type", "server mail").withDetail("product", javaMailSender.getHost());

        } catch (final MessagingException e) {
            LOGGER.debug("Cannot connect to e-mail server. Error: {}", e.getMessage());
            builder.down(e);
        }
    }
}
