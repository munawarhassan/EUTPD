package com.pmi.tpd.core.user;

import com.pmi.tpd.api.ApplicationConstants;

/**
 * Keys used when storing user preferences.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class UserPreferenceKeys {

    /** Constant <code>USER_I18_LOCALE="Config.PropertyKeys.I18N_DEFAULT_LOCALE"</code>. */
    public static final String USER_I18_LOCALE = ApplicationConstants.PropertyKeys.I18N_DEFAULT_LOCALE;

    /** */
    public static final String PASSWORD_RESET_TOKEN_PROPERTY = "user.password.token";

    /** */
    public static final String PASSWORD_RESET_TOKEN_EXPIRATION_PROPERTY = "user.password.expiration";

    /** */
    public static final String AVATAR_SOURCE = "user.avatar.source";

    /** */
    public static final String USER_LAST_UPDATE = "user.lastupdate";

    private UserPreferenceKeys() {
    }

}
