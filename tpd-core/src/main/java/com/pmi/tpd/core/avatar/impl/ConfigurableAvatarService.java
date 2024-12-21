package com.pmi.tpd.core.avatar.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.annotation.Unsecured;
import com.pmi.tpd.web.core.request.spi.IRequestContext;
import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.AvatarStoreException;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.SimpleAvatarSupplier;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.AvatarUrlDecorator;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;
import com.pmi.tpd.core.avatar.spi.IAvatarSource;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.core.util.RequestLocalCache;

/**
 * A configurable implementation of {@link IInternalAvatarService} which relies on an {@link IAvatarSource}s to provide
 * avatar URLs.
 * <p>
 * <b>Implementation Note</b>: For the moment, we have 2 different {@link IAvatarSource}s, and we support a simple
 * toggle between the two. This is likely to change over time. For that reason, rather than storing a Boolean property
 * for the active source, we store a string. However, rather than building an incredibly complicated solution right now,
 * the two available implementations are autowired in and we toggle between them for enable/disable.
 */
@Named("avatarService")
@Transactional(propagation = Propagation.SUPPORTS)
public class ConfigurableAvatarService implements IInternalAvatarService {

    /**
     * A pattern for a data URI which contains Base64-encoded data.
     * <p>
     * The groups returned by this pattern are:
     * <ol>
     * <li>The declared content type for the encoded data</li>
     * <li>The Base64-encoded data</li>
     * </ol>
     * If the data URI is not Base64-encoded, it will not match this pattern. This pattern is intended for use reading
     * in avatars so it is expected that the URI contains image data, which must be Base64-encoded. Additionally, if a
     * character set is set on the data URI, it will not match this pattern. Image data URIs should omit the charset.
     * <p>
     * The Base64 specification allows for all alphanumeric characters, as well as + and /. That makes up the set of
     * characters allowed by the data grouping. The specification also allows for = to be used as padding. IF the data
     * URI includes any = characters, they will be <i>excluded</i> from the data group returned, since they are empty
     * padding and would otherwise be stripped out during decoding. = characters are only allowed at the <i>end</i> of
     * the data URI.
     * <p>
     * Additional considerations: The specification allows for whitespace in certain circumstances. This expression
     * <i>does not</i> allow for whitespace. As it has no value in Base64, all whitespace should be stripped before
     * performing matching. Lastly, the specification allows for an alternate "URL and filename safe" alphabet which
     * replaces + and / with - and _, respectively. This expression <i>does not</i> allow for that alphabet because the
     * decoding mechanism used does not support it. Better to catch it while matching instead of during decoding.
     */
    private static final Pattern PATTERN_DATA_URI = Pattern.compile("data:([^;]+);base64,([A-Za-z0-9+/]+)=*$");

    private static final Logger log = LoggerFactory.getLogger(ConfigurableAvatarService.class);

    private final RequestLocalCache<AvatarKey, String> avatarCache;

    private final AvatarUrlDecorator urlDecorator;

    private final List<IAvatarSource> sources;

    private AvatarSourceType defaultSource;

    private final I18nService i18nService;

    private final INavBuilder navBuilder;

    private final IApplicationProperties applicationProperties;

    private final IAvatarRepository repository;

    @Autowired
    public ConfigurableAvatarService(final I18nService i18nService, final INavBuilder navBuilder,
            final IApplicationProperties applicationProperties, final IAvatarRepository repository,
            final AvatarUrlDecorator urlDecorator, final List<IAvatarSource> sources,
            final IRequestContext requestContext) {
        this.sources = sources;
        this.defaultSource = AvatarSourceType.Gravatar;
        this.i18nService = i18nService;
        this.navBuilder = navBuilder;
        this.applicationProperties = applicationProperties;
        this.repository = repository;
        this.urlDecorator = urlDecorator;

        avatarCache = new RequestLocalCache<>(requestContext);
    }

