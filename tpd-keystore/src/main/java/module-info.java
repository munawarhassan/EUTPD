module tpd.keystore {

  /** cgib proxy */
  opens com.pmi.tpd.keystore to spring.core;

  exports com.pmi.tpd.keystore.event;

  exports com.pmi.tpd.keystore;

  exports com.pmi.tpd.keystore.spring;

  exports com.pmi.tpd.keystore.preference;

  exports com.pmi.tpd.keystore.model;

  requires java.compiler;

  requires transitive tpd.api;

  requires transitive tpd.security;

  requires tpd.spring;

  requires transitive java.annotation;

  requires transitive static javax.inject;

  requires transitive org.joda.time;

  requires com.fasterxml.jackson.databind;

  requires com.google.common;

  requires static lombok;

  /**
   * QueryDsl requirements
   */
  requires transitive static com.querydsl.core;

  requires transitive com.querydsl.collections;

  requires transitive ecj;

  /**
   * Spring requirements
   */
  requires spring.beans;

  requires transitive spring.core;

  requires transitive spring.data.commons;

  requires transitive spring.security.core;

  requires spring.context;

}