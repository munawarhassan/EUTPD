package com.pmi.tpd.core.security.provider.ldap;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.security.configuration.GroupLdapSchema;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.user.IGroup;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class LdapGroupRepository implements ILdapGroupRepository {

    /** */
    private final LdapTemplate ldapTemplate;

    /** */
    private final LdapAuthenticationProperties config;

    /**
     * @param ldapTemplate
     *            Executes core LDAP functionality .
     * @param config
     *            ldap configuration
     */
    public LdapGroupRepository(final LdapTemplate ldapTemplate, final LdapAuthenticationProperties config) {
        this.ldapTemplate = ldapTemplate;
        this.config = config;
    }

    private ContextMapper<LdapGroup> getContextMapper() {
        return new LdapGroup.GroupContextMapper(config.getGroupSchema());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<LdapGroup> findByName(@Nullable final String groupName, @Nonnull final Pageable pageRequest) {
        checkNotNull(pageRequest, "pageRequest");
        final GroupLdapSchema groupLdapSchema = config.getGroupSchema();
        AndFilter filter = buildAdditionFilter();
        if (!Strings.isNullOrEmpty(groupName)) {
            filter = filter.and(new LikeFilter(groupLdapSchema.getGroupNameAttribute(), "*" + groupName + "*"));
        }
        final LdapQuery query = buildQuery().countLimit(pageRequest.getPageSize()).filter(filter);

        final List<LdapGroup> groups = ldapTemplate.search(query, getContextMapper());
        Iterable<LdapGroup> page;
        if (pageRequest.getOffset() > 0) {
            page = Iterables.skip(groups, (int) pageRequest.getOffset());
        } else {
            page = groups;
        }
        return PageUtils.createPage(page, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IGroup findByName(@Nonnull final String groupName) {
        checkNotNull(groupName, "groupName");
        final GroupLdapSchema groupLdapSchema = config.getGroupSchema();
        final LdapQuery query = buildQuery()
                .filter(buildBaseFilter().and(new EqualsFilter(groupLdapSchema.getGroupNameAttribute(), groupName)));
        try {
            return ldapTemplate.searchForObject(query, getContextMapper());
        } catch (final EmptyResultDataAccessException | NameNotFoundException ex) {
            return null;
        }
    }

    private LdapQueryBuilder buildQuery() {
        final LdapQueryBuilder query = LdapQueryBuilder.query();
        if (!Strings.isNullOrEmpty(config.getLdapSchema().getBaseDn())) {
            query.base(config.getLdapSchema().getBaseDn());
        }
        return query;
    }

    private AndFilter buildBaseFilter() {
        final AndFilter andFilter = new AndFilter();
        final GroupLdapSchema groupLdapSchema = config.getGroupSchema();
        andFilter.and(new HardcodedFilter(groupLdapSchema.getGroupObjectFilter()));
        return andFilter;
    }

    private AndFilter buildAdditionFilter() {
        final GroupLdapSchema groupSchema = config.getGroupSchema();
        final AndFilter andFilter = buildBaseFilter();
        if (!Strings.isNullOrEmpty(config.getLdapSchema().getAdditionalGroupDn())) {
            return andFilter.and(new HardcodedFilter(config.getLdapSchema().getAdditionalGroupDn()));
        }
        if (!Strings.isNullOrEmpty(groupSchema.getGroupObjectFilter())) {
            return andFilter.and(new HardcodedFilter(groupSchema.getGroupObjectFilter()));
        }
        return andFilter;
    }

}
