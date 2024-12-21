package com.pmi.tpd.core.euceg.spi;

/**
 * <p>
 * IProductIdGeneratorRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductIdGeneratorRepository {

    String getNextProductId(String submitterId);
}
