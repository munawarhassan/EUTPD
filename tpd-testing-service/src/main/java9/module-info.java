/**
 * @author Christophe Friederich
 */
// TODO find solution to use JPSM, hazelcast and hazelcast-tests use same module name and share same namespace.
module tpd.testing.service {

    exports com.pmi.tpd.service.testing;

    exports com.pmi.tpd.service.testing.mock;

    exports com.pmi.tpd.service.testing.mockito;

    exports com.pmi.tpd.service.testing.junit5;

    requires tpd.api;

    requires tpd.security;

    requires transitive tpd.testing.junit5;

    requires com.google.common;

    requires transitive org.junit.jupiter.api;

    requires org.mockito;

    requires org.hamcrest;

    requires static com.github.spotbugs.annotations;

    requires org.apache.commons.lang3;

    requires org.slf4j;

    requires spring.aop;

    requires spring.beans;

    requires spring.context;

    requires spring.core;

    requires spring.expression;

    requires spring.security.core;

    requires spring.data.commons;

    requires com.hazelcast.core;

    requires hazelcast-spring;

}
