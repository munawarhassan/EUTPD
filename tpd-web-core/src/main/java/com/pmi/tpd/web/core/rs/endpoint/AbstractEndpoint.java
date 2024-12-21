package com.pmi.tpd.web.core.rs.endpoint;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Abstract base for {@link Endpoint} implementations.
 *
 * @param <T>
 *            the endpoint data type
 */
public abstract class AbstractEndpoint<T> implements Endpoint<T>, EnvironmentAware {

    private static final String ENDPOINTS_ENABLED_PROPERTY = "endpoints.enabled";

    private Environment environment;

    /**
     * Endpoint identifier. With HTTP monitoring the identifier of the endpoint is mapped to a URL (e.g. 'foo' is mapped
     * to '/foo').
     */
    @NotNull
    @Pattern(regexp = "\\w+", message = "ID must only contains letters, numbers and '_'")
    private String id;

    /**
     * Mark if the endpoint exposes sensitive information.
     */
    private boolean sensitive;

    /**
     * Enable the endpoint.
     */
    private Boolean enabled;

    /**
     * Create a new sensitive endpoint instance. The enpoint will enabled flag will be based on the spring
     * {@link Environment} unless explicitly set.
     *
     * @param id
     *            the endpoint ID
     */
    public AbstractEndpoint(final String id) {
        this(id, true);
    }

    /**
     * Create a new endpoint instance. The enpoint will enabled flag will be based on the spring {@link Environment}
     * unless explicitly set.
     *
     * @param id
     *            the endpoint ID
     * @param sensitive
     *            if the endpoint is sensitive
     */
    public AbstractEndpoint(final String id, final boolean sensitive) {
        this.id = id;
        this.sensitive = sensitive;
    }

    /**
     * Create a new endpoint instance.
     *
     * @param id
     *            the endpoint ID
     * @param sensitive
     *            if the endpoint is sensitive
     * @param enabled
     *            if the endpoint is enabled or not.
     */
    public AbstractEndpoint(final String id, final boolean sensitive, final boolean enabled) {
        this.id = id;
        this.sensitive = sensitive;
        this.enabled = enabled;
    }

    protected final Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean isEnabled() {
        if (this.enabled != null) {
            return this.enabled;
        }
        if (this.environment != null) {
            return this.environment.getProperty(ENDPOINTS_ENABLED_PROPERTY, Boolean.class, true);
        }
        return true;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isSensitive() {
        return this.sensitive;
    }

    public void setSensitive(final boolean sensitive) {
        this.sensitive = sensitive;
    }

}
