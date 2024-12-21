package com.pmi.tpd.core.elasticsearch.junit.jupiter;

public enum IntegrationtestEnvironment {

    ELASTICSEARCH,
    OPENSEARCH,
    UNDEFINED;

    public static final String SYSTEM_PROPERTY = "sde.integration-test.environment";

    public static IntegrationtestEnvironment get() {

        final String property = System.getProperty(SYSTEM_PROPERTY, "elasticsearch");
        switch (property.toUpperCase()) {
            case "ELASTICSEARCH":
                return ELASTICSEARCH;
            case "OPENSEARCH":
                return OPENSEARCH;
            default:
                return UNDEFINED;
        }
    }
}