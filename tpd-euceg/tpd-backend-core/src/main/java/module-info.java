module tpd.backend.core {

    exports com.pmi.tpd.euceg.backend.core;

    exports com.pmi.tpd.euceg.backend.core.domibus.api;

    exports com.pmi.tpd.euceg.backend.core.domibus.api.model;

    opens com.pmi.tpd.euceg.backend.core.domibus.api.model to com.fasterxml.jackson.databind;

    exports com.pmi.tpd.euceg.backend.core.event;

    exports com.pmi.tpd.euceg.backend.core.delivery;

    exports com.pmi.tpd.euceg.backend.core.delivery.mock;

    exports com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

    exports com.pmi.tpd.euceg.backend.core.domibus.ws;

    exports com.pmi.tpd.euceg.backend.core.message;

    exports com.pmi.tpd.euceg.backend.core.spi;

    requires transitive tpd.api;

    requires transitive tpd.euceg.api;

    requires tpd.euceg.backend.ws.stubs;

    requires tpd.euceg.plugin.ws.stubs;

    requires java.naming;

    requires transitive javax.inject;

    requires static java.annotation;

    requires static lombok;

    requires org.slf4j;

    requires com.google.common;

    requires org.joda.time;

    requires org.apache.commons.codec;

    requires transitive static com.querydsl.core;

    /**
     * JAX-RS client requirements
     */
    requires transitive java.ws.rs;

    requires jersey.client;

    requires jersey.common;

    requires jersey.media.json.jackson;

    requires transitive com.fasterxml.jackson.annotation;

    requires com.fasterxml.jackson.databind;

    requires transitive spring.data.commons;

    /**
     * crypto requirements
     */

    requires org.bouncycastle.pkix;

    requires org.bouncycastle.provider;

    /**
     * WS requirements
     */

    requires jakarta.activation;

    requires java.xml.bind;

    requires java.jws;

    requires transitive java.xml.ws;

    requires org.apache.cxf.core;

    requires org.apache.cxf.frontend.jaxws;

    requires org.apache.cxf.transport.http;

    requires org.apache.cxf.frontend.simple;

    requires org.apache.cxf.logging;

    requires com.ctc.wstx;

    /**
     * JMS requirements
     */

    requires transitive spring.core;

    requires static spring.beans;

    requires transitive static javax.jms.api;

    requires transitive static spring.jms;

    requires static activemq.client;

    requires transitive spring.tx;

    requires spring.context;

    requires transitive java.logging;

    uses com.ctc.wstx.stax.WstxEventFactory;

    uses com.ctc.wstx.stax.WstxInputFactory;

    uses com.ctc.wstx.stax.WstxOutputFactory;

}
