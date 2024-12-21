package com.pmi.tpd.api.util;

/**
 * Convenience version of {@link IOperation} that does not define a checked exception on its interface.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@FunctionalInterface
public interface IUncheckedOperation<R> extends IOperation<R, RuntimeException> {

    @Override
    R perform();
}
