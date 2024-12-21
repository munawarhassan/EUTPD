package com.pmi.tpd.security.permission;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Permissions available in application.
 *
 * @since 2.0
 * @author Christophe Friederich
 */
public enum Permission {

  // NOTE: next permission id is 4.

  /**
   * Allows access to application.
   * <p>
   * This allows the user to authenticate against application and counts him or
   * her towards the license limit.
   */
  USER(3, 0, I18nArgs.EMPTY),
  /**
   * Allows access to the common administration tasks in application, such as
   * granting global permissions.
   * <p>
   * This grants overall admin access to application.
   */
  ADMIN(2, 9000, I18nArgs.EMPTY, USER) {

    @Override
    public boolean isGrantableToAll() {
      return false;
    }
  },

  /**
   * Allows access to the advanced administration tasks in application.
   * <p>
   * In addition to the permissions already granted by {@link #ADMIN}, this grants
   * the user full access to the
   * restricted admin functions, such as enabling SSH access, updating the license
   * for application or migrating
   * databases.
   */
  SYS_ADMIN(1, 10000, I18nArgs.EMPTY, ADMIN) {

    @Override
    public boolean isGrantableToAll() {
      return false;
    }
  };

  /** Maps from {@link #getId() IDs} to their permissions. */
  private static Map<Integer, Permission> idToPermissionMap;

  /** Maps from {@link #getWeight() weights} to their permissions. */
  private static Map<Integer, Permission> weightToPermissionMap;

  /** */
  private final int id;

  /** */
  private final PermissionI18n i18n;

  /** */
  private final Set<Permission> inheritedPermissions;

  /** */
  private final Set<Class<?>> resourceTypes;

  /** */
  private final int weight;

  /** */
  // computed fields
  private Set<Permission> implyingPermissions;

  /** */
  private Set<Permission> inheritingPermissions;

  /**
   * Constructor for resource permissions.
   *
   * @param id
   *                      permission ID
   * @param weight
   *                      permission weight
   * @param resourceTypes
   *                      resource types this permission applies to
   * @param i18nArguments
   *                      i18n arguments
   * @param inherited
   *                      inherited permissions
   */
  Permission(final int id, final int weight, final Set<Class<?>> resourceTypes, final Object[] i18nArguments,
      final Permission... inherited) {
    this.id = id;
    this.i18n = new PermissionI18n(this, i18nArguments);
    this.resourceTypes = resourceTypes;
    this.weight = weight;

    final ImmutableSet.Builder<Permission> builder = ImmutableSet.builder();
    for (final Permission p : inherited) {
      appendInheritedPermissions(builder, p);
    }
    inheritedPermissions = builder.build();
  }

  /**
   * Constructor for global permissions. Global permissions are not associated
   * with any resource types.
   *
   * @param id
   *                      permission ID
   * @param weight
   *                      permission weight
   * @param i18nArguments
   *                      i18n arguments
   * @param inherited
   *                      inherited permissions
   */
  Permission(final int id, final int weight, final Object[] i18nArguments, final Permission... inherited) {
    this(id, weight, Collections.<Class<?>>emptySet(), i18nArguments, inherited);
  }

  /**
   * Gets a permission by its id.
   *
   * @param id
   *           id of the permission
   * @return the permission, or {@code null} if no permission matches the id
   */
  // This method is used by the GenericEnumUserType to map from the ID stored in
  // the database to the enumeration entry
  @Nullable
  public static Permission fromId(final int id) {
    if (idToPermissionMap == null) {
      final ImmutableMap.Builder<Integer, Permission> builder = ImmutableMap.builder();
      for (final Permission permission : values()) {
        builder.put(permission.getId(), permission);
      }
      idToPermissionMap = builder.build();
    }
    return idToPermissionMap.get(id);
  }

  /**
   * Gets a permission by its weight.
   *
   * @param weight
   *               weight of the permission
   * @return the permission, or {@code null} if no permission matches the weight
   */
  @Nullable
  public static Permission fromWeight(final int weight) {
    if (weightToPermissionMap == null) {
      final ImmutableMap.Builder<Integer, Permission> builder = ImmutableMap.builder();
      for (final Permission permission : values()) {
        builder.put(permission.getWeight(), permission);
      }
      weightToPermissionMap = builder.build();
    }
    return weightToPermissionMap.get(weight);
  }

  /**
   * @return Returns all the global permissions.
   * @see #isGlobal()
   */
  @Nonnull
  public static Set<Permission> getGlobalPermissions() {
    return Sets.filter(EnumSet.allOf(Permission.class),
        (Predicate<Permission>) Permission::isGlobal);
  }

  /**
   * Gets all the permissions associated with a resource.
   * <p>
   * For example, {@code getPermissionsOn(Project.class)} returns all the
   * permissions that applies to the resource,
   * <em>excluding {@link #isGlobal() global} permissions</em>.
   *
   * @param resourceClass
   *                      resource the permission must be applicable to
   * @return Returns all the permissions associated with a resource..
   * @see #isResource(Class)
   */
  @Nonnull
  public static Set<Permission> getPermissionsOn(final Class<?> resourceClass) {
    return Sets.filter(EnumSet.allOf(Permission.class),
        (Predicate<Permission>) permission -> permission.isResource(resourceClass));
  }

