package com.pmi.tpd.core.security.provider.ldap;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.elasticsearch.common.Strings;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.authentication.BadCredentialsException;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.UserLdapSchema;
import com.pmi.tpd.core.security.provider.ldap.LdapUser.UserContextMapper;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class LdapUserRepository implements ILdapUserRepository {

    /** */
    private final LdapTemplate ldapTemplate;

    /** */
    private final LdapAuthenticationProperties config;

    /** */
    private final UserDirectory userDirectory;

    /**
     * @param ldapTemplate
     *            Executes core LDAP functionality
     * @param config
     *            ldap configuration.
     * @param userDirectory
     *            user directory.
     */
    public LdapUserRepository(final LdapTemplate ldapTemplate, final LdapAuthenticationProperties config,
            final UserDirectory userDirectory) {
        this.ldapTemplate = ldapTemplate;
        this.config = config;
        this.userDirectory = userDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<LdapUser> findByName(@Nullable final String username, @Nonnull final Pageable pageRequest) {
        checkNotNull(pageRequest, "pageRequest");
        final UserLdapSchema userLdapSchema = config.getUserSchema();
        AndFilter filter = buildAdditionFilter();
        if (!Strings.isNullOrEmpty(username)) {
            filter = filter.and(new LikeFilter(userLdapSchema.getUserNameAttribute(), username + "*"));
        }

        final LdapQuery query = buildQuery().countLimit(pageRequest.getPageSize()).filter(filter);
        final List<LdapUser> users = ldapTemplate.search(query, getContextMapper());
        Iterable<LdapUser> page;
        if (pageRequest.getOffset() > 0) {
            page = Iterables.skip(users, (int) pageRequest.getOffset());
        } else {
            page = users;
        }
        return PageUtils.createPage(page, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public LdapUser findByName(final String username) {
        checkNotNull(username, "username");
        final UserLdapSchema userLdapSchema = config.getUserSchema();
        final LdapQuery query = buildQuery().filter(new EqualsFilter(userLdapSchema.getUserNameAttribute(), username));
        try {
            return ldapTemplate.searchForObject(query, getContextMapper());
        } catch (final EmptyResultDataAccessException | NameNotFoundException e) {
            return null;
        }
    }

    @Override
    @Nonnull
    public LdapUser authenticate(@Nonnull final String username, @Nonnull final String password) {

        final UserLdapSchema userLdapSchema = config.getUserSchema();
        final LdapQuery query = buildQuery().filter(new EqualsFilter(userLdapSchema.getUserNameAttribute(), username));
        try {
            return this.ldapTemplate.authenticate(query, password, getContextMapper());
        } catch (final EmptyResultDataAccessException | NameNotFoundException e) {
            throw new BadCredentialsException("no user");
        }
    }

    private UserContextMapper getContextMapper() {
        return new UserContextMapper(config, userDirectory);
    }

    private LdapQueryBuilder buildQuery() {
        final LdapQueryBuilder query = LdapQueryBuilder.query();
        if (!Strings.isNullOrEmpty(config.getLdapSchema().getBaseDn())) {
            query.base(config.getLdapSchema().getBaseDn());
        }
        return query;
    }

    private AndFilter buildAdditionFilter() {
        final AndFilter andFilter = new AndFilter();
        final UserLdapSchema userSchema = config.getUserSchema();

        if (!Strings.isNullOrEmpty(userSchema.getUserObjectFilter())) {
            return andFilter.and(new HardcodedFilter(userSchema.getUserObjectFilter()));
        }
        return andFilter;
    }

}
