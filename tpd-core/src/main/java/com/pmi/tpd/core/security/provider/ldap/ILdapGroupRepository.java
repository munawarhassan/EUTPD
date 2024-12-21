package com.pmi.tpd.core.security.provider.ldap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.user.IGroup;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public interface ILdapGroupRepository {

    /**
     * @param name
     *            0 or more characters to apply as a filter on returned group (can <b>not</b> be {@code null}).
     * @param pageRequest
     *            defines the page of groups to retrieve (can <b>not</b> be {@code null}).
     * @return Returns the requested page of groups, potentially filtered, which may be empty but never {@code null}.
     */
    @Nonnull
    Page<LdapGroup> findByName(@Nonnull String name, @Nonnull Pageable pageRequest);

    /**
     * Find the {@code LdapUser LDAP user} associated to {@code groupName}.
     *
     * @param groupName
     *            the group name (can <b>not</b> be {@code null}).
     * @return Returns the group or {@code null} if no group can be found with that name.
     */
    @Nullable
    IGroup findByName(String groupName);

}