  /**
   * Gets the permission with the maximum {@link #getWeight() weigth}.
   *
   * @param p1
   *           permission 1 (can be {@code null})
   * @param p2
   *           permission 2 (can be {@code null})
   * @return the permission with the maximum weight
   */
  @Nullable
  public static Permission max(final Permission p1, final Permission p2) {
    if (p1 == null) {
      return p2;
    }
    return p2 == null || p1.getWeight() > p2.getWeight() ? p1 : p2;
  }

  /**
   * @return i18n-ed description of this permission suitable for user interfaces.
   * @since 2.0
   */
  @Nonnull
  public PermissionI18n getI18n() {
    return i18n;
  }

  /**
   * @return the id of this permission
   */
  public int getId() {
    return id;
  }

  /**
   * @return Returns the set of permissions that inherit this permission
   *         (excluding this permission).
   */
  @Nonnull
  public Set<Permission> getImplyingPermissions() {
    if (implyingPermissions == null) {
      final Set<Permission> inheriting = EnumSet.noneOf(Permission.class);
      inheriting.addAll(getInheritingPermissions());
      inheriting.remove(this);
      implyingPermissions = Collections.unmodifiableSet(inheriting);
    }
    return implyingPermissions;
  }

  /**
   * @return Returns all permissions this permission inherits.
   */
  public Set<Permission> getInheritedPermissions() {
    return inheritedPermissions;
  }

  /**
   * @return Returns all permissions that inherit this permission (including this
   *         permission).
   */
  @Nonnull
  public Set<Permission> getInheritingPermissions() {
    if (inheritingPermissions == null) {
      final Set<Permission> perms = EnumSet.of(this);
      for (final Permission p : Permission.values()) {
        if (p.getInheritedPermissions().contains(this)) {
          perms.add(p);
        }
      }
      inheritingPermissions = Collections.unmodifiableSet(perms);
    }
    return inheritingPermissions;
  }

  /**
   * Return all resource types that this permission applies to. This could be
   * domain objects, as well as numeric
   * values representing resource IDs.
   *
   * @return resources types that this permission applies to, or an empty set for
   *         {@link #isGlobal() global
   *         permissions}.
   * @see #isGlobal()
   * @see #isResource()
   * @since 3.8
   */
  @Nonnull
  public Set<Class<?>> getResourceTypes() {
    return resourceTypes;
  }

  /**
   * Retrieves the weight of this permission relative to other permissions.
   * <p>
   * Higher weight implies the permission has precedence over its lesser
   * counterpart(s). Weight can be used to perform
   * an in-order traversal of the permission hierarchy.
   *
   * @return the weight of this permission
   */
  public int getWeight() {
    return weight;
  }

  /**
   * Indicates whether this {@code Permission} can be granted globally.
   * <p>
   * Global permissions applies to all resources.
   *
   * @return {@code true} if this permission applies globally to all resources; or
   *         {@code false} if this permission
   *         applied to specific resource(s)
   */
  public boolean isGlobal() {
    return resourceTypes.isEmpty();
  }

  /**
   * @return {@code true} if the permission can be granted to a user, group or all
   *         users, {@code false} otherwise
   */
  public boolean isGrantable() {
    return true;
  }

  /**
   * Indicates whether this permission may be granted to all users, or if it must
   * be granted to users or groups
   * individually.
   *
   * @return {@code true} if the permission may be blanket granted to all users;
   *         or {@code false} if it may only be
   *         granted individually to users or groups
   */
  public boolean isGrantableToAll() {
    return isGrantable();
  }

  /**
   * Indicates whether this {@code Permission} only applies to specific
   * resource(s), such as projects and
   * repositories.
   * <p>
   * This is the logical negation of {@link #isGlobal()}; a permission cannot be
   * both global and resource.
   *
   * @return {@code true} if this permission can be applied to a specific
   *         resource; or {@code false} if this is a
   *         {@link #isGlobal() global} permission
   * @see #isGlobal()
   */
  public boolean isResource() {
    return !isGlobal();
  }

  /**
   * Indicates whether this permission applies to a given resource type.
   * <p>
   * Caveat: <em>global</em> permissions (that applies to all resources) will
   * return {@code false}.
   *
   * @param resourceClass
   *                      the type of the resource
   * @return {@code true} if this permission applies to instances of
   *         {@code resourceClass}
   * @see #isGlobal()
   * @see #isResource()
   */
  public boolean isResource(final Class<?> resourceClass) {
    return !isGlobal() && Iterables.any(resourceTypes, new IsAssignableFrom(resourceClass));
  }

  private void appendInheritedPermissions(final ImmutableSet.Builder<Permission> builder, final Permission p) {
    builder.add(p);
    for (final Permission perm : p.getInheritedPermissions()) {
      appendInheritedPermissions(builder, perm);
    }
  }

  /**
   * Common i18n args used by permissions. They can't exist directly as constants
   * in the {@code Permission} class due
   * to the forward-reference issue.
   */
  private static class I18nArgs {

    /** */
    static final Object[] EMPTY = new Object[0];
  }

  /**
   * The inversion of Guava's {@code Predicates.assignableFrom}.
   */
  private static final class IsAssignableFrom implements Predicate<Class<?>> {

    /** */
    private final Class<?> clazz;

    private IsAssignableFrom(final Class<?> clazz) {
      this.clazz = clazz;
    }

    @Override
    public boolean apply(final Class<?> input) {
      return input.isAssignableFrom(clazz);
    }
  }
}
