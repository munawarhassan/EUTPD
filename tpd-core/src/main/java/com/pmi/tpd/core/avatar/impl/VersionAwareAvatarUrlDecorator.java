package com.pmi.tpd.core.avatar.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.AvatarUrlDecorator;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;

@Service
public class VersionAwareAvatarUrlDecorator implements AvatarUrlDecorator {

    private final Cache<Long, Long> userSerialCache;

    private static final String CACHE_NAME = VersionAwareAvatarUrlDecorator.class.getName();

    @Inject
    public VersionAwareAvatarUrlDecorator(final IAvatarRepository repository, final CacheFactory cacheFactory) {
        // max size = (num projects + num avatars) * (size(Integer) + size(Long) + delta),
        // where delta = any cache entry overhead. This is assumed small enough to not bound this cache.
        final CacheSettings settings = new CacheSettingsBuilder().replicateAsynchronously()
                .replicateViaCopy()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build();
        userSerialCache = createSerialCache(repository, AvatarType.USER, cacheFactory, settings);
    }

    @Override
    public void decorate(@Nonnull final INavBuilder.Builder<?> builder, @Nonnull final IUser user) {
        decorateUrl(builder, userSerialCache, user.getId());
    }

    /**
     * Invalidate the user avatar serial cache when a user updates their avatar.
     *
     * @param user
     *            the user which has updated their avatar
     */
    @Override
    public void invalidate(@Nonnull final IUser user) {
        userSerialCache.remove(user.getId());
    }

    private void decorateUrl(final INavBuilder.Builder<?> builder, final Cache<Long, Long> cache, final Long id) {
        final Long serial = cache.get(id);
        if (serial != null) {
            builder.withParam("v", serial.toString());
        }
    }

    private Cache<Long, Long> createSerialCache(final IAvatarRepository repository,
        final AvatarType type,
        final CacheFactory cacheFactory,
        final CacheSettings settings) {
        return cacheFactory.getCache(CACHE_NAME + ".serial." + type, new CacheLoader<Long, Long>() {

            @Nonnull
            @Override
            public Long load(@Nonnull final Long key) {
                return repository.getVersionId(type, key);
            }
        }, settings);
    }
}
