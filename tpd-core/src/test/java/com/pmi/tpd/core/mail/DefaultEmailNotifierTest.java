package com.pmi.tpd.core.mail;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.springframework.context.MessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.exceptions.TemplateInputException;

import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultEmailNotifierTest extends MockitoTestCase {

    @Mock(strictness = Strictness.LENIENT)
    private IAuthenticationContext authenticationContext;

    @Mock(strictness = Strictness.LENIENT)
    private I18nService i18nService;

    @Mock
    MessageSource messageSource;

    @Mock
    private IMailService mailService;

    @Mock(strictness = Strictness.LENIENT)
    private INavBuilder navBuilder;

    @InjectMocks
    private DefaultEmailNotifier notifier;

    @Mock
    private ITemplateEngine templateRenderer;

    @Mock(strictness = Strictness.LENIENT)
    private IUser user;

    @BeforeEach
    public void setUp() throws Exception {
        when(authenticationContext.getCurrentUser()).thenReturn(Optional.of(user));
        when(i18nService.getMessage(anyString(), any())).thenAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            return args[0] + ", " + args[1];
        });
        when(i18nService.createKeyedMessage(anyString(), any())).thenAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final String code = (String) args[0];
            return new KeyedMessage(code, code, code);
        });
        when(i18nService.createKeyedMessage(anyString())).thenAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final String code = (String) args[0];
            return new KeyedMessage(code, code, code);
        });
        when(mailService.isHostConfigured()).thenReturn(true);
        when(user.getDisplayName()).thenReturn("A. D. Ministrator");
        when(navBuilder.buildBaseUrl()).thenReturn("http://localhost:8080/");
    }

    @Test
    public void testValidate() {
        assertThrows(NoMailHostConfigurationException.class, () -> {
            when(mailService.isHostConfigured()).thenReturn(false);
            notifier.validateCanSendEmails();
        });
    }

    @Test
    public void testNoMailHost() {
        assertThrows(NoMailHostConfigurationException.class, () -> {
            when(mailService.isHostConfigured()).thenReturn(false);
            notifier.sendCreatedUser(UserRequest.builder().username("test").build(), "abc");
        });
    }

    @Test
    public void testNoMailHostWhenPasswordReset() {
        assertThrows(NoMailHostConfigurationException.class, () -> {
            when(mailService.isHostConfigured()).thenReturn(false);
            notifier.sendPasswordReset(UserRequest.builder().username("test").build(), "abc");
        });
    }

    @Test
    public void testRenderingException() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            final UserRequest user = UserRequest.builder()
                    .username("fred")
                    .displayName("fred Smith")
                    .email("fred@example.com")
                    .build();
            doThrow(new TemplateInputException("hey")).when(templateRenderer).process(anyString(), any());
            notifier.sendCreatedUser(user, "TOKEN");
        });
    }

    @Test
    public void testCreatedUser() throws Exception {
        final UserRequest user = UserRequest.builder()
                .username("fred")
                .displayName("Fred Smith")
                .email("fred@example.com")
                .build();
        whenRendered(templateRenderer, DefaultEmailNotifier.TEMPLATE_EMAIL_CREATE_USER);

        notifier.sendCreatedUser(user, "TOKEN");

        final ArgumentCaptor<MailMessage> messageArgument = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailService).sendNow(messageArgument.capture());
        assertEquals("app.notification.mail.welcome.subject, TPD Submission Tool",
            messageArgument.getValue().getSubject());
        assertEquals("fred@example.com", messageArgument.getValue().getTo().iterator().next());
        assertEquals("BODY", messageArgument.getValue().getText());
    }

    @Test
    public void testPasswordReset() throws Exception {
        final UserRequest user = UserRequest.builder()
                .username("fred")
                .displayName("Fred Smith")
                .email("fred@example.com")
                .build();
        whenRendered(templateRenderer, DefaultEmailNotifier.TEMPLATE_EMAIL_RESET_USER_PASSWORD);

        notifier.sendPasswordReset(user, "TOKEN");

        final ArgumentCaptor<MailMessage> messageArgument = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailService).sendNow(messageArgument.capture());
        assertEquals("app.notification.mail.reset-password.subject, Fred Smith",
            messageArgument.getValue().getSubject());
        assertEquals("fred@example.com", messageArgument.getValue().getTo().iterator().next());
        assertEquals("BODY", messageArgument.getValue().getText());
    }

    private void whenRendered(final ITemplateEngine renderer, final String template) {
        when(renderer.process(eq(template), any())).thenReturn("BODY");
    }

}
