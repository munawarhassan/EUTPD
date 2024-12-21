package com.pmi.tpd.core.user.preference;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.user.preference.spi.IUserPropertyManager;
import com.pmi.tpd.core.user.spi.IUserRepository;

/**
 * <p>
 * DefaultUserPreferencesManager class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class DefaultUserPreferencesManager implements IUserPreferencesManager {

    /** */
    private final LoadingCache<String, UserPreferences> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(1000)
            .build(new CacheLoader<String, UserPreferences>() {

                @Override
                public UserPreferences load(final String userkey) {
                    return new UserPreferences(userPropertyManager, defaultUserPreference,
                            userRepository.findByName(userkey));
                }
            });

    /** */
    private final IUserRepository userRepository;

    /** */
    private final IUserPropertyManager userPropertyManager;

    private final IPreferences defaultUserPreference;

    /**
     * <p>
     * Constructor for DefaultUserPreferencesManager.
     * </p>
     *
     * @param userRepository
     *                            a {@link com.pmi.tpd.core.user.spi.IUserRepository} object.
     * @param userPropertyManager
     *                            a {@link com.pmi.tpd.core.user.preference.spi.IUserPropertyManager} object.
     */
    @Inject
    public DefaultUserPreferencesManager(@Nonnull final IEventPublisher publisher,
            @Nonnull final IUserRepository userRepository, @Nonnull final IUserPropertyManager userPropertyManager,
            final IPropertySetFactory factory) {
        Assert.notNull(publisher);
        this.defaultUserPreference = new DefaultPreferences(factory.buildMemory());
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.userPropertyManager = checkNotNull(userPropertyManager, "userPropertyManager");
    }

    /**
     * <p>
     * onClearCache.
     * </p>
     *
     * @param event
     *              a {@link com.pmi.tpd.api.lifecycle.ClearCacheEvent} object.
     */
    @EventListener
    public void onClearCache(final ClearCacheEvent event) {
        clearCache();
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Optional<IPreferences> getPreferences(final IUser user) {
        if (user == null) {
            return Optional.of(new UserPreferences((IUser) null));
        }

        try {
            return Optional.ofNullable(cache.get(user.getName()));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<IUser> findUserByProperty(final String property, final String value) {
        return this.userPropertyManager.findUserByProperty(property, value, PropertySet.STRING);
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Optional<IPreferences> getPreferences(@Nullable final String key) {
        if (key == null) {
            return Optional.of(new UserPreferences((IUser) null));
        }

        try {
            return Optional.ofNullable(cache.get(key));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void clearCache(final IUser user) {
        if (user == null) {
            return;
        }

        cache.invalidate(user.getName());
        // UserPreferences is actually backed by UserPropertyManager which does most the caching - we ought to clear it
        // too.
        userPropertyManager.clearCache(user);
    }
}
