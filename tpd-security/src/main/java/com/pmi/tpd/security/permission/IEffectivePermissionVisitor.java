package com.pmi.tpd.security.permission;

import javax.annotation.Nonnull;

/**
 * Visitor for {@link IEffectivePermission effective permissions}. Implementors
 * are encouraged to extend
 * {@link AbstractEffectivePermissionVisitor} instead of directly implementing
 * this interface.
 *
 * @since 2.0
 */
public interface IEffectivePermissionVisitor<T> {

  /**
   * @param permission
   * @return
   */
  T visit(@Nonnull IEffectiveGlobalPermission permission);

  /**
   * @param permission
   * @return
   */
  T visit(@Nonnull IEffectiveProductPermission permission);

  /**
   * @param permission
   * @return
   */
  T visit(@Nonnull IEffectiveSubmissionPermission permission);
}
