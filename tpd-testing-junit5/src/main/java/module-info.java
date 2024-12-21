
module tpd.testing.junit5 {

  exports com.pmi.tpd.testing.junit5;

  exports com.pmi.tpd.testing.mockito;

  exports com.pmi.tpd.testing.query;

  requires java.annotation;

  requires transitive org.junit.jupiter.api;

  requires org.junit.jupiter.params;

  requires org.junit.jupiter.engine;

  requires org.junit.platform.engine;

  requires org.junit.platform.launcher;

  requires org.junit.platform.commons;

  requires org.hamcrest;

  requires transitive org.mockito;

  requires org.mockito.junit.jupiter;

  requires com.google.common;

  requires org.slf4j;

  requires org.apache.commons.logging;

}
