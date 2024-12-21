package com.pmi.tpd.core.security.provider.ldap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public interface ILdapUserRepository {

    /**
     * Utility method to perform a simple LDAP 'bind' authentication. Search for the LDAP entry to authenticate using
     * the supplied LdapQuery; use the DN of the found entry together with the password as input to
     * {@link ContextSource#getContext(String, String)}, thus authenticating the entry.
     * <p>
     * <b>Note:</b> This method differs from the older authenticate methods in that encountered exceptions are thrown
     * rather than supplied to a callback for handling.
     * </p>
     *
     * @param query
     *            the LdapQuery specifying the details of the search.
     * @param password
     *            the password to use for authentication.
     * @return the user result.
     * @throws IncorrectResultSizeDataAccessException
     *             if more than one users were found
     * @throws org.springframework.dao.EmptyResultDataAccessException
     *             if only one user was found
     * @throws NamingException
     *             if something went wrong in authentication.
     */
    @Nonnull
    LdapUser authenticate(@Nonnull final String username, @Nonnull final String password);

    /**
     * @param username
     *            0 or more characters to apply as a filter on returned user (can <b>not</b> be {@code null}).
     * @param pageRequest
     *            defines the page of users to retrieve (can <b>not</b> be {@code null}).
     * @return Returns the requested page of users, potentially filtered, which may be empty but never {@code null}.
     */
    @Nonnull
    Page<LdapUser> findByName(@Nullable String username, @Nonnull Pageable pageRequest);

    /**
     * Find the {@code LdapUser LDAP user} associated to {@code username}.
     *
     * @param username
     *            the user name (can <b>not</b> be {@code null}).
     * @return Returns the user or {@code null} if no user can be found with that name.
     */
    @Nullable
    LdapUser findByName(@Nonnull String username);

}