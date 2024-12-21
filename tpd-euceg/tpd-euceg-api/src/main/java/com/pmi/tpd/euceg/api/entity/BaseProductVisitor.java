package com.pmi.tpd.euceg.api.entity;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.Product;
import org.eu.ceg.TobaccoProduct;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class BaseProductVisitor<T> implements IProductVisitor<T>, Function<IProductEntity, T> {

    @Override
    public T apply(final @Nullable IProductEntity entity) {
        if (entity != null) {
            return entity.accept(this);
        }
        return null;
    }

    @Override
    public T visit(@Nonnull final IProductEntity entity) {
        if (entity.getProduct() != null) {
            visit(entity.getProduct());
        }
        return null;
    }

    @Override
    public Product visit(@Nonnull final Product product) {
        if (product instanceof TobaccoProduct) {
            return visit((TobaccoProduct) product);
        } else if (product instanceof EcigProduct) {
            return visit((EcigProduct) product);
        }
        return product;
    }

    @Override
    public Product visit(@Nonnull final EcigProduct product) {
        return product;
    }

    @Override
    public Product visit(@Nonnull final TobaccoProduct product) {
        return product;
    }

}
