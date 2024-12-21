package com.pmi.tpd.api.tenant;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

public interface ITenantAccessor {

    /**
     * Retrieve the tenants currently installed on this server. If a tenant is currently in the process of being
     * introduced, this method will block to prevent race conditions with processing of the TenantArrived event.
     *
     * @return the currently installed tenants. May be empty if no tenants are installed.
     */
    Iterable<ITenant> getAvailableTenants();

    /**
     * Execute some code as a particular tenant. This should only be necessary in circumstances where the application
     * has not already put a tenant in the current context. If you attempt to use this method to change tenant contexts
     * (i.e. you are in the context of one tenant, and try to execute code as a different tenant) an exception will be
     * thrown
     *
     * @param tenant
     *            the tenant to execute the call on behalf of
     * @param call
     *            the Callable to execute
     * @param <T>
     *            the expected return type of the call
     * @return the return value of the provided Callable.
     * @throws InvocationTargetException
     *             an exception was thrown by the provided Callable
     * @throws TenantUnavailableException
     *             the operation could not be performed because the tenant is not currently installed on the server.
     *             This may happen because a tenant has been uninstalled since the last call to
     *             {@link #getAvailableTenants()}.
     * @throws UnexpectedTenantChangeException
     *             the method was called from the context of an tenant other than the one provided as a parameter
     */
    <T> T asTenant(ITenant tenant, Callable<T> call) throws TenantUnavailableException, InvocationTargetException;
}
