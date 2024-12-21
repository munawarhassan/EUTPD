package com.pmi.tpd.euceg.api.entity;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public enum ProductStatus {
    /** */
    DRAFT("Draft"),
    /** */
    IMPORTED("Imported"),
    /** */
    VALID("Valid"),
    /** */
    SENT("Sent");

    private String name;

    private ProductStatus(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Map<String, String> toMap() {
        final Map<String, String> map = Maps.newLinkedHashMap();
        for (final ProductStatus e : ProductStatus.values()) {
            map.put(e.toString(), e.getName());
        }
        return map;
    }
}
