module tpd.api {

    exports com.pmi.tpd.api;

    exports com.pmi.tpd.api.audit;

    exports com.pmi.tpd.api.audit.annotation;

    exports com.pmi.tpd.api.cache;

    exports com.pmi.tpd.api.context;

    exports com.pmi.tpd.api.context.annotation;

    exports com.pmi.tpd.api.config;

    exports com.pmi.tpd.api.config.annotation;

    exports com.pmi.tpd.api.event;

    exports com.pmi.tpd.api.event.advisor;

    exports com.pmi.tpd.api.event.advisor.event;

    exports com.pmi.tpd.api.event.publisher;

    exports com.pmi.tpd.api.event.annotation;

    exports com.pmi.tpd.api.exec;

    exports com.pmi.tpd.api.i18n;

    exports com.pmi.tpd.api.i18n.support;

    exports com.pmi.tpd.api.lifecycle;

    exports com.pmi.tpd.api.lifecycle.config;

    exports com.pmi.tpd.api.lifecycle.notification;

    exports com.pmi.tpd.api.model;

    exports com.pmi.tpd.api.crypto;

    exports com.pmi.tpd.api.user;

    exports com.pmi.tpd.api.user.avatar;

    exports com.pmi.tpd.api.tenant;

    exports com.pmi.tpd.api.paging;

    exports com.pmi.tpd.api.scheduler;

    exports com.pmi.tpd.api.scheduler.config;

    exports com.pmi.tpd.api.scheduler.status;

    exports com.pmi.tpd.api.scheduler.util;

    exports com.pmi.tpd.api.util;

    exports com.pmi.tpd.api.util.xml;

    exports com.pmi.tpd.api.util.zip;

    exports com.pmi.tpd.api.versioning;

    exports com.pmi.tpd.api.exception;

    opens com.pmi.tpd.api.paging to com.fasterxml.jackson.databind;

    requires java.annotation;

    requires transitive spring.beans;

    requires transitive spring.core;

    requires transitive spring.context;

    requires transitive spring.data.commons;

    requires transitive com.google.common;

    requires org.apache.commons.lang3;

    requires transitive org.joda.time;

    requires static com.fasterxml.jackson.annotation;

    requires com.fasterxml.jackson.databind;

    requires org.slf4j;

    requires transitive org.bouncycastle.provider;

    requires transitive com.querydsl.core;

    requires transitive com.querydsl.collections;

    requires static lombok;

    requires java.validation;

    requires transitive static io.swagger.v3.oas.annotations;

    requires transitive atlassian.util.concurrent;

    requires transitive java.xml;

}
