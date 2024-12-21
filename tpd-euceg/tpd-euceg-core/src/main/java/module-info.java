module tpd.euceg.core {

    exports com.pmi.tpd.euceg.core;

    exports com.pmi.tpd.euceg.core.importer;

    exports com.pmi.tpd.euceg.core.exporter;

    exports com.pmi.tpd.euceg.core.exporter.product;

    exports com.pmi.tpd.euceg.core.exporter.submission;

    exports com.pmi.tpd.euceg.core.exporter.submission.xml;

    exports com.pmi.tpd.euceg.core.exporter.submission.xpath;

    exports com.pmi.tpd.euceg.core.filestorage;

    exports com.pmi.tpd.euceg.core.filestorage.internal;

    exports com.pmi.tpd.euceg.core.task;

    exports com.pmi.tpd.euceg.core.refs;

    exports com.pmi.tpd.euceg.core.util.validation;

    opens com.pmi.tpd.euceg.core to com.fasterxml.jackson.databind;

    requires java.compiler;

    requires java.annotation;

    requires transitive tpd.api;

    requires transitive tpd.euceg.api;

    requires transitive tpd.backend.core;

    requires static transitive tpd.cluster;

    requires static transitive tpd.spring;

    requires transitive tpd.scheduler;

    requires transitive tpd.security;

    requires transitive javax.inject;

    requires transitive java.validation;

    requires static lombok;

    requires com.google.common;

    requires transitive org.joda.time;

    requires org.slf4j;

    requires org.apache.commons.lang3;

    requires org.apache.commons.io;

    requires transitive diff4j;

    requires transitive commons.jxpath;

    requires transitive commons.beanutils;

    requires transitive plexus.utils;

    /**
     * Jaxb requirements
     */

    requires jakarta.activation;

    requires transitive java.xml.bind;

    requires transitive jaxb2.basics.runtime;

    requires org.glassfish.jaxb.runtime;

    requires com.fasterxml.jackson.core;

    requires com.fasterxml.jackson.annotation;

    requires transitive com.fasterxml.jackson.databind;

    requires com.fasterxml.jackson.dataformat.yaml;

    /**
     * POI requirements
     */
    requires org.apache.poi.poi;

    requires org.apache.poi.ooxml;

    requires org.apache.commons.compress;

    requires org.apache.commons.codec;

    /**
     * Spring requirements
     */
    requires spring.context;

    requires spring.core;

    requires transitive spring.data.commons;

    /**
     * QueryDsl requirements
     */

    requires com.querydsl.collections;

    requires com.querydsl.codegen.utils;

    requires com.querydsl.core;

    requires ecj;

}