    /**
     * Creates an {@link IAvatarSupplier} from the provided data URI.
     * <p>
     * The URI is required to match the {@link #PATTERN_DATA_URI data URI pattern}, which mandates that the image data
     * be Base64 encoded. The content type is parsed out and provided on the {@link IAvatarSupplier}, and the Base64
     * data will be decoded in the {@code InputStream} automatically.
     *
     * @param uri
     *            the data URI provided by the client
     * @return an {@link IAvatarSupplier} which can be used to read the image data from the provided {@code uri}
     */
    @Nonnull
    @Override
    @Unsecured("Creating a supplier from a data URI requires no specific permission")
    public IAvatarSupplier createSupplierFromDataUri(@Nonnull String uri) {
        checkNotNull(uri, "uri");

        // The data URI specification allows for whitespace in certain circumstances. To be safe, strip any whitespace
        // from the URI string, since it is not allowed by the regular expression.
        //
        // Additionally, - is replaced with + and _ is replaced with /. The Base64 specification offers two possible
        // "alphabets", the normal one (which includes + and /) and a "URL safe" one (which includes - and _). The
        // decoding logic in Base64InputStream does not check for the "URL safe" alphabet, so the swap ensures the
        // data URIs used consistently use the normal alphabet
        uri = uri.replaceAll("\\s", "").replace('-', '+').replace('_', '/');

        final Matcher matcher = PATTERN_DATA_URI.matcher(uri);
        if (!matcher.matches()) {
            throw new AvatarStoreException(i18nService.createKeyedMessage("app.service.avatar.invaliddatauri"));
        }

        final String contentType = matcher.group(1);
        final String data = matcher.group(2);
        final InputStream base64Data = new ByteArrayInputStream(getBytesUtf8(data));

        return new SimpleAvatarSupplier(contentType, new Base64InputStream(base64Data));
    }

