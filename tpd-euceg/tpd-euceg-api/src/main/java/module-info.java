module tpd.euceg.api {

    exports org.eu.ceg;

    opens org.eu.ceg;

    exports com.pmi.tpd.euceg.api.entity;

    exports com.pmi.tpd.euceg.api;

    exports com.pmi.tpd.euceg.api.binding;

    opens com.pmi.tpd.euceg.api.binding;

    requires com.google.common;

    requires jakarta.activation;

    requires java.datatransfer;

    requires java.xml;

    requires static java.annotation;

    requires java.xml.bind;

    requires transitive org.eclipse.persistence.moxy;

    requires transitive org.eclipse.persistence.core;

    requires jaxb2.basics.runtime;

    requires org.joda.time;

    requires org.slf4j;

    requires transitive tpd.api;

    requires transitive static com.querydsl.core;

    requires transitive spring.data.commons;

}
