package com.pmi.tpd.web.core.rs.endpoint;

/**
 * An endpoint that can be used to expose useful information to operations.
 *
 * @param <T>
 *            the endpoint data type
 */
public interface Endpoint<T> {

    /**
     * The logical ID of the endpoint. Must only contain simple letters, numbers and '_' characters (ie a
     * {@literal "\w"} regex).
     *
     * @return the endpoint ID
     */
    String getId();

    /**
     * Return if the endpoint is enabled.
     *
     * @return if the endpoint is enabled
     */
    boolean isEnabled();

    /**
     * Return if the endpoint is sensitive, i.e. may return data that the average user should not see. Mappings can use
     * this as a security hint.
     *
     * @return if the endpoint is sensitive
     */
    boolean isSensitive();

    /**
     * Called to invoke the endpoint.
     *
     * @return the results of the invocation
     */
    T invoke();

}
