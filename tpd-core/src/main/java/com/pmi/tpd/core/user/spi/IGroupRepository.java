package com.pmi.tpd.core.user.spi;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.QGroupEntity;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * Spring JPA repository interface associated to {@link GroupEntity}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IGroupRepository extends IDslAccessor<GroupEntity, Long> {

    /**
     * {@inheritDoc}
     */
    @Override
    QGroupEntity entity();

    /**
     * Retrieves a group by their {@link GroupEntity#getName()}.
     *
     * @param name
     *            the name to match (can be {@code null}).
     * @return Returns the group or {@code null} if no group can be found with that name.
     */
    @Nullable
    GroupEntity findByName(@Nullable String name);

    /**
     * @param request
     *            defines the page of groups to retrieve (accept {@link IFilterable}) (can <b>not</b> be {@code null}).
     * @return Returns the requested page of groups, which may be empty but never {@code null}.
     */
    Page<GroupEntity> findGroups(Pageable request);

    /**
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups (can be {@code null})..
     * @param pageRequest
     *            defines the page of groups to retrieve (can <b>not</b> be {@code null}).
     * @return Returns the requested page of groups, which may be empty but never {@code null}.
     */
    @Nonnull
    Page<GroupEntity> findGroupsByName(@Nullable String groupName, @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of groups which the specified user is a member of, optionally filtering the returned results to
     * those containing the specified {@code groupName}. If the provided {@code groupName} is {@code null} or empty
     * groups will not be filtered. If no user exists with the specified {@code username}, no groups will be returned.
     *
     * @param username
     *            the <i>exact</i> name of the user to retrieve groups for (can <b>not</b> be {@code null}).
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups (can be {@code null}).
     * @param pageRequest
     *            defines the page of groups to retrieve (can <b>not</b> be {@code null}).
     * @return Returns the requested page of groups, which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<GroupEntity> findGroupsByUser(@Nonnull String username,
        @Nullable String groupName,
        @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of groups which the specified user is <i>not</i> a member of, optionally filtering the returned
     * results to those containing the specified {@code groupName}. If the provided {@code groupName} is {@code null} or
     * empty groups will not be filtered. If no user exists with the specified {@code username}, no groups will be
     * returned.
     *
     * @param username
     *            the <i>exact</i> name of the user to retrieve groups for (can <b>not</b> be {@code null}).
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups (can be {@code null}).
     * @param pageRequest
     *            defines the page of groups to retrieve.
     * @return Returns the requested page of groups, which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<GroupEntity> findGroupsWithoutUser(@Nonnull String username,
        @Nullable String groupName,
        @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of deleted groups by their {@link GroupEntity#getDeletedDate()} being earlier than a specified
     * date.
     *
     * @param date
     *            date to compare against (can <b>not</b> be {@code null}).
     * @param request
     *            the page request (can <b>not</b> be {@code null}).
     * @return Returns the requested page of groups, which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<GroupEntity> findByDeletedDateEarlierThan(@Nonnull Date date, @Nonnull Pageable request);

    /**
     * Gets indication whether the group name exists.
     *
     * @param groupName
     *            the group name to check existing (can <b>not</b> be {@code null} or empty).
     * @return Returns {@code true} if the group name exist, otherwise {@code false}.
     * @since 2.0
     */
    boolean exists(@Nonnull String groupName);

}
