package com.pmi.tpd.metrics.heath;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.euceg.backend.core.IBackendManager;

/**
 * SpringBoot Actuator HealthIndicator check for Domibus Server.
 */
public class DomibusBackendHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusBackendHealthIndicator.class);

    private final IBackendManager backendManager;

    public DomibusBackendHealthIndicator(final IBackendManager backendManager) {
        this.backendManager = checkNotNull(backendManager, "backendManager");;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initializing backendService health indicator");
        }
        try {
            if (!backendManager.isRunning()) {
                builder.outOfService();
            } else {
                backendManager.healthCheck(false);
                builder.up().withDetail("type", "server").withDetail("product", "Domibus");
            }

        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot connect to Domibus server. Error: {}", e.getMessage());
            }
            builder.down(e);
        }
    }
}
