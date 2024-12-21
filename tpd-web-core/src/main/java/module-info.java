module tpd.web.core {

    exports com.pmi.tpd.web.core.hateoas;

    exports com.pmi.tpd.web.core.support;

    exports com.pmi.tpd.web.core.servlet;

    exports com.pmi.tpd.web.core.rs.container;

    exports com.pmi.tpd.web.core.rs.annotation;

    exports com.pmi.tpd.web.core.rs.support;

    exports com.pmi.tpd.web.core.rs.jackson;

    exports com.pmi.tpd.web.core.rs.endpoint;

    exports com.pmi.tpd.web.core.rs.error;

    exports com.pmi.tpd.web.core.rs.renderer;

    exports com.pmi.tpd.web.core.request;

    exports com.pmi.tpd.web.core.request.event;

    exports com.pmi.tpd.web.core.request.spi;

    requires java.base;

    requires com.fasterxml.jackson.annotation;

    requires com.fasterxml.jackson.core;

    requires transitive com.fasterxml.jackson.databind;

    requires com.fasterxml.jackson.jaxrs.base;

    requires com.fasterxml.jackson.jaxrs.json;

    requires com.google.common;

    requires hk2.api;

    requires transitive java.annotation;

    requires java.validation;

    requires transitive java.ws.rs;

    requires transitive javax.inject;

    requires static javax.servlet.api;

    requires transitive jersey.client;

    requires transitive jersey.common;

    requires static lombok;

    requires org.apache.commons.lang3;

    requires org.joda.time;

    requires org.slf4j;

    requires transitive spring.hateoas;

    requires transitive spring.web;

    requires transitive spring.beans;

    requires transitive spring.context;

    requires transitive spring.core;

    requires transitive spring.data.commons;

    requires transitive io.swagger.v3.oas.annotations;

    requires tpd.api;

    requires transitive tpd.cluster;

    requires transitive tpd.security;

}
