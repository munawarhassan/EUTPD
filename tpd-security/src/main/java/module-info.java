module tpd.security {

    exports com.pmi.tpd.security;

    exports com.pmi.tpd.security.random;

    exports com.pmi.tpd.security.support;

    exports com.pmi.tpd.security.annotation;

    exports com.pmi.tpd.security.permission;

    exports com.pmi.tpd.security.spring;

    requires transitive tpd.api;

    requires java.annotation;

    requires com.google.common;

    requires spring.aop;

    requires spring.beans;

    requires spring.context;

    requires spring.core;

    requires transitive spring.data.commons;

    requires spring.expression;

    requires transitive spring.security.core;

    requires spring.security.config;

    requires transitive static com.querydsl.core;

    requires transitive org.slf4j;

}
