module tpd.scheduler {

    exports com.pmi.tpd.scheduler.quartz.hazelcast;

    exports com.pmi.tpd.scheduler.status;

    exports com.pmi.tpd.scheduler.support;

    exports com.pmi.tpd.scheduler;

    exports com.pmi.tpd.scheduler.quartz.spi;

    exports com.pmi.tpd.scheduler.spi;

    exports com.pmi.tpd.scheduler.exec;

    exports com.pmi.tpd.scheduler.exec.cluster;

    exports com.pmi.tpd.scheduler.exec.support;

    exports com.pmi.tpd.scheduler.quartz;

    exports com.pmi.tpd.scheduler.spring;

    requires transitive atlassian.util.concurrent;

    requires java.annotation;

    requires com.google.common;

    requires java.sql;

    requires transitive javax.inject;

    requires org.slf4j;

    requires transitive quartz;

    requires spring.aop;

    requires spring.beans;

    requires spring.context;

    requires spring.core;

    requires transitive tpd.api;

    requires static tpd.cluster;

    requires tpd.spring;

    requires transitive tpd.web.core;

    requires transitive com.querydsl.core;

    requires transitive spring.data.commons;
}
