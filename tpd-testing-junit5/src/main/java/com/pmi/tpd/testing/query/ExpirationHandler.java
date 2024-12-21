package com.pmi.tpd.testing.query;

/**
 * Strategies for handling expired timeouts of the {@link TimedQuery}.
 */
@FunctionalInterface
public interface ExpirationHandler {

    /**
     * Handle timeout expiration for given query.
     *
     * @param <T>
     *            type of the query result
     * @param query
     *            timed query name / question
     * @param currentValue
     *            current evaluation of the query
     * @param timeout
     *            timeout of the query
     * @return result value to be returned by the query ater the timeout expiration
     */
    <T> T expired(String query, T currentValue, long timeout);

    public static final ExpirationHandler RETURN_CURRENT = new ExpirationHandler() {

        @Override
        public <T> T expired(final String query, final T currentValue, final long timeout) {
            return currentValue;
        }
    };

    public static final ExpirationHandler RETURN_NULL = new ExpirationHandler() {

        @Override
        public <T> T expired(final String query, final T currentValue, final long timeout) {
            return null;
        }
    };

    public static final ExpirationHandler THROW_ASSERTION_ERROR = new ExpirationHandler() {

        @Override
        public <T> T expired(final String query, final T currentValue, final long timeout) {
            throw new AssertionError("Timeout <" + timeout + "> expired for: " + query);
        }
    };

    public static final ExpirationHandler THROW_ILLEGAL_STATE = new ExpirationHandler() {

        @Override
        public <T> T expired(final String query, final T currentValue, final long timeout) {
            throw new IllegalStateException("Timeout <" + timeout + "> expired for: " + query);
        }
    };

}
