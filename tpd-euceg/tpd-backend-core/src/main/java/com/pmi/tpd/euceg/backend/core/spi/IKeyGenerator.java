package com.pmi.tpd.euceg.backend.core.spi;

/**
 * Key generator.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IKeyGenerator {

    /**
     * @return A freshly generated key dependent on the concrete class used
     */
    byte[] getEncodedKey();
}
