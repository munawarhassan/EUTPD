package com.pmi.tpd.api.tenant;

/**
 * Thrown when code that is already in one tenant context tries to execute on behalf of another tenant
 */
public class UnexpectedTenantChangeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
