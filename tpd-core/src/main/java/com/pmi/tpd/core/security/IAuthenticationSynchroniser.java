package com.pmi.tpd.core.security;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.security.provider.IAuthenticationProvider;

/**
 * Allow to synchronise external user directory with internal directory.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IAuthenticationSynchroniser {

    /**
     * Synchronise user with associated authorities group.
     *
     * @param username
     *            the user to synchronize.
     * @param authorities
     *            list of authorities group name.
     * @param provider
     *            provider associated to user.
     * @return Returns the user synchronised.
     */
    @Nullable
    IUser synchronise(@Nonnull String username,
        @Nonnull Set<String> authorities,
        @Nonnull IAuthenticationProvider provider);

}
