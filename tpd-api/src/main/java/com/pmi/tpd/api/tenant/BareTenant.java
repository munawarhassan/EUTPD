package com.pmi.tpd.api.tenant;

/**
 * Bare minimum {@link ITenant} for use by {@link BareTenantAccessor.compatibility.CompatibilityTenantAccessor}
 */
public class BareTenant implements ITenant {

    @Override
    public String name() {
        return "tenant";
    }

    @Override
    public String toString() {
        return name();
    }
}
