package com.pmi.tpd.api.tenant;

/**
 * A unique identifier for a tenant on the server whose data must be isolated from that of other tenants. This
 * identifier can be represented as a unique, case-sensitive name. As instances of this class may be logged during
 * debugging, an implementation of {@link #toString()} that includes the name is recommended.
 */
public interface ITenant {
    // IMPLEMENTOR NOTES:
    //
    // Properties may be filled in on this class as necessary -
    //
    // Tenant should obey equals(), i.e. if two Tenant instances represent the same tenant on the same server, they
    // should be equal.
    //
    // Please do not put any property on this object that can not be trivially converted to JSON.

    /**
     * Get the name of the tenant. A unique, case-sensitive identifier for a particular tenant.
     *
     * @return The name of the tenant
     */
    String name();
}
