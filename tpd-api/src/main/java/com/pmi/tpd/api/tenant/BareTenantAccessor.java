package com.pmi.tpd.api.tenant;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * Naive implementation of TenantAccessor that always declares a single tenant available. Deployed for backwards
 * compatibility between plugins and applications that have not yet introduced the tenant API.
 */
public class BareTenantAccessor implements ITenantAccessor, ITenantContext {

    private static final ITenant TENANT = new BareTenant();

    @Override
    public ITenant getCurrentTenant() {
        return TENANT;
    }

    @Override
    public Iterable<ITenant> getAvailableTenants() {
        return Collections.singleton(TENANT);
    }

    @Override
    public <T> T asTenant(final ITenant tenant, final Callable<T> call)
            throws InvocationTargetException, TenantUnavailableException {
        // Something very strange has happened.
        if (!tenant.equals(TENANT)) {
            throw new UnexpectedTenantChangeException();
        }

        try {
            return call.call();
        } catch (final Exception e) {
            throw new InvocationTargetException(e);
        }
    }
}
