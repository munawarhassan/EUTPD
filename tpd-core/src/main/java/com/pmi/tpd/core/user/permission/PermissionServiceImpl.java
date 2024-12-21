package com.pmi.tpd.core.user.permission;

import static com.google.common.collect.Lists.newArrayList;
import static com.pmi.tpd.api.paging.PageUtils.asIterable;
import static com.pmi.tpd.security.support.CommonValidations.validateGlobalPermission;
import static com.pmi.tpd.security.support.CommonValidations.validateGroup;
import static com.pmi.tpd.security.support.CommonValidations.validateGroups;
import static com.pmi.tpd.security.support.CommonValidations.validatePageRequest;
import static com.pmi.tpd.security.support.CommonValidations.validateResourcePermission;
import static com.pmi.tpd.security.support.CommonValidations.validateUser;
import static java.util.Optional.ofNullable;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.paging.IPageProvider;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.api.util.Timer;
import com.pmi.tpd.api.util.TimerUtils;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IEffectivePermission;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * None of the service methods here are restricted by permissions because this
 * is the service that controls them, and it
 * needs to be available to all contexts.
 */
@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements IPermissionService {

  /** */
  public static final int GROUP_PAGESIZE = 1000;

  /** */
  public static final String GROUP_PERMISSION_SEARCH = "Group Permission Search";

  /** */
  public static final int USER_GROUP_PAGE_LIMIT = 500;

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

  /** */
  private final IAuthenticationContext authenticationContext;

  /** */
  private final IEffectivePermissionRepository effectivePermissionDao;

  // private final FeatureManager featureManager;
  //
  // private final RecoveryModeService recoveryModeService;

  /** */
  private final IPermissionGraphFactory permissionGraphFactory;

  /** */
  private final IUserService userService;

  /** */
  // TODO to fix find better solution to retrieve paging users.
  private final int maxUserPageSize = 1000;

  /**
   * @param authenticationContext
   * @param effectivePermissionDao
   * @param permissionGraphFactory
   * @param userService
   */
  @Autowired
  public PermissionServiceImpl(final IAuthenticationContext authenticationContext,
      final IEffectivePermissionRepository effectivePermissionDao,
      final IPermissionGraphFactory permissionGraphFactory, final IUserService userService
  // final FeatureManager featureManager, final RecoveryModeService
  // recoveryModeService
  ) {
    this.authenticationContext = authenticationContext;
    this.effectivePermissionDao = effectivePermissionDao;
    this.permissionGraphFactory = permissionGraphFactory;
    this.userService = userService;
    // this.featureManager = featureManager;
    // this.recoveryModeService = recoveryModeService;
  }

  @Override
  public boolean hasGlobalPermission(@Nullable final String username, @Nonnull final Permission permission) {
    final IUser user = ofNullable(username).map(this::findUserByName).orElse(null);
    return hasGlobalPermission(user, permission);
  }

  @Override
  public boolean hasGlobalPermission(@Nullable final IUser user, @Nonnull final Permission permission) {
    validateGlobalPermission(permission);

    return hasPermission(user, null, permission);
  }

  @Override
  public boolean hasGlobalPermission(@Nullable final UserAuthenticationToken token, final Permission permission) {
    validateGlobalPermission(permission);

    return hasPermission(token, null, permission);
  }

  @Override
  public boolean hasGlobalPermission(@Nonnull final Permission permission) {
    return hasGlobalPermission(authenticationContext.getCurrentToken().orElse(null), permission);
  }

  @Override
  public boolean hasAnyUserPermission(@Nonnull final IUser user, @Nonnull final Permission permission) {
    validateUser(user);
    validateResourcePermission(permission);

    return hasPermission(user, null, permission);
  }

  @Override
  public boolean hasAnyUserPermission(@Nonnull final Permission permission) {
    return authenticationContext.getCurrentToken()
        .map(token -> hasAnyUserPermission(token, permission))
        .orElse(false);
  }

  @Override
  public boolean hasDirectGlobalUserPermission(@Nonnull final Permission permission) {
    validateGlobalPermission(permission);

    return hasDirectUserPermission(authenticationContext.getCurrentToken().get(), null, permission);
  }

  private boolean hasDirectUserPermission(final UserAuthenticationToken token,
      final Object resource,
      final Permission permission) {
    return token != null && hasDirectUserPermission(token.getPrincipal(), resource, permission);
  }

  @Override
  public boolean hasGlobalGroupPermission(@Nonnull final Permission permission, @Nonnull final String group) {
    validateGlobalPermission(permission);
    validateGroup(group);

    final GroupPermissionCriteria criteria = new GroupPermissionCriteria.Builder(group).permission(permission)
        .build();
    return effectivePermissionDao.isGrantedToGroup(criteria);
  }

  @Nonnull
  @Override
  public Page<IUser> getGrantedUsers(@Nonnull final Permission permission, @Nonnull final Pageable request) {
    validateGlobalPermission(permission);
    validatePageRequest(request);

    return PageUtils.asPageOf(IUser.class,
        effectivePermissionDao.findUsers(permission, request, QUserEntity.userEntity.activated.isTrue()));
  }

  @Nonnull
  @Override
  public Page<String> getGrantedGroups(@Nonnull final Permission permission, @Nonnull final Pageable request) {
    validateGlobalPermission(permission);
    validatePageRequest(request);

    return effectivePermissionDao.findGroups(permission, request);
  }

  @Override
  public boolean hasGlobalPermissionThroughGroupMembership(@Nonnull final Permission permission,
      @Nonnull final Set<String> excludedGroups) {
    validateGlobalPermission(permission);
    validateGroups(excludedGroups);

    return hasPermissionsThroughGroup(authenticationContext.getCurrentToken().get(),
        null,
        permission,
        excludedGroups);
  }

  @Nonnull
  @Override
  public Set<String> getUsersWithPermission(@Nonnull final Permission permission) {
    validateGlobalPermission(permission);

    final Set<String> usersWithPermission = new HashSet<>();

    final Iterable<IUser> users = Iterables.filter(
        asIterable(request -> getGrantedUsers(permission, request), maxUserPageSize),
        user -> user.isActivated());

    Iterables.addAll(usersWithPermission, Iterables.transform(users, user -> user.getUsername()));

    final Iterable<String> groups = asIterable(request -> getGrantedGroups(permission, request), GROUP_PAGESIZE);

    for (final String group : groups) {
      final Iterable<UserRequest> usersInGroup = Iterables.filter(
          asIterable(request -> userService.findUsersByGroup(group, request), maxUserPageSize),
          user -> user.isActivated());

      Iterables.addAll(usersWithPermission, Iterables.transform(usersInGroup, user -> user.getUsername()));
    }

    return usersWithPermission;
  }

  @Override
  public Permission getHighestGlobalPermission(final IUser user) {
    if (isNullOrInactive(user)) {
      return null;
    }
    final List<Permission> globalPermissions = getOrderedGlobalPermissions();
    for (final Permission globalPermission : globalPermissions) {
      if (hasGlobalPermission(user, globalPermission)) {
        return globalPermission;
      }
    }
    return null;
  }

  @Override
  public Permission getHighestGlobalPermission(final String username) {
    final IUser user = username != null ? findUserByName(username) : null;
    return user != null ? getHighestGlobalPermission(user) : null;
  }

  @Override
  public Permission getHighestGlobalGroupPermission(final String groupName) {
    if (groupName == null) {
      return null;
    }
    final List<Permission> globalPermissions = getOrderedGlobalPermissions();
    for (final Permission globalPermission : globalPermissions) {
      if (hasGlobalGroupPermission(globalPermission, groupName)) {
        return globalPermission;
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public Iterable<IEffectivePermission> getEffectivePermissions(@Nonnull final IUser user) {
    return permissionGraphFactory.createGraph(user);
  }

  @VisibleForTesting
  PartitionedGroups getAllGroups(final String username) {
    // Assemble all the groups for the user
    final int pageSize = USER_GROUP_PAGE_LIMIT;
    return partitionGroups(
        asIterable((IPageProvider<String>) request -> userService.findGroupsByUser(username, request),
            PageUtils.newRequest(0, pageSize)),
        pageSize);
  }

  /**
   * Attempts to produce a unified graph of <i>all</i> of the specified
   * {@link IUser user's} permissions, including
   * any permissions granted by the groups they're a member of.
   * <p>
   * Note: With its current implementation, the computed graph should not be
   * cached for long, because changes made to
   * the user's groups, their permissions, or the user's direct permissions will
   * not be reflected. At the moment, the
   * life span of the computed graph is bounded to a single request.
   *
   * @param user
   *             the user to graph permissions for
   * @return a graph of the user's permissions (or something that will simulate
   *         one!)
   */
  @Nonnull
  private IPermissionGraph createPermissionGraph(@Nonnull final IUser user) {
    validateUser(user);

    return permissionGraphFactory.createGraph(user);
  }

  private static boolean isNullOrInactive(final IUser user) {
    return user == null || !user.isActivated();
  }

  private IUser findUserByName(final String username) {
    return userService.getUserByName(username);
  }

  private List<Permission> getOrderedGlobalPermissions() {
    final List<Permission> globalPermissions = newArrayList(Permission.getGlobalPermissions());
    Collections.sort(globalPermissions,
        (o1, o2) -> o1 == o2 ? 0 : o1.getWeight() < o2.getWeight() ? 1 : o1.getWeight() > o2.getWeight() ? -1 : 0);
    return globalPermissions;
  }

  @Nonnull
  private IPermissionGraph getPermissionGraph(@Nonnull final UserAuthenticationToken token) {
    // When dealing with a user token, which is bound to the security context and
    // cleared at the end of each
    // server request, attempt to cache _all_ of the user's permissions the first
    // time a permission check is
    // performed in a given request. This way the user's groups are only traversed
    // once and all subsequent
    // permission checks can be answered directly from the permission graph held in
    // memory, making them all
    // substantially more performant.
    IPermissionGraph graph = token.getPermissions();
    if (graph == null) {
      graph = createPermissionGraph(token.getPrincipal());

      token.setPermissions(graph);
    }
    return graph;
  }

  @Override
  public boolean hasAnyUserPermission(final UserAuthenticationToken token, final Permission permission) {
    validateResourcePermission(permission);

    return hasPermission(token, null, permission);
  }

  private boolean hasDirectUserPermission(final IUser user, final Object resource, final Permission permission) {
    if (isNullOrInactive(user)) {
      // null users (anonymous) cannot have permissions associated with them and
      // inactive users cannot login therefore we always return false
      return false;
    }
    final Long userId = user.getId();
    if (userId == null) {
      // If the user hasn't been persisted, clearly they don't have any permissions.
      return false;
    }

    final UserPermissionCriteria criteria = new UserPermissionCriteria.Builder(userId).permission(permission)
        // .resource(resource)
        .build();
    final boolean granted = effectivePermissionDao.isGrantedToUser(criteria);
    if (granted) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("{}: {}, or an inheriting permission, has been explicit granted",
            user.getUsername(),
            permission);
      }
    } else {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("{}: {} has not been explicitly granted", user.getUsername(), permission);
      }
    }
    return granted;
  }

  private boolean hasPermission(@Nullable final UserAuthenticationToken token,
      final Object resource,
      final Permission permission) {
    if (token == null) {
      return false;
    }

    return token.isGranted(permission, resource) || hasPermission(token.getPrincipal(), resource, permission);
  }

  private boolean hasPermission(@Nullable final IUser user,
      @Nullable final Object resource,
      @Nonnull final Permission permission) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("user = {}, resource = {}, requested permission = {}", user, resource, permission);
    }

    if (isNullOrInactive(user)) {
      // Anonymous users cannot be granted permissions by definition.
      // Inactive users cannot login so we shouldn't honour any of their permissions.
      return false;
    }

    // // check for recovery mode
    // if (recoveryModeService.isRecoveryModeOn()
    // && user.getName().equals(recoveryModeService.getRecoveryUsername())) {
    // // recovery user is assumed to have all permissions
    // return true;
    // }

    IPermissionGraph graph;
    final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);
    if (user.equals(currentUser)) {
      // Optimisation for when the current user explicitly queries for their own
      // permission.
      // This can happen for various reasons within a request.

      // noinspection ConstantConditions
      graph = getPermissionGraph(authenticationContext.getCurrentToken().get());
    } else {
      // if we can't use the graph associated with the current user, create a new
      // graph
      graph = createPermissionGraph(user);
    }

    return graph.isGranted(permission, resource);
  }

  private boolean hasPermissionsThroughGroup(@Nonnull final Principal user,
      final Object resource,
      final Permission permission,
      final Set<String> excludedGroups) {
    final String name = user.getName();

    // Iterate over the user's groups and determine whether any of them are granted
    // the requested permission.
    //
    // The alternative approach is to iterate over groups which are granted the
    // permission and determine if the user
    // is a member of any of those groups. Which is more efficient depends on
    // whether the user has more groups or
    // the
    // permission has been granted to more groups. Since there's no cut and dried
    // answer, either approach is viable.
    // This approach is chosen because it seems likely users will not be members of
    // large numbers of groups, and
    // this
    // approach makes the most efficient use of our own permission DAO.
    try (Timer ignored = TimerUtils.start(GROUP_PERMISSION_SEARCH)) {
      final Pageable pageRequest = PageUtils.newRequest(0, USER_GROUP_PAGE_LIMIT);
      Page<String> page = userService.findGroupsByUser(name, pageRequest);
      while (true) {
        // All of the groups retrieved from findGroupsByUser will be lowercased due to
        // the implementation of
        // the UserService, so in order for this removal to do the right thing, it needs
        // to lowercase as well
        final Set<String> groups = Sets.newHashSet(page.getContent());
        Iterables.removeAll(groups, excludedGroups);
        if (groups.isEmpty()) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("All groups on the page have been excluded");
          }
        } else {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Testing for permission against {} groups", groups.size());
          }

          final GroupPermissionCriteria criteria = new GroupPermissionCriteria.Builder(groups)
              .permission(permission)
              // .resource(resource)
              .build();
          final boolean granted = effectivePermissionDao.isGrantedToGroup(criteria);
          if (granted) {
            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("{}: Permission {} granted by group membership", name, permission);
            }

            return true;
          }
        }

        if (page.isLast()) {
          LOGGER.trace("{}: All group memberships have been exhausted", name);
          break;
        }
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("{}: Loading next page of groups", name);
        }
        // noinspection ConstantConditions
        page = userService.findGroupsByUser(name, page.nextPageable());
      }
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("permission not granted by any group membership");
      }
      return false;
    }
  }

  private PartitionedGroups partitionGroups(final Iterable<String> groups, final int partitionSize) {
    return new PartitionedGroups() {

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public Iterator<Collection<String>> iterator() {
        // We need to handle the special case of an empty list of groups, but don't want
        // to invoke
        // PagedIterable.iterator twice and incur the cost of _two_ lookups
        // tofindGroupsByUser().
        final Iterator<String> iterator = groups.iterator();
        if (!iterator.hasNext()) {
          // Ensure we always return at least one element
          return Collections.<Collection<String>>singletonList(Collections.<String>emptyList()).iterator();
        }
        return (Iterator) Iterators.partition(iterator, partitionSize);
      }
    };
  }

}
