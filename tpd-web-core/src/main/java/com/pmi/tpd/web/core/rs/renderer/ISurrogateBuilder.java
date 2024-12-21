package com.pmi.tpd.web.core.rs.renderer;

/**
 * Builds JSON surrogates from original model objects.
 * 
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISurrogateBuilder<T, R> {

  T buildFor(R object);
}
