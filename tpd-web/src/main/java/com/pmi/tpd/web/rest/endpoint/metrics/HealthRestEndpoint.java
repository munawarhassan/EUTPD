package com.pmi.tpd.web.rest.endpoint.metrics;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.metrics.heath.Health;
import com.pmi.tpd.web.core.rs.endpoint.Endpoint;
import com.pmi.tpd.web.core.rs.endpoint.RestEndpoint;

/**
 * Adapter to expose {@link HealthEndpoint} as an {@link RestEndpoint}.
 */
public class HealthRestEndpoint implements RestEndpoint {

    private static final String ENV_PREFIX = "endpoints.health.";

    private final HealthEndpoint delegate;

    private final boolean secure;

    private final Map<String, HttpStatus> statusMapping = new HashMap<>();

    private final Environment environment;

    private long lastAccess = 0;

    private Health cached;

    public HealthRestEndpoint(final HealthEndpoint delegate, final Environment environment) {
        this(delegate, environment, true);
    }

    public HealthRestEndpoint(final HealthEndpoint delegate, final Environment environment, final boolean secure) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
        this.environment = environment;
        this.secure = secure;
    }

    @Override
    public Object invoke(final Request request, final SecurityContext context) {
        final Principal principal = context.getUserPrincipal();
        if (!this.delegate.isEnabled()) {
            // Shouldn't happen because the request mapping should not be registered
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "This endpoint is disabled"), HttpStatus.NOT_FOUND);
        }
        final Health health = getHealth(principal);
        final HttpStatus status = this.statusMapping.get(health.getStatus().getCode());
        if (status != null) {
            return new ResponseEntity<>(health, status);
        }
        return health;
    }

    private Health getHealth(final Principal principal) {
        final long accessTime = System.currentTimeMillis();
        if (isCacheStale(accessTime) || isSecure(principal) || isUnrestricted()) {
            this.lastAccess = accessTime;
            this.cached = this.delegate.invoke();
        }
        if (isSecure(principal) || isUnrestricted()) {
            return this.cached;
        }
        return Health.status(this.cached.getStatus()).build();
    }

    private boolean isCacheStale(final long accessTime) {
        if (this.cached == null) {
            return true;
        }
        return accessTime - this.lastAccess > this.delegate.getTimeToLive();
    }

    private boolean isUnrestricted() {
        final Boolean sensitive = this.environment.getProperty(ENV_PREFIX + "sensitive", Boolean.class);
        return !this.secure || Boolean.FALSE.equals(sensitive);
    }

    private boolean isSecure(final Principal principal) {
        return principal != null
                && !principal.getClass().getName().contains(ApplicationConstants.Security.ANONYMOUS_USER);
    }

    @Override
    public String getId() {
        return this.delegate.getId();
    }

    @Override
    public boolean isSensitive() {
        return this.delegate.isSensitive();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Endpoint> getEndpointType() {
        return this.delegate.getClass();
    }

}
