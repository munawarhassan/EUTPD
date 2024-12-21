module tpd.cluster {

    exports com.pmi.tpd.cluster.annotation;

    exports com.pmi.tpd.cluster.spring;

    exports com.pmi.tpd.cluster;

    exports com.pmi.tpd.cluster.hazelcast;

    exports com.pmi.tpd.cluster.spi;

    exports com.pmi.tpd.cluster.concurrent;

    exports com.pmi.tpd.cluster.event;

    exports com.pmi.tpd.cluster.latch;

    exports com.pmi.tpd.cluster.util;

    requires com.google.common;

    requires transitive com.hazelcast.core;

    requires org.codehaus.commons.compiler;

    requires org.codehaus.janino;

    requires transitive hazelcast.spring;

    requires java.annotation;

    requires transitive javax.inject;

    requires transitive javax.servlet.api;

    requires org.apache.commons.lang3;

    requires org.slf4j;

    requires spring.beans;

    requires transitive spring.context;

    requires transitive spring.core;

    requires spring.tx;

    requires tpd.api;

    requires tpd.spring;

}