    @Override
    // note: this _must_ be the same permission check as the saveForUser() method:
    // users can delete their own avatars, admins can delete all avatars except sysadmins',
    // sysadmins can delete all avatars
    @PreAuthorize("isCurrentUser(#user) or hasGlobalPermission('SYS_ADMIN') or "
            + "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
    public void deleteForUser(@Nonnull final IUser user) {
        repository.delete(AvatarType.USER, user.getId());
    }

    @Nonnull
    @Override
    @Unsecured("Avatars are trivial data which is available in any context, including non-authenticated contexts")
    public ICacheableAvatarSupplier getForUser(@Nonnull final IUser user, final int size) {
        checkNotNull(user, "user");

        return repository.load(AvatarType.USER, user.getId(), size);
    }

    @Nullable
    @Override
    @Unsecured("Avatars are trivial data which is available in any context, including non-authenticated contexts")
    public String getUrlForPerson(@Nonnull final IPerson person, @Nonnull final AvatarRequest request) {
        checkNotNull(person, "person");
        checkNotNull(request, "request");

        return avatarCache.get(AvatarKey.forPerson(person, request), () -> doGetUrlForPerson(person, request));
    }

    @Nonnull
    @Override
    @Unsecured("Avatars are trivial data which is available in any context, including non-authenticated contexts")
    public ICacheableAvatarSupplier getUserDefault(final int size) {
        return repository.loadDefault(AvatarType.USER, size);
    }

    @Override
    @Unsecured("Anyone is allowed to check whether avatars are enabled")
    public boolean isEnabled() {
        return !AvatarSourceType.Disable.name().equals(applicationProperties.getAvatarSource().orElse(null));
    }

    @Override
    @Unsecured("Anyone is allowed to get the default avatar source")
    public AvatarSourceType getDefaultSource() {
        return this.defaultSource;
    }

    @Override
    @Unsecured("Anyone is allowed to set the default avatar source")
    public void setDefaultSource(@Nonnull final AvatarSourceType defaultSource) {
        this.defaultSource = checkNotNull(defaultSource, "defaultSource");
    }

    @Override
    @Unsecured("Anyone is allowed to check whether a user's avatar is locally stored")
    public boolean isLocalForUser(@Nonnull final IUser user) {
        return repository.isStored(AvatarType.USER, checkNotNull(user, "user").getId());
    }

    /**
     * {@link IAvatarRepository#delete(AvatarType, Object) Clean up} avatars when users are deleted.
     *
     * @param event
     *            the event containing the {@link ApplicationUser user} which has been deleted
     * @since 2.4
     */
    @EventListener
    public void onUserCleanup(final UserCleanupEvent event) {
        cleanup(AvatarType.USER, event.getDeletedUser().getId());
        urlDecorator.invalidate(event.getDeletedUser());
    }

    @Override
    @PreAuthorize("isCurrentUser(#user) or " + // Users can always change their own avatar
            "hasGlobalPermission('SYS_ADMIN') or " + // SYS_ADMINs can change anyone's avatar
            // ADMINs can change anyone's avatar except SYS_ADMINs
            "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
    public void saveForUser(@Nonnull final IUser user, @Nonnull final IAvatarSupplier supplier) {
        checkNotNull(user, "user");

        repository.store(AvatarType.USER, user.getId(), supplier);
        urlDecorator.invalidate(user);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void setEnabled(final boolean enabled) {
        applicationProperties.setAvatarSource(enabled ? getDefaultSource().name() : AvatarSourceType.Disable.name());
    }

    private void cleanup(final AvatarType type, final Long id) {
        try {
            repository.delete(type, id);
        } catch (final AvatarStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not cleanup avatars for " + type + " " + id, e);
            } else {
                log.warn("Could not cleanup avatars for " + type + " " + id + ": " + e.getMessage());
            }
        }
    }

    @Nullable
    private String doGetUrlForPerson(@Nonnull final IPerson person, @Nonnull final AvatarRequest request) {
        // If the provided Person is a IUser, check for the presence of a local avatar. If they have one,
        // regardless of whether is enabled, return a local URL
        if (person instanceof IUser) {
            final IUser user = (IUser) person;
            if (repository.isStored(AvatarType.USER, user.getId())) {
                final INavBuilder.Builder<?> builder = navBuilder.user(user).avatar(request.getSize().width());
                urlDecorator.decorate(builder, user);
                return request.isUseConfigured() ? builder.buildConfigured() : builder.buildRelative();
            }
        }
        if (this.isEnabled() && request.getSource() != null) {
            final IAvatarSource source = getAvatarSource(request.getSource());
            if (source != null) {
                return source.getUrlForPerson(person, request);
            }
        }
        return null;
    }

    @Nullable
    @CheckForNull
    private IAvatarSource getAvatarSource(@Nonnull final AvatarSourceType source) {
        checkNotNull(source, "source");
        if (this.isEnabled()) {
            return Iterables.tryFind(this.sources, (src) -> source.equals(src.getType()))
                    .or(Iterables.tryFind(this.sources, (src) -> defaultSource.equals(src.getType())))
                    .orNull();
        }
        return null;
    }

    private static class AvatarKey {

        private final Object owner;

        private final AvatarRequest request;

        private AvatarKey(final Object owner, final AvatarRequest request) {
            this.owner = checkNotNull(owner, "owner");
            this.request = checkNotNull(request, "request");
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final AvatarKey that = (AvatarKey) other;

            return owner.equals(that.owner) && request.equals(that.request);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, request);
        }

        static AvatarKey forPerson(final IPerson person, final AvatarRequest request) {
            return new AvatarKey(personId(person), request);
        }

        @Nonnull
        private static Object personId(final IPerson person) {
            // unique "ID" for a person to store avatars against; for registered users this is simply the ID
            // for other persons e-mail is sufficient as it's the only piece of data used to generate avatar URLs
            if (person instanceof IUser) {
                return ((IUser) person).getId();
            }
            return StringUtils.defaultString(person.getEmail());
        }
    }

}
