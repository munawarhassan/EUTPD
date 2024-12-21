package com.pmi.tpd.core.user;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Optional;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.IUserProfile;
import com.pmi.tpd.api.user.UserKey;
import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.IAvatarService;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.util.UrlUtils;

public class DefaultUserProfile implements IUserProfile {

    private final IUser user;

    private final INavBuilder navBuilder;

    private final IAvatarService avatarService;

    private final AvatarSourceType avatarSource;

    public DefaultUserProfile(final IUser user, final IUserPreferencesManager userPreferencesManager,
            final INavBuilder navBuilder, final IInternalAvatarService avatarService) {
        this.user = checkNotNull(user);
        this.navBuilder = checkNotNull(navBuilder);
        this.avatarService = checkNotNull(avatarService);
        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(user);
        if (pref.isPresent()) {
            avatarSource = pref.get()
                    .getString(UserPreferenceKeys.AVATAR_SOURCE)
                    .map(v -> AvatarSourceType.valueOf(v))
                    .orElse(avatarService.getDefaultSource());
        } else {
            this.avatarSource = avatarService.getDefaultSource();
        }
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getDisplayName() {
        return user.getDisplayName();
    }

    @Override
    public UserKey getUserKey() {
        return UserKey.fromLong(user.getId());
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public URI getProfilePictureUri(final int width, final int height) {
        // our avatars are always square, ignore the "preferred" height
        return getUrlForPerson(AvatarSize.valueOf(width));
    }

    @Override
    public URI getProfilePictureUri() {
        return getUrlForPerson(AvatarSize.Medium);
    }

    /**
     * @return the {@link ApplicationUser} backing the {@link IUserProfile}.
     */
    public IUser getUser() {
        return user;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DefaultUserProfile that = (DefaultUserProfile) o;

        return getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    /**
     * Protected access for testing.
     *
     * @param width
     *              the width (and height, as it's square) of the avatar
     * @return an absolute URI to the avatar for this user
     */
    protected URI getUrlForPerson(final AvatarSize width) {
        final String avatarUrl = avatarService.getUrlForPerson(user,
            AvatarRequest.from(navBuilder, width, avatarSource));
        return UrlUtils.uncheckedCreateURI(avatarUrl);
    }
}
