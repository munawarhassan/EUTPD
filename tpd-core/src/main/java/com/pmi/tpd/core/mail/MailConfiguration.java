package com.pmi.tpd.core.mail;

import javax.annotation.Nonnull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.ITemplateEngine;

import com.pmi.tpd.spring.env.EnableConfigurationProperties;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({ MailProperties.class })
public class MailConfiguration {

    private final MailProperties mailProperties;

    public MailConfiguration(final MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    /**
     * @param applicationProperties
     * @param templateEngine
     * @return
     */
    @Bean
    public IMailService mailService(@Nonnull final ITemplateEngine templateEngine) {
        return new DefaultMailService(mailProperties, templateEngine);
    }

}
