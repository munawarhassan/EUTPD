package com.pmi.tpd.core.event.advisor;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;

public class SimpleSetupConfig extends AbstractConfigurable implements ISetupConfig {

    public static boolean IS_SETUP = false;

    @Override
    public boolean isSetup() {
        return IS_SETUP;
    }

    @Override
    public boolean isSetupPage(@Nonnull final String uri) {
        return checkNotNull(uri, "uri").contains("setup");
    }
}
