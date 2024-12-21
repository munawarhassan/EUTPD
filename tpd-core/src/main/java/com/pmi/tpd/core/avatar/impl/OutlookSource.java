package com.pmi.tpd.core.avatar.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.spi.IAvatarSource;

/**
 * An implementation of {@link IAvatarSource} which uses <a href="http://www.gravatar.com">Gravatar</a> to provide
 * avatars based on e-mail addresses.
 */
public class OutlookSource implements IAvatarSource {

    private final String httpsUrlFormat;

    public OutlookSource(final String httpsUrlFormat) {
        this.httpsUrlFormat = httpsUrlFormat;
    }

    @Override
    @Nonnull
    public AvatarSourceType getType() {
        return AvatarSourceType.Outlook;
    }

    @Nonnull
    @Override
    public String getUrlForPerson(@Nonnull final IPerson person, @Nonnull final AvatarRequest request) {
        checkNotNull(person);

        final String emailAddress = person.getEmail();
        return getUrlForPerson(emailAddress, request);
    }

    private String getUrlForPerson(final String emailAddress, final AvatarRequest request) {
        checkNotNull(request);
        String size;
        switch (request.getSize()) {
            case ExtraSmall:
                size = "HR48x48";
                break;
            case Small:
                size = "HR64x64";
                break;
            case Large:
                size = "HR120x120";
                break;
            case ExtraLarge:
                size = "HR240x240";
                break;
            case Medium:
            default:
                size = "HR96x96";
        }

        return String.format(this.httpsUrlFormat, emailAddress, size);
    }

}
