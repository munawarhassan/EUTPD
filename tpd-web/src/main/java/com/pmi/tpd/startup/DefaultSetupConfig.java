package com.pmi.tpd.startup;

import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.exception.InfrastructureException;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.advisor.setup.ISetupConfig;
import com.pmi.tpd.web.rest.rsrc.api.setup.SetupResource;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultSetupConfig implements ISetupConfig {

    @Override
    public boolean isSetup() {
        try {
            final IApplicationProperties settings = ComponentManager.getInstance().getApplicationProperties();
            return settings.isSetup() || settings.isAutoSetup();
        } catch (final InfrastructureException ex) {
            // Spring is not started yet
            return false;
        }

    }

    @Override
    public boolean isSetupPage(final String uri) {
        Assert.checkNotNull(uri, "uri");
        return uri.contains(SetupResource.FULL_PAGE_NAME);
    }

}
