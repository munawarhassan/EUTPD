package com.pmi.tpd.core.avatar.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;

import javax.annotation.Nonnull;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.spi.IAvatarSource;

/**
 * An implementation of {@link IAvatarSource} which uses <a href="http://www.gravatar.com">Gravatar</a> to provide
 * avatars based on e-mail addresses.
 */
public class GravatarSource implements IAvatarSource {

    private final String defaultFallbackUrl;

    private final String httpUrlFormat;

    private final String httpsUrlFormat;

    public GravatarSource(final String httpUrlFormat, final String httpsUrlFormat, final String defaultFallbackUrl) {
        this.defaultFallbackUrl = defaultFallbackUrl;
        this.httpUrlFormat = httpUrlFormat;
        this.httpsUrlFormat = httpsUrlFormat;
    }

    @Override
    @Nonnull
    public AvatarSourceType getType() {
        return AvatarSourceType.Gravatar;
    }

    @Nonnull
    @Override
    public String getUrlForPerson(@Nonnull final IPerson person, @Nonnull final AvatarRequest request) {
        checkNotNull(person);

        final String emailAddress = person.getEmail();
        return getUrlForPerson(emailAddress, request);
    }

    /**
     * Encodes the provided {@code url} for use as a query parameter, allowing it to be embedded in another URL.
     *
     * @param url
     *            the url to encode
     * @return the encoded URL
     */
    private String encodeUrl(final String url) {
        return UriUtils.encodeQueryParam(url, "UTF-8");
    }

    /**
     * Uses either the {@link #httpUrlFormat standard URL format} or the {@link #httpsUrlFormat secure URL format},
     * depending on whether the {@link AvatarRequest#isSecure() request is secure}, and formats in:
     * <ol>
     * <li>Lowercased and MD5-encoded {@code emailAddress}</li>
     * <li>{@link AvatarRequest#getSize() Requested size}</li>
     * <li>{@link #defaultFallbackUrl Fallback URL}</li>
     * <li>Unmodified {@code emailAddress}</li>
     * </ol>
     *
     * @param emailAddress
     *            the e-mail address for the person whose avatar is being requested
     * @param request
     *            describes the avatar which is being requested
     * @return a URL which will return an avatar for the provided e-mail address
     */
    private String getUrlForPerson(String emailAddress, final AvatarRequest request) {
        checkNotNull(request);

        // this hash ensures Gravatar fallsback to the default url
        String hash = "00000000000000000000000000000000";
        if (StringUtils.isBlank(emailAddress)) {
            emailAddress = "";
        } else {
            hash = hash(emailAddress);
        }
        final String format = request.isSecure() ? httpsUrlFormat : httpUrlFormat;
        return String.format(format, hash, request.getSize().width(), encodeUrl(defaultFallbackUrl), emailAddress);
    }

    /**
     * Generates a hex-encoded hash of the provided {@code emailAddress}. The bytes for the e-mail address are decoded
     * using UTF-8 of else an UnsupportedEncodingException is thrown. This should not occur as UTF-8 is a mandatory
     * encoding for the Java platform.
     *
     * @param emailAddress
     *            the e-mail address to hash
     * @return a hex-encoded hash of the provided address
     */
    private String hash(final String emailAddress) {
        return DigestUtils.md5Hex(emailAddress.toLowerCase(Locale.US));
    }
}
