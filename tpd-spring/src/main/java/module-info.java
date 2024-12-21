module tpd.spring {

    exports com.pmi.tpd.spring.context.bind to spring.beans;

    exports com.pmi.tpd.spring.context;

    exports com.pmi.tpd.spring.convert;

    opens com.pmi.tpd.spring.convert;

    exports com.pmi.tpd.spring.env;

    exports com.pmi.tpd.spring.transaction;

    requires transitive tpd.api;

    requires java.annotation;

    requires com.google.common;

    requires transitive java.desktop;

    requires java.validation;

    requires javax.inject;

    requires org.slf4j;

    requires org.yaml.snakeyaml;

    requires transitive spring.beans;

    requires transitive spring.context;

    requires transitive spring.core;

    requires spring.jcl;

    requires spring.web;

    requires transitive static spring.data.commons;

    requires transitive spring.tx;

    requires transitive static com.querydsl.core;
}
