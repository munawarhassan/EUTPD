package com.pmi.tpd.service.mail;

import static java.util.Optional.of;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.google.common.io.Files;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.mail.DefaultEmailNotifier;
import com.pmi.tpd.core.mail.IMailService;
import com.pmi.tpd.core.mail.MailMessage;
import com.pmi.tpd.keystore.model.EntryType;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class EmailNotifierTest extends MockitoTestCase {

    private DefaultEmailNotifier emailNotifier;

    private ReloadableResourceBundleMessageSource messageSource;

    private SpringTemplateEngine templateEngine;

    private IMailService mailService;

    private I18nService i18nService;

    private INavBuilder navBuilder;

    private IAuthenticationContext authenticationContext;

    @BeforeEach
    public void setUp() throws Exception {
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/i18n/tpd-web/app-webapp");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(0);
        templateEngine = templateEngine(messageSource, templateResolver());
        mailService = mock(IMailService.class);
        i18nService = mock(I18nService.class);
        navBuilder = mock(INavBuilder.class, withSettings().lenient());
        authenticationContext = mock(IAuthenticationContext.class, withSettings().lenient());

        when(authenticationContext.getCurrentUser()).thenReturn(
            of(User.builder().username("user").displayName("Fred User").email("fred.user@company.com").build()));
        when(mailService.isHostConfigured()).thenReturn(true);
        when(navBuilder.buildBaseUrl()).thenReturn("http://localhost/context");

        emailNotifier = new DefaultEmailNotifier(mailService, messageSource, templateEngine, i18nService,
                authenticationContext, navBuilder);

    }

    @Test
    public void generateHtmlSendCreatedUserInEnglish() throws Exception {

        final ArgumentCaptor<MailMessage> captureActual = ArgumentCaptor.forClass(MailMessage.class);

        doNothing().when(mailService).sendNow(captureActual.capture());

        final String token = "1234567";

        emailNotifier.sendCreatedUser(
            UserRequest.builder()
                    .username("toto")
                    .displayName("toto Dupond")
                    .email("toto@company.com")
                    .langKey("en")
                    .build(),
            token);

        final String actual = captureActual.getValue().getText();
        // Files.write(actual.getBytes(),
        // new File(new File("target"), "sendCreatedUserInEnglish.html"));
        assertContains(actual,
            "Welcome to TPD Submission Tool", // title
            "Welcome to TPD Submission Tool",
            "Fred User has invited you to join TPD Submission Tool");
        assertContains(actual,
            "href=\"http://localhost/context/#/reset-password?token=1234567\"",
            "href=\"http://localhost/context/#/reset-password?token=1234567\"");
        assertContains(actual,
            "Full Name: toto Dupond",
            "Username: toto (this is your log-in name)",
            "Email: toto@company.com");
    }

    @Test
    public void generateHtmlSendCreatedUserInFrench() throws Exception {
        final ArgumentCaptor<MailMessage> captureActual = ArgumentCaptor.forClass(MailMessage.class);

        doNothing().when(mailService).sendNow(captureActual.capture());

        final String token = "1234567";

        emailNotifier.sendCreatedUser(
            UserRequest.builder()
                    .username("toto")
                    .displayName("toto Dupond")
                    .email("toto@company.com")
                    .langKey("fr")
                    .build(),
            token);

        final String actual = captureActual.getValue().getText();
        // Files.write(actual.getBytes(),
        // new File(new File("target"), "sendCreatedUserInFrench.html"));
        assertContains(actual,
            "Bienvenue à TPD Submission Tool", // title
            "Bienvenue à TPD Submission Tool",
            "Fred User vous invite à vous joindre à TPD Submission Tool.");
        assertContains(actual,
            "Nom Complet : toto Dupond",
            "Nom d&#39;utilisateur : toto (c&#39;est votre identifiant)",
            "Email : toto@company.com");
    }

    @Test
    public void generateHtmlResetPasswordInEnglish() throws Exception {
        final ArgumentCaptor<MailMessage> captureActual = ArgumentCaptor.forClass(MailMessage.class);

        doNothing().when(mailService).sendNow(captureActual.capture());

        final String token = "1234567";

        emailNotifier.sendPasswordReset(
            UserRequest.builder()
                    .username("toto")
                    .displayName("toto Dupond")
                    .email("toto@company.com")
                    .langKey("en")
                    .build(),
            token);

        final String actual = captureActual.getValue().getText();
        // Files.write(actual.getBytes(),
        // new File(new File("target"), "sendResetPasswordInEnglish.html"));
        assertContains(actual,
            "Password Reset", // title
            "Password reset request for toto",
            "TPD Submission Tool has received a password reset request for &#39;toto&#39; today at");
        assertContains(actual,
            "href=\"http://localhost/context/#/reset-password?token=1234567\"",
            "href=\"http://localhost/context/#/reset-password?token=1234567\"");
    }

    @Test
    public void generateHtmlResetPasswordInFrench() throws Exception {
        final ArgumentCaptor<MailMessage> captureActual = ArgumentCaptor.forClass(MailMessage.class);

        doNothing().when(mailService).sendNow(captureActual.capture());

        final String token = "1234567";

        emailNotifier.sendPasswordReset(
            UserRequest.builder()
                    .username("toto")
                    .displayName("toto Dupond")
                    .email("toto@company.com")
                    .langKey("fr")
                    .build(),
            token);

        final String actual = captureActual.getValue().getText();
        Files.write(actual.getBytes(), new File(new File("target"), "sendResetPasswordInFrench.html"));
        assertContains(actual,
            "Réinitialisation du mot de passe", // title
            "Demande de réinitialisation du mot de passe pour toto",
            "TPD Submission Toola reçu une demande de réinitialisation de mot de passe pour &#39;toto&#39; aujourd&#39;hui à");
        assertContains(actual,
            "href=\"http://localhost/context/#/reset-password?token=1234567\"",
            "href=\"http://localhost/context/#/reset-password?token=1234567\"");
    }

    @Test
    public void generateHtmlExpiredCertificated() throws Exception {
        final ArgumentCaptor<MailMessage> captureActual = ArgumentCaptor.forClass(MailMessage.class);

        doNothing().when(mailService).sendNow(captureActual.capture());

        final DateTime now = new DateTime();

        emailNotifier.sendExpiredCertificate("john.wayne@company.com",
            KeyStoreEntry.builder()
                    .algorithm("ALG")
                    .alias("alias-certificate")
                    .expiredDate(now)
                    .type(EntryType.KeyPair)
                    .build());

        final SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", ApplicationConstants.getDefaultLocale());
        final String actual = captureActual.getValue().getText();
        // Files.write(actual.getBytes(), new File(new File("target"), "ceritificateExpired.html"));
        assertContains(actual,
            "Expiration Certificate", // title
            "TPD Submission Tool has certificate <b>'alias-certificate'</b>",
            "that expired or will expired the <b>" + format.format(now.toDate()) + "</b>");
    }

    private ITemplateResolver templateResolver() {
        final ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("thymeleaf/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode("HTML");
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        emailTemplateResolver.setOrder(1);
        return emailTemplateResolver;
    }

    private SpringTemplateEngine templateEngine(final MessageSource messageSource,
        final ITemplateResolver templateResolver) {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setMessageSource(messageSource);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

}
