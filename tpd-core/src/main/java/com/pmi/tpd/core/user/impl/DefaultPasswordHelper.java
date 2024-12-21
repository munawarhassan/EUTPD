package com.pmi.tpd.core.user.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.UserPreferenceKeys;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class DefaultPasswordHelper implements IPasswordResetHelper {

    /** */
    private final PasswordEncoder passwordEncoder;

    /** */
    private final ISecureTokenGenerator tokenGenerator;

    /** */
    private final IUserRepository userRepository;

    /** */
    private final IUserPreferencesManager userPreferencesManager;

    /** */
    private final long tokenValidityPeriod; // validity period of the password reset token

    /**
     * @param userRepository
     *                               user repository.
     * @param userPreferencesManager
     *                               a specific user preference manager.
     * @param tokenGenerator
     *                               a token generator.
     * @param passwordEncoder
     *                               a passsord encoder.
     * @param tokenValidityPeriod
     *                               validity period of the password reset token.
     */
    public DefaultPasswordHelper(@Nonnull final IUserRepository userRepository,
            final IUserPreferencesManager userPreferencesManager, @Nonnull final ISecureTokenGenerator tokenGenerator,
            @Nonnull final PasswordEncoder passwordEncoder, final int tokenValidityPeriod) {
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.userPreferencesManager = checkNotNull(userPreferencesManager, "userPreferencesManager");
        this.tokenGenerator = checkNotNull(tokenGenerator, "tokenGenerator");
        this.passwordEncoder = checkNotNull(passwordEncoder, "passwordEncoder");
        this.tokenValidityPeriod = TimeUnit.MINUTES.toMillis(tokenValidityPeriod);
    }

    @Override
    public Optional<IUser> findUserByResetToken(@Nonnull final String token) {
        final Optional<IUser> user = userPreferencesManager
                .findUserByProperty(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY, token);
        return user.filter(u -> getPasswordToken(u).isPresent());
    }

    @Override
    public String addResetPasswordToken(@Nonnull final String username) {
        checkNotNull(username, "username");
        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(username);
        final String token = generatePassword();
        pref.ifPresent(p -> {
            try {
                p.setString(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY, token);
                p.setLong(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY,
                    System.currentTimeMillis() + tokenValidityPeriod);
            } catch (final ApplicationException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        return token;
    }

    @Override
    public Optional<String> getPasswordToken(@Nonnull final IUser user) {
        // read the token
        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(checkNotNull(user, "user"));
        if (pref.isPresent()) {
            final Optional<String> token = pref.get().getString(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY);
            final long expiration = pref.get()
                    .getLong(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY)
                    .orElse(0L);

            // check the expiration
            return token.isPresent() && expiration >= System.currentTimeMillis() ? token : Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public void resetPassword(@Nonnull final IUser user, @Nonnull final String newPassord) {
        final UserEntity entity = Assert.notNull(this.userRepository.getById(checkNotNull(user, "user").getId()));
        // update the password
        userRepository.save(entity.copy().password(encodePassord(newPassord)).build());
        userPreferencesManager.getPreferences(entity.getName()).ifPresent(pref -> {
            try {
                // remove the reset token if present
                if (pref.exists(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY)) {
                    pref.remove(UserPreferenceKeys.PASSWORD_RESET_TOKEN_PROPERTY);
                }
                if (pref.exists(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY)) {
                    pref.remove(UserPreferenceKeys.PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY);
                }
            } catch (final ApplicationException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

    }

    @Override
    @Nonnull
    public String encodePassord(@Nonnull final String password) {
        return passwordEncoder.encode(checkNotNull(password, "password"));
    }

    @Override
    public String generatePassword() {
        return tokenGenerator.generateToken();
    }
}
