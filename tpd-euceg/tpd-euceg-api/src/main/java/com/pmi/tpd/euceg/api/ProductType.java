package com.pmi.tpd.euceg.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;

import com.google.common.base.Preconditions;

/**
 * Enumeration identify each EUCEG product type for a specified product or a specified instance of submission or
 * product.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public enum ProductType {

    /** tobacco product type. */
    TOBACCO("Tobacco Products", TobaccoProductSubmission.class, TobaccoProduct.class),
    /** e-cigarette type. */
    ECIGARETTE("E-Cigarettes", EcigProductSubmission.class, EcigProduct.class);

    /** */
    private final Class<?>[] instanceClasses;

    /** */
    private final String label;

    ProductType(final String label, final Class<?>... cl) {
        this.label = label;
        this.instanceClasses = cl;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Gets the product type corresponding to the specified entity.
     *
     * @param entity
     *               a entity to use (can <b>not</b> be {@code null}).
     * @return Returns the product type corresponding to the specified entity, {@code null} otherwise.
     */
    @Nullable
    public static ProductType productType(@Nonnull final Object entity) {
        return productType(Preconditions.checkNotNull(entity).getClass());
    }

    /**
     * Gets the product type corresponding to the specified type.
     *
     * @param cl
     *           the type (can <b>not</b> be {@code null}).
     * @return Returns the product type corresponding to the specified type, {@code null} otherwise.
     */
    @Nullable
    public static ProductType productType(@Nonnull final Class<?> cl) {
        Preconditions.checkNotNull(cl, "cl");
        for (final ProductType productType : ProductType.values()) {
            for (final Class<?> clz : productType.instanceClasses) {
                if (clz.equals(cl)) {
                    return productType;
                }
            }

        }
        return null;
    }

}
