package com.pmi.tpd.euceg.api.entity;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author christophe friederich
 * @since 2.5
 */
public enum ProductPirStatus {

    /** The product was imported for the first time, but has not yet been sent. */
    AWAITING("Awaiting"),
    /**
     * The product has been sent, the product has not been withdrawn form all associated national markets. To say
     * otherwise, the withdrawal date is not filled for each product presentation . And it is not replaced by a new
     * product (a product references the product {@link ProductEntity#getChild()}.
     */
    ACTIVE("Active"),
    /** The product is replaced by a new product, but has not been withdrawn from all national markets. */
    INACTIVE("Inactive"),
    /** The product has been withdrawn form all national markets. */
    WITHDRAWN("Withdrawn");

    private String name;

    private ProductPirStatus(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Map<String, String> toMap() {
        final Map<String, String> map = Maps.newLinkedHashMap();
        for (final ProductPirStatus e : ProductPirStatus.values()) {
            map.put(e.toString(), e.getName());
        }
        return map;
    }
}
