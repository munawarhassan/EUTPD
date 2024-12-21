package com.pmi.tpd.core.event.advisor.setup;

import javax.annotation.Nonnull;

/**
 * Interface allows to configure the event service.
 *
 * @author cfriedri
 * @since 1.0
 */
public interface ISetupConfig {

    /**
     * Gets a value indicating whether the application server has already completed the setup.
     *
     * @return Returns <code>true</code> if the application server has already completed the setup, <code>false</code>
     *         otherwise.
     */
    boolean isSetup();

    /**
     * Gets a value indicating whether the request match to the setup page.
     *
     * @param request
     *            the request to check
     * @return Returns <code>true</code> if the request match to the setup page, <code>false</code> otherwise.
     */
    boolean isSetupPage(@Nonnull String uri);

}
