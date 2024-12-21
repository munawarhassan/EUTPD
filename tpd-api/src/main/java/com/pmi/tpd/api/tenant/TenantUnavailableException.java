package com.pmi.tpd.api.tenant;

/**
 * Thrown when an operation is being performed for a tenant that is not, or is no longer installed on the current
 * server.
 */
public class TenantUnavailableException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
