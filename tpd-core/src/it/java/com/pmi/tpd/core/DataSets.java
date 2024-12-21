package com.pmi.tpd.core;

public class DataSets {

    public static final String PREFIX = "classpath:dbunit/";

    public static final String SUFFIX = ".xml";

    public static final String GLOBAL_PERMISSIONS = PREFIX + "global_permission" + SUFFIX;

    public static final String USERS = PREFIX + "users" + SUFFIX;

    private DataSets() {
        throw new UnsupportedOperationException("DataSets is a constants class and cannot be instantiated");
    }
}
