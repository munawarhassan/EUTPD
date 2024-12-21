package com.pmi.tpd.core.elasticsearch.junit.jupiter;

public class ClusterConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClusterConnectionException(final String message) {
        super(message);
    }

    public ClusterConnectionException(final Throwable cause) {
        super(cause);
    }
}