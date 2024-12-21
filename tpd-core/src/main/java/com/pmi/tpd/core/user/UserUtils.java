package com.pmi.tpd.core.user;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.Principal;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.IAuthenticationContext;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public final class UserUtils {

    /**
     * Used to check whether a {@link IUser} {@link IUser#isActivated() is active}.
     *
     * @see IUser#isActivated()
     * @since 2.0
     */
    public static final Predicate<? super IUser> IS_ACTIVE = user -> user.isActivated();

    /**
     * Converts a {@link IUser} to its {@link IUser#getId() id}.
     *
     * @see IUser#getId()
     * @since 2.0
     */
    public static final Function<? super IUser, Long> TO_ID = user -> user.getId();

    /**
     * Converts a {@code Principal}, {@link IUser}, to its username.
     *
     * @since 2.0
     */
    public static final Function<? super Principal, String> TO_USERNAME = user -> user.getName();

    /** */
    private static final String NAME_MATCHING_REGEX = "(\\b|^|[^\\p{L}\\p{N}])";

    /** */
    private static final Function<? super IUser, String> TO_DISPLAY_NAME = user -> user.getDisplayName();

    /**
     * Prevents instantiation.
     *
     * @throws UnsupportedOperationException
     *             Thrown if this class is instantiated via Reflection, or some other mechanism which ignores
     *             visibility.
     */
    private UserUtils() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * @param authentication
     * @return
     */
    @Nonnull
    public static Optional<String> getCurrentUserName(@Nonnull final IAuthenticationContext authentication) {
        return authentication.getCurrentUser().map(TO_DISPLAY_NAME);
    }

    /**
     * @param filter
     *            the filter to use
     * @return a {@code Pattern} which can be used to match {@link IUser#getName() usernames} or group names
     *         consistently
     * @since 2.0
     */
    @Nonnull
    public static Pattern createNameMatchingPattern(@Nonnull final String filter) {
        checkNotNull(filter, "filter");
        return Pattern.compile(NAME_MATCHING_REGEX + Pattern.quote(filter), Pattern.CASE_INSENSITIVE);
    }
}
