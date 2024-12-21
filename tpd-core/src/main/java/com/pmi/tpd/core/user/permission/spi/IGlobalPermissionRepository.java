package com.pmi.tpd.core.user.permission.spi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.QGlobalPermissionEntity;
import com.pmi.tpd.security.permission.IPermittedGroup;
import com.pmi.tpd.security.permission.IPermittedUser;

/**
 * A repository for managing {@link InternalGlobalPermission global permissions}.
 *
 * @since 2.0
 */
public interface IGlobalPermissionRepository extends IGrantedPermissionRepository<GlobalPermissionEntity> {

    /**
    *
    */
    @Override
    @Nonnull
    QGlobalPermissionEntity entity();

    /**
     * Retrieves users which have a direct global permission.
     * <p>
     * Users which do not have any global permissions <i>explicitly</i> assigned to them will not be returned.
     * </p>
     *
     * @param filter
     *            an optional filter to limit the users to the ones having the {@code filter} in their usernames (can be
     *            {@code null}).
     * @param pageRequest
     *            specify the page of users which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more users and their highest associated global permission, ordered by username
     */
    @Nonnull
    Page<IPermittedUser> findHighestPermissionPerUser(@Nullable String filter, @Nonnull Pageable pageRequest);

    /**
     * Retrieves users which have a direct global permission.
     * <p>
     * Users which do not have any global permissions <i>explicitly</i> assigned to them will not be returned.
     * </p>
     *
     * @param pageRequest
     *            specify the page of users which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more users, ordered by username
     */
    @Nonnull
    Page<IUser> findUsersWithPermission(@Nonnull Pageable pageRequest);

    /**
     * Retrieves users which do not have a direct global permission.
     *
     * @param pageRequest
     *            specify the page of users which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more users, ordered by username
     */
    @Nonnull
    Page<IUser> findUsersWithoutPermission(Pageable pageRequest);

    /**
     * Retrieves groups which have a direct global permission.
     * <p>
     * Groups which do not have any global permissions <i>explicitly</i> assigned to them will not be returned.
     * </p>
     *
     * @param filter
     *            an optional filter to limit the groups by their names (can be {@code null}).
     * @param pageRequest
     *            specify the page of groups which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more groups and their highest associated global permission, ordered by name
     */
    @Nonnull
    Page<IPermittedGroup> findHighestPermissionPerGroup(@Nullable String filter, @Nonnull Pageable pageRequest);

    /**
     * Retrieves groups which have a direct global permission.
     * <p>
     * Groups which do not have any global permissions <i>explicitly</i> assigned to them will not be returned.
     * </p>
     *
     * @param pageRequest
     *            specify the page of groups which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more groups matching the specified criteria, ordered by name
     * @see #findGroupsWithoutPermission(Pageable)
     */
    @Nonnull
    Page<String> findGroupsWithPermission(@Nonnull Pageable pageRequest);

    /**
     * Retrieves groups which have not a direct global permission.
     *
     * @param pageRequest
     *            specify the page of groups which should be returned (can <b>not</b> be {@code null}).
     * @return a page containing zero or more groups matching the specified criteria, ordered by name
     * @see #findGroupsWithPermission(Pageable)
     */
    @Nonnull
    Page<String> findGroupsWithoutPermission(@Nonnull Pageable pageRequest);

}
