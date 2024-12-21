package com.pmi.tpd.core.user.preference.spi;

import java.util.Optional;

import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.user.IUser;

/**
 * <p>
 * IUserPropertyManager interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUserPropertyManager {

    /**
     * Get the property set associated with a user.
     *
     * @param user
     *            the property set is associated with.
     * @return Property set.
     */
    IPropertyAccessor getPropertySetAccessor(IUser user);

    /**
     * <p>
     * clearCache.
     * </p>
     *
     * @param user
     *            a {@link com.pmi.tpd.api.user.IUser} object.
     */
    void clearCache(IUser user);

    /**
     * @param key
     * @param value
     * @param type
     * @return
     */
    Optional<IUser> findUserByProperty(String key, Object value, int type);
}
