package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IPerson;

/**
 * Provides URLs from which avatar images can be retrieved.
 */
public interface IAvatarService {

    /**
     * Retrieves a URL referencing an avatar for the provided {@link IPerson person}.
     * <p>
     * Implementations of this interface <i>shall not</i> return {@code null}. If no avatar is available for the
     * provided {@code person}, the URL of a default avatar is returned.
     *
     * @param person
     *            the person whose avatar is being requested
     * @param request
     *            a request describing the avatar being requested
     * @return a URL referencing an avatar for the provided {@code person}
     */
    @Nullable
    String getUrlForPerson(@Nonnull IPerson person, @Nonnull AvatarRequest request);
}
