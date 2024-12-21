package com.pmi.tpd.core.user.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.longThat;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.UserPreferenceKeys;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class PasswordResetHelperTest extends MockitoTestCase {

    private static final int DEFAULT_TOKEN_VALIDITY_PERIOD = 1;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserPreferencesManager userPreferencesManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ISecureTokenGenerator tokenGenerator;

    private IPasswordResetHelper helper;

    @BeforeEach
    public void setUp() throws Exception {
        helper = new DefaultPasswordHelper(userRepository, userPreferencesManager, tokenGenerator, passwordEncoder,
                DEFAULT_TOKEN_VALIDITY_PERIOD);
    }

    @Test
    public void testResetPassword() throws Exception {
        final UserEntity entity = UserEntity.builder()
                .id(1L)
                .username("name")
                .displayName("firstName lastName")
                .email("test@example.com")
                .build();
        final IUser user = User.builder(entity).build();
        final String newPassword = "newpassword";
        final IPreferences preference = mock(IPreferences.class);

        when(userRepository.getById(eq(1L))).thenReturn(entity);
        when(userPreferencesManager.getPreferences(eq(user.getName()))).thenReturn(Optional.of(preference));
        when(preference.exists(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY)).thenReturn(true);
        when(preference.exists(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY)).thenReturn(true);
        when(passwordEncoder.encode(eq(newPassword))).thenReturn(newPassword);

        helper.resetPassword(user, newPassword);

        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        final UserEntity actual = captor.getValue();
        assertEquals(newPassword, actual.getPassword());

        verify(userPreferencesManager).getPreferences(eq(user.getName()));
        verify(preference).remove(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY));
        verify(preference).remove(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY));

    }

    @Test
    public void testAddResetPasswordToken() throws Exception {
        final String userName = "name";
        final String token = "token";
        final IPreferences preference = mock(IPreferences.class);

        when(userPreferencesManager.getPreferences(eq(userName))).thenReturn(Optional.of(preference));
        when(tokenGenerator.generateToken()).thenReturn(token);

        final String actualToken = helper.addResetPasswordToken(userName);

        assertEquals(token, actualToken);
        verify(preference).setString(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY), eq(token));
        verify(preference).setLong(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY), tokenExpiration());

        verify(tokenGenerator).generateToken();
    }

    @Test
    public void testFindPasswordResetRequest() throws Exception {
        final String token = "token-in-db";
        final IUser user = User.builder().id(1L).build();
        final IPreferences preference = mock(IPreferences.class);

        when(userPreferencesManager.findUserByProperty(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY), eq(token)))
                .thenReturn(of(user));
        when(userPreferencesManager.getPreferences(eq(user))).thenReturn(Optional.of(preference));
        when(preference.getString(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY))).thenReturn(of(token));
        when(preference.getLong(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY)))
                .thenReturn(of(System.currentTimeMillis() + 3600000));

        assertEquals(user, helper.findUserByResetToken("token-in-db").get());
    }

    @Test
    public void testFindPasswordResetRequestWithExpiredToken() throws Exception {
        final String token = "token-in-db";
        final IUser user = User.builder().id(1L).build();
        final IPreferences preference = mock(IPreferences.class);

        when(userPreferencesManager.findUserByProperty(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY), eq(token)))
                .thenReturn(of(user));
        // token has expired
        when(userPreferencesManager.getPreferences(eq(user))).thenReturn(Optional.of(preference));
        when(preference.getString(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY))).thenReturn(of(token));
        when(preference.getLong(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY))).thenReturn(of(123L));

        assertTrue(helper.findUserByResetToken("token-in-db").isEmpty());
    }

    @Test
    public void testFindPasswordResetRequestForUserWithoutToken() throws Exception {
        final String token = "token-in-db";
        final IUser user = User.builder().id(1L).build();
        final IPreferences preference = mock(IPreferences.class);

        when(userPreferencesManager.findUserByProperty(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY), eq(token)))
                .thenReturn(of(user));

        when(userPreferencesManager.getPreferences(eq(user))).thenReturn(Optional.of(preference));
        when(preference.getLong(eq(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY))).thenReturn(empty());

        assertTrue(helper.findUserByResetToken("token-in-db").isEmpty());
    }

    private static Long tokenExpiration() {
        return longThat(argument -> {
            final long expiration = argument;
            final long now = System.currentTimeMillis();
            return expiration > now
                    && expiration - now < 1.5 * TimeUnit.MINUTES.toMillis(DEFAULT_TOKEN_VALIDITY_PERIOD);
        });
    }
}
