package com.pmi.tpd.security.permission;

import javax.annotation.Nullable;

/**
 * Encapsulates the graph of a user's permissions, including any permissions
 * granted by their group memberships.
 * <p>
 * The primary goal of constructing a graph is to calculate all of the
 * permissions the user has been granted via their
 * group memberships in a single traversal, rather than repeatedly traversing
 * memberships for each permission check.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IPermissionGraph {

  /**
   * Determines whether the user has a effective permission.
   * <p>
   * The {@code resource} provided can fall into three categories:
   * <ul>
   * <li>{@code null}, for {@link Permission#isGlobal() global} permission
   * checks</li>
   * <li>{@code null}, for "any" {@link Permission#isResource() resource}
   * permission checks</li>
   * <li>Any non-{@code null} resource to check permission against</li>
   * </ul>
   *
   * @param permission
   *                   the permission to test
   * @param resource
   *                   {@code null} or the resource the user should have the
   *                   permission on
   * @return {@code true} if the user has been granted the requested permission,
   *         whether directly or via their group
   *         memberships; otherwise, {@code false}
   */
  boolean isGranted(Permission permission, @Nullable Object resource);

}
