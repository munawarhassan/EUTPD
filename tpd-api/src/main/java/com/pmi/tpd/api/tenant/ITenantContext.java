package com.pmi.tpd.api.tenant;

import javax.annotation.Nonnull;

/**
 * Service for retrieving the tenant (if any) in the context of the application.
 */
public interface ITenantContext {

  /**
   * Get the tenant for the current thread
   *
   * @return The tenant for the current thread
   * @throws IllegalStateException
   *                               If no tenant is set
   */
  @Nonnull
  ITenant getCurrentTenant();
}
