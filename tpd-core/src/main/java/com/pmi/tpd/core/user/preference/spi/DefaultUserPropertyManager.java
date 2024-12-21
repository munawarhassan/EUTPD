package com.pmi.tpd.core.user.preference.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pmi.tpd.api.cache.GoogleCacheInstruments;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.user.spi.IUserRepository;

/**
 * <p>
 * DefaultUserPropertyManager class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Singleton
public class DefaultUserPropertyManager implements IUserPropertyManager, IStartable {

    /** cache of userkey -> propertyset for exclusive access. */
    protected final LoadingCache<String, IPropertyAccessor> psCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(30, MINUTES)
            .build(CacheLoader.from(this::createPropertySetFunction));

    /** */
    private static final String ENTITY_TYPE = "User";

    /** */
    private final IUserRepository userRepository;

    /** */
    private final IPropertySetFactory propertySetFactory;

    /**
     * <p>
     * Constructor for DefaultUserPropertyManager.
     * </p>
     *
     * @param propertySetFactory
     *            a {@link com.pmi.tpd.api.context.IPropertySetFactory} object.
     * @param userKeyStore
     *            a {@link com.pmi.tpd.core.user.spi.IUserRepository} object.
     */
    @Autowired
    public DefaultUserPropertyManager(@Nonnull final IEventPublisher eventPublisher,
            @Nonnull final IPropertySetFactory propertySetFactory, @Nonnull final IUserRepository userKeyStore) {
        this.userRepository = checkNotNull(userKeyStore, "userKeyStore");
        this.propertySetFactory = checkNotNull(propertySetFactory, "propertySetFactory");
    }

    /**
     * <p>
     * onClearCache.
     * </p>
     *
     * @param event
     *            a {@link com.pmi.tpd.api.lifecycle.ClearCacheEvent} object.
     */
    @EventListener
    public void onClearCache(final ClearCacheEvent event) {
        psCache.invalidateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        new GoogleCacheInstruments(getClass().getSimpleName()).addCache(psCache).install();
    }

    /** {@inheritDoc} */
    @Override
    public IPropertyAccessor getPropertySetAccessor(final IUser user) {
        Assert.notNull(user, "user");
        return getPropertySetForUserKey(user.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<IUser> findUserByProperty(final String key, final Object value, final int type) {
        final Optional<Long> optionalId = this.propertySetFactory.findByValue(ENTITY_TYPE, type, key, value);
        return optionalId.flatMap(id -> this.userRepository.findById(id));
    }

    private IPropertyAccessor getPropertySetForUserKey(final String userkey) {
        if (userkey == null) {
            return null;
        }
        return psCache.getUnchecked(userkey);
    }

    /** {@inheritDoc} */
    @Override
    public void clearCache(final IUser user) {
        psCache.invalidate(user.getName());
    }

    private IPropertyAccessor createPropertySetFunction(final String userkey) {
        final Long id = userRepository.getIdForUserKey(userkey);
        return propertySetFactory.buildCaching(ENTITY_TYPE, id, true);

    }
}
