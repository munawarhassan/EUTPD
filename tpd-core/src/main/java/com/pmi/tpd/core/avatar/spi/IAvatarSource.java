package com.pmi.tpd.core.avatar.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.IAvatarService;

/**
 * Describes a component from which avatar URLs can be retrieved.
 * <p>
 * There are a couple of motivating factors behind this interface's existence:
 * <ul>
 * <li>Simplify autowiring: Because sources do not implement {@code AvatarService}, there is still only a single bean of
 * that type in the {@code ApplicationContext}, allowing autowiring by type without qualifiers.</li>
 * <li>Facilitate more implementations: At some point, this interface may be exposed as a plugin point, for example,
 * allowing custom avatar sources to be implemented. Additionally, if JIRA or Crowd add support for functioning as an
 * avatar server, implementations based on them may be created.</li>
 * </ul>
 * If a point comes where we have more than an on/off pairing of sources, additional methods may be added to this
 * interface, requiring different sources to define a name, key and description. That would facilitate adding a combo
 * box to the UI (rather than a simple checkbox) where an administrator could choose a specific avatar implementation
 * from the various ones the system supports.
 */
public interface IAvatarSource {

    @Nonnull
    AvatarSourceType getType();

    /**
     * Retrieves a URL referencing an avatar for the provided {@link IPerson person}.
     * <p>
     * Implementations of this interface <i>shall not</i> return {@code null}. If no avatar is available for the
     * provided {@code person}, the URL of a default avatar is returned.
     * <p>
     * The returned URL will always be absolute.
     *
     * @param person
     *            the person whose avatar is being requested
     * @param request
     *            a request describing the avatar being requested
     * @return an absolute URL referencing an avatar for the provided {@code person}
     * @see IAvatarService#getUrlForPerson(IPerson, AvatarRequest)
     */
    @Nonnull
    String getUrlForPerson(@Nonnull IPerson person, @Nonnull AvatarRequest request);
}
