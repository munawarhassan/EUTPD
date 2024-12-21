package com.pmi.tpd.core.mail;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.user.IEmailNotifier;
import com.pmi.tpd.keystore.ICertificateMailNotifier;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.security.IAuthenticationContext;

/**
 * @author Christophe Friederich
 */
@Named
public class DefaultEmailNotifier implements IEmailNotifier, ICertificateMailNotifier {

    /** */
    public static final String TEMPLATE_EMAIL_CREATE_USER = "userCreatedWithoutPassword";

    /** */
    public static final String TEMPLATE_EMAIL_RESET_USER_PASSWORD = "userResetPassword";

    /** */
    public static final String TEMPLATE_CERTIFICATE_EXPIRED = "certificateExpired";

    /** */
    private final IMailService mailService;

    /** */
    private final ITemplateEngine templateEngine;

    /** */
    private final I18nService i18nService;

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final INavBuilder navBuilder;

    /**
     * @param mailService
     * @param messageSource
     * @param templateEngine
     * @param i18nService
     * @param authenticationContext
     * @param navBuilder
     */
    @Autowired
    public DefaultEmailNotifier(final IMailService mailService, @Nonnull final MessageSource messageSource,
            @Nonnull final ITemplateEngine templateEngine, @Nonnull final I18nService i18nService,
            @Nonnull final IAuthenticationContext authenticationContext, @Nonnull final INavBuilder navBuilder) {
        this.mailService = checkNotNull(mailService, "mailService");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.templateEngine = checkNotNull(templateEngine, "templateEngine");
        this.navBuilder = checkNotNull(navBuilder, "navBuilder");
    }

    @Override
    public void validateCanSendEmails() {
        if (!mailService.isHostConfigured()) {
            throw new NoMailHostConfigurationException(i18nService.createKeyedMessage("app.service.cant.send.emails"));
        }
    }

    @Override
    public void sendExpiredCertificate(@Nonnull final String contact, @Nonnull final KeyStoreEntry key)
            throws MailException {
        checkNotNull(key, "key");
        checkHasText(contact, "contact");
        validateCanSendEmails();
        final String subject = i18nService.getMessage("app.notification.mail.certificate.expired.subject",
            Product.getFullName());
        final String text = renderText(TEMPLATE_CERTIFICATE_EXPIRED,
            ImmutableMap.<String, Object> of("key", key, "product", Product.getFullName()),
            ApplicationConstants.getDefaultLocale());

        mailService.sendNow(MailMessage.builder().to(contact).text(text).subject(subject).build());
    }

    @Override
    public void sendCreatedUser(final UserRequest user, final String token) {
        validateCanSendEmails();

        final IUser invitingUser = authenticationContext.getCurrentUser().orElse(null);

        final String subject = i18nService.getMessage("app.notification.mail.welcome.subject", Product.getFullName());
        final String text = renderText(TEMPLATE_EMAIL_CREATE_USER,
            ImmutableMap.<String, Object> of("invitingUser",
                invitingUser,
                "newUser",
                user,
                "token",
                token,
                "baseUrl",
                navBuilder.buildBaseUrl()),
            getLocaleUser(user));

        send(user, subject, text);

    }

    @Override
    public void sendPasswordReset(final UserRequest user, final String token) {
        validateCanSendEmails();

        // This should localize to the locale the user had set when requesting the password reset
        final String subject = i18nService.getMessage("app.notification.mail.reset-password.subject",
            user.getDisplayName());
        final String text = renderText(TEMPLATE_EMAIL_RESET_USER_PASSWORD,
            ImmutableMap.<String, Object> of("date",
                FastDateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()),
                "user",
                user,
                "token",
                token,
                "baseUrl",
                navBuilder.buildBaseUrl()),
            getLocaleUser(user));

        send(user, subject, text);
    }

    private Locale getLocaleUser(final UserRequest user) {
        Locale locale = LocaleUtils.toLocale(user.getLangKey());
        if (locale == null) {
            locale = ApplicationConstants.getDefaultLocale();
        }
        return locale;
    }

    private void send(final UserRequest user, final String subject, final String text) {
        mailService.sendNow(MailMessage.builder().to(user.getEmail()).text(text).subject(subject).build());
    }

    private String renderText(final String template, final Map<String, Object> data, final Locale locale) {
        try {
            final Context context = new Context(locale, data);
            return templateEngine.process(template, context);
        } catch (final TemplateEngineException e) {
            throw new IllegalStateException("Error rendering email body", e);
        }
    }

}